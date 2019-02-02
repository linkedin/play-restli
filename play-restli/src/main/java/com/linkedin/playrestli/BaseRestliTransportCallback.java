package com.linkedin.playrestli;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.linkedin.common.callback.Callback;
import com.linkedin.data.ByteString;
import com.linkedin.r2.message.Response;
import com.linkedin.r2.message.rest.RestException;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.message.stream.StreamException;
import com.linkedin.r2.message.stream.entitystream.FullEntityReader;
import com.linkedin.r2.transport.common.WireAttributeHelper;
import com.linkedin.r2.transport.common.bridge.common.TransportCallback;
import com.linkedin.r2.transport.common.bridge.common.TransportResponse;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import play.Logger;
import play.libs.Json;


/**
 * Created by qliu on 3/1/16.
 */
public abstract class BaseRestliTransportCallback<T extends Response, P extends BaseGenericResponse<B>, B> implements
                                                                                                           TransportCallback<T> {
  private static final Logger.ALogger LOGGER = Logger.of(BaseRestliTransportCallback.class);

  private static final Charset CHARSET = Charset.forName("UTF-8");

  protected final CompletableFuture<P> _completableFuture = new CompletableFuture<>();

  public CompletableFuture<P> getCompletableFuture() {
    return _completableFuture;
  }

  @Override
  public void onResponse(TransportResponse<T> response) {
    redeemCompletableFutureWithRestliResponse(_completableFuture, response);
  }

  /**
   * Convert the given rest.li response to a Play Result CompletableFuture. This method will convert rest.li errors, status
   * codes, and data into a format Play can use for a Result.
   *
   * @param response
   */
  private void redeemCompletableFutureWithRestliResponse(CompletableFuture<P> completableFuture, TransportResponse<T> response) {
    if (response.hasError()) {
      redeemCompletableFutureWithRestliError(completableFuture, response.getError(), response.getWireAttributes());
    } else {
      redeemCompletableFutureWithRestliSuccess(completableFuture, response.getResponse(), response.getWireAttributes());
    }
  }

  private void redeemCompletableFutureWithRestliSuccess(CompletableFuture<P> completableFuture, T response, Map<String, String> wireAttrs) {
    Map<String, String> allHeaders = ImmutableMap.<String, String>builder()
        .putAll(WireAttributeHelper.toWireAttributes(wireAttrs))
        .putAll(response.getHeaders())
        .build();
    completableFuture.complete(createResponse(response.getStatus(), allHeaders, response.getCookies(), response));
  }

  protected abstract P createResponse(int status, Map<String, String> headers, List<String> cookies, T response);

  protected abstract P createErrorResponse(int status, Map<String, String> headers);

  private void redeemCompletableFutureWithRestliError(CompletableFuture<P> completableFuture,
      Throwable error,
      Map<String, String> wireAttrs) {
    if (error instanceof RestException) {
      redeemCompletableFutureWithRestliError(completableFuture, ((RestException) error).getResponse(), wireAttrs, RestResponse::getEntity);
    } else if (error instanceof StreamException) {
      redeemCompletableFutureWithRestliError(completableFuture, ((StreamException) error).getResponse(), wireAttrs, response -> {
        CompletableFuture<ByteString> dataFuture = new CompletableFuture<>();
        FullEntityReader reader = new FullEntityReader(new Callback<ByteString>() {
          @Override
          public void onError(Throwable e) {
            LOGGER.error("Failed to read the entity stream.", e);
            dataFuture.complete(ByteString.empty());
          }

          @Override
          public void onSuccess(ByteString result) {
            dataFuture.complete(result);
          }
        });
        response.getEntityStream().setReader(reader);
        return dataFuture.join();
      });
    } else {
      completableFuture.completeExceptionally(error);
    }
  }

  private <T extends Response> void redeemCompletableFutureWithRestliError(CompletableFuture<P> completableFuture,
      T response,
      Map<String, String> wireAttrs,
      Function<T, ByteString> getEntity) {
    JsonNode entity = Json.parse(getEntity.apply(response).asString(CHARSET));
    String textValue = entity.findPath("message").textValue();
    String errorMessage = textValue == null ? "Unspecified Rest.li error" : textValue;

    if (response.getStatus() >= 500) {
      LOGGER.error(String.format("Rest.li returned a %d response code: %s", response.getStatus(), errorMessage));
    }

    Map<String, String> allHeaders = ImmutableMap.<String, String>builder().putAll(WireAttributeHelper.toWireAttributes(wireAttrs))
        .put(RestliConstants.RESTLI_ERROR_HEADER, errorMessage)
        .build();
    completableFuture.complete(createErrorResponse(response.getStatus(), allHeaders));
  }
}
