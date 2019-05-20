package com.linkedin.playrestli;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.linkedin.r2.message.stream.StreamResponse;
import com.linkedin.r2.message.stream.entitystream.ReadHandle;
import com.linkedin.r2.message.stream.entitystream.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import play.api.http.HttpChunk;


/**
 * Created by qliu on 10/14/14.
 *
 * Restli stream callback. Wraps a promise and redeems it both on error and success so the calling end
 * can act on Restli errors.
 *
 */
public class RestliStreamTransportCallback
    extends BaseRestliTransportCallback<StreamResponse, GenericStreamResponse, Source<HttpChunk, ?>> {
  private static class EntityStreamReader implements Reader {
    private final AtomicBoolean _done = new AtomicBoolean(false);
    private ReadHandle _rh;
    private ConcurrentLinkedQueue<CompletableFuture<Optional<Pair<NotUsed, HttpChunk>>>> _completableFutures =
        new ConcurrentLinkedQueue<>();

    @Override
    public void onInit(ReadHandle rh) {
      _rh = rh;
    }

    @Override
    public void onDataAvailable(com.linkedin.data.ByteString data) {
      if (!data.isEmpty()) {
        _completableFutures.remove()
            .complete(Optional.of(
                Pair.create(NotUsed.getInstance(), new HttpChunk.Chunk(ByteString.fromArray(data.copyBytes())))));
      }
    }

    @Override
    public synchronized void onDone() {
      _done.set(true);
      // When stream is done, notify all remaining promises
      _completableFutures.forEach(completableFuture -> {
        if (!completableFuture.isDone()) {
          completableFuture.complete(Optional.empty());
        }
      });
      _completableFutures.clear();
    }

    @Override
    public synchronized void onError(Throwable e) {
      // When stream is wrong, notify all remaining promises
      _completableFutures.forEach(completableFuture -> completableFuture.completeExceptionally(e));
      _completableFutures.clear();
    }

    public CompletionStage<Optional<Pair<NotUsed, HttpChunk>>> readNextChunk() {
      CompletableFuture<Optional<Pair<NotUsed, HttpChunk>>> completableFuture = new CompletableFuture<>();
      _completableFutures.add(completableFuture);
      if (_done.get()) {
        completableFuture.complete(Optional.empty());
      } else {
        _rh.request(1);
      }

      return completableFuture;
    }
  }

  @Override
  protected GenericStreamResponse createErrorResponse(int status, Map<String, String> headers) {
    return new GenericStreamResponse(status, headers);
  }

  @Override
  protected GenericStreamResponse createResponse(int status, Map<String, String> headers, List<String> cookies,
      StreamResponse response) {
    EntityStreamReader reader = new EntityStreamReader();
    response.getEntityStream().setReader(reader);

    // Note that, we can't provide an explicit ExecutionContext here. Since the implementation of Source.unfoldAsync
    // reuses the same thread, the ThreadLocal state should be fine.
    Source<HttpChunk, NotUsed> body = Source.unfoldAsync(NotUsed.getInstance(), __ -> reader.readNextChunk());
    return new GenericStreamResponse(status, headers, cookies, body);
  }
}
