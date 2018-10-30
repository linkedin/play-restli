package com.linkedin.restli.server.play;

import akka.actor.ActorSystem;
import akka.japi.function.Procedure;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import com.google.common.collect.ImmutableMap;
import com.linkedin.restli.server.play.mock.MockRestResponse;
import com.linkedin.restli.server.play.mock.MockStreamResponse;
import com.linkedin.r2.message.rest.RestException;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.message.stream.StreamResponse;
import com.linkedin.r2.transport.common.bridge.common.TransportResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import scala.compat.java8.FutureConverters;

import static org.testng.AssertJUnit.assertEquals;


/**
 * Created by rli on 10/14/14.
 *
 */
public class TestRestliStreamTransportCallback {

  private RestliStreamTransportCallback callback;
  private static final String WIRE_ATTR_HEADER_KEY = "X-LI-R2-W-wireAttrKey";
  private static final String WIRE_ATTR_KEY = "wireAttrKey";
  private static final String WIRE_ATTR_VALUE = "wireAttrValue";
  private Materializer materializer;

  @BeforeMethod
  public void setUp() {
    callback = new RestliStreamTransportCallback();
    final ActorSystem system = ActorSystem.create("test");
    ActorMaterializerSettings settings = ActorMaterializerSettings.create(system);
    materializer = ActorMaterializer.create(settings, system, "test");
  }

  @Test
  public void testRestliSuccessToPromiseEmpty() throws Exception {
    MockStreamResponse response = new MockStreamResponse();
    doRestliSuccessToPromiseTest(response);
  }

  @Test
  public void testRestliSuccessToPromiseWithHeaders() throws Exception {
    MockStreamResponse response = new MockStreamResponse().withHeaders(ImmutableMap.of("foo", "bar"));
    doRestliSuccessToPromiseTest(response);
  }

  @Test
  public void testRestliSuccessToPromiseWithEntity() throws Exception {
    MockStreamResponse response = new MockStreamResponse().withEntity("Hello World");
    doRestliSuccessToPromiseTest(response);
  }

  private void doRestliSuccessToPromiseTest(MockStreamResponse restResponse) throws Exception {
    callback.onResponse(createTransportResponse(restResponse));
    GenericStreamResponse response = FutureConverters.toJava(callback.getPromise().future()).toCompletableFuture().get(1000L, TimeUnit.MILLISECONDS);
    assertEquals(restResponse.getStatus(), response.getStatus());
    for (Map.Entry<String, String> expected: restResponse.getHeaders().entrySet()) {
      assertEquals(expected.getValue(), response.getHeaders().get(expected.getKey()));
    }
    assertEquals(WIRE_ATTR_VALUE, response.getHeaders().get(WIRE_ATTR_HEADER_KEY));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    response.getBody().runWith(Sink.foreach((Procedure<ByteString>) data -> {
      try {
        baos.write(data.toArray());
      } catch (IOException e) {
        // ignore
      }
    }).mapMaterializedValue(doneCompletionStage -> doneCompletionStage.thenAcceptAsync(__ -> {
        try {
          baos.close();
        } catch (IOException e) {
          // ignore
        }
      })), materializer).toCompletableFuture().get(1, TimeUnit.DAYS);

    assertEquals(restResponse.getEntity().copyBytes(), baos.toByteArray());
  }

  @Test
  public void testRestliErrorToPromiseNonRestliException() {
    final RuntimeException expected = new RuntimeException();
    callback.onResponse(createTransportResponse(expected));
    FutureConverters.toJava(callback.getPromise().future()).exceptionally(throwable -> {
      assertEquals(expected, throwable);
      return null;
    });
  }

  @Test
  public void testRestliErrorToPromiseRestliExceptionWithEmptyMessage() throws Exception {
    int expectedStatus = 555;
    String expectedMessage = "Unspecified Rest.li error";
    doTestRestliErrorToPromise(expectedStatus, expectedMessage, "{}");
  }

  @Test
  public void testRestliErrorToPromiseRestliExceptionWithMessage() throws Exception {
    int expectedStatus = 500;
    String expectedMessage = "Something went wrong";
    doTestRestliErrorToPromise(expectedStatus, expectedMessage, String.format("{\"message\":\"%s\"}", expectedMessage));
  }

  private void doTestRestliErrorToPromise(int status, String message, String entity) throws Exception {
    RestResponse restResponse = new MockRestResponse().withStatus(status).withEntity(entity);
    RestException exception = new RestException(restResponse);
    callback.onResponse(createTransportResponse(exception));
    GenericStreamResponse response = FutureConverters.toJava(callback.getPromise().future()).toCompletableFuture().get(1000L, TimeUnit.MILLISECONDS);
    assertEquals(status, response.getStatus());
    assertEquals(message, response.getHeaders().get(RestliConstants.RESTLI_ERROR_HEADER));
    assertEquals(WIRE_ATTR_VALUE, response.getHeaders().get(WIRE_ATTR_HEADER_KEY));
  }

  private TransportResponse<StreamResponse> createTransportResponse(final StreamResponse response) {
    return new TransportResponse<StreamResponse>() {
      @Override
      public StreamResponse getResponse() {
        return response;
      }

      @Override
      public boolean hasError() {
        return false;
      }

      @Override
      public Throwable getError() {
        return null;
      }

      @Override
      public Map<String, String> getWireAttributes() {
        return ImmutableMap.of(WIRE_ATTR_KEY, WIRE_ATTR_VALUE);
      }
    };
  }

  private TransportResponse<StreamResponse> createTransportResponse(final Throwable throwable) {
    return new TransportResponse<StreamResponse>() {
      @Override
      public StreamResponse getResponse() {
        return null;
      }

      @Override
      public boolean hasError() {
        return true;
      }

      @Override
      public Throwable getError() {
        return throwable;
      }

      @Override
      public Map<String, String> getWireAttributes() {
        return ImmutableMap.of(WIRE_ATTR_KEY, WIRE_ATTR_VALUE);
      }
    };
  }
}
