package com.linkedin.playrestli;

import com.google.common.collect.ImmutableMap;
import com.linkedin.playrestli.mock.MockRestResponse;
import com.linkedin.r2.message.rest.RestException;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.transport.common.bridge.common.TransportResponse;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import scala.compat.java8.FutureConverters;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by rli on 10/14/14.
 *
 */
public class TestRestliTransportCallback {

  private RestliTransportCallback callback;
  private static final String WIRE_ATTR_HEADER_KEY = "X-LI-R2-W-wireAttrKey";
  private static final String WIRE_ATTR_KEY = "wireAttrKey";
  private static final String WIRE_ATTR_VALUE = "wireAttrValue";

  @BeforeMethod
  public void setUp() throws Exception {
    callback = new RestliTransportCallback();
  }

  @Test
  public void testRestliSuccessToPromiseEmpty() throws Exception {
    MockRestResponse response = new MockRestResponse();
    doRestliSuccessToPromiseTest(response);
  }

  @Test
  public void testRestliSuccessToPromiseWithHeaders() throws Exception {
    MockRestResponse response = new MockRestResponse().withHeaders(ImmutableMap.of("foo", "bar"));
    doRestliSuccessToPromiseTest(response);
  }

  @Test
  public void testRestliSuccessToPromiseWithEntity() throws Exception {
    MockRestResponse response = new MockRestResponse().withEntity("Hello World");
    doRestliSuccessToPromiseTest(response);
  }

  private void doRestliSuccessToPromiseTest(RestResponse restResponse) throws Exception {
    callback.onResponse(createTransportResponse(restResponse));
    GenericResponse response = FutureConverters.toJava(callback.getPromise().future()).toCompletableFuture().get(1000L, TimeUnit.MILLISECONDS);
    assertEquals(restResponse.getStatus(), response.getStatus());
    for (Map.Entry<String, String> expected: restResponse.getHeaders().entrySet()) {
      assertEquals(expected.getValue(), response.getHeaders().get(expected.getKey()));
    }
    assertEquals(WIRE_ATTR_VALUE, response.getHeaders().get(WIRE_ATTR_HEADER_KEY));
    assertEquals(restResponse.getEntity().copyBytes(), response.getBody());
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
    GenericResponse response = FutureConverters.toJava(callback.getPromise().future()).toCompletableFuture().get(1000L, TimeUnit.MILLISECONDS);
    assertEquals(status, response.getStatus());
    assertEquals(message, response.getHeaders().get(RestliConstants.RESTLI_ERROR_HEADER));
    assertEquals(WIRE_ATTR_VALUE, response.getHeaders().get(WIRE_ATTR_HEADER_KEY));
  }

  private TransportResponse<RestResponse> createTransportResponse(final RestResponse response) {
    return new TransportResponse<RestResponse>() {
      @Override
      public RestResponse getResponse() {
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

  private TransportResponse<RestResponse> createTransportResponse(final Throwable throwable) {
    return new TransportResponse<RestResponse>() {
      @Override
      public RestResponse getResponse() {
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
