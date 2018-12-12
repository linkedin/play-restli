package com.linkedin.playrestli;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.linkedin.common.callback.Callback;
import com.linkedin.data.ByteString;
import com.linkedin.r2.filter.R2Constants;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.stream.StreamRequest;
import com.linkedin.r2.message.stream.entitystream.EntityStream;
import com.linkedin.r2.message.stream.entitystream.EntityStreams;
import com.linkedin.r2.message.stream.entitystream.ReadHandle;
import com.linkedin.r2.message.stream.entitystream.Reader;
import com.linkedin.r2.message.stream.entitystream.WriteHandle;
import com.linkedin.r2.message.stream.entitystream.Writer;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import play.libs.Json;
import play.mvc.Http;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


/**
 * Created by qliu
 *
 */
@RunWith(DataProviderRunner.class)
public class TestRestliStreamServerComponent {

  private static final String CHARSET = "UTF-8";
  private static final byte[] EMPTY_DATA = new byte[0];
  private RestliServerStreamComponent restliServer;
  private HttpDispatcher _httpDispatcher;
  @Before
  public void setUp() {
    _httpDispatcher = createMock(HttpDispatcher.class);
    restliServer = new RestliServerStreamComponent(_httpDispatcher);
  }

  @Test
  @UseDataProvider("testRemoteAddressData")
  public void testRemoteAddress(final String remoteAddress) throws Exception {
    Capture<RequestContext> captureContext = newCapture();
    _httpDispatcher.handleRequest(EasyMock.<StreamRequest> anyObject(), capture(captureContext), anyObject());
    expectLastCall();
    replay(_httpDispatcher);

    Http.RequestImpl request = createMockRequest(new byte[0]).remoteAddress(remoteAddress).build();
    restliServer.handleRequest(request, new RestliStreamTransportCallback());

    verify(_httpDispatcher);
    assertEquals(remoteAddress, captureContext.getValue().getLocalAttr(R2Constants.REMOTE_ADDR));
  }

  @DataProvider
  public static Object[][] testRemoteAddressData(){
    return new String[][]{{"127.0.0.1"}, {null}};
  }

  @Test
  public void testCreateRestRequestEmpty() throws Exception {
    Http.RequestBuilder request = createMockRequest(EMPTY_DATA);
    doCreateStreamRequestTest(request.build(), EMPTY_DATA);
  }

  @Test
  public void testCreateRestRequestWithHeaders() throws Exception {
    Http.RequestBuilder request = createMockRequest(EMPTY_DATA);
    request.method("POST");
    request.uri("/foo/bar");
    request.headers(new Http.Headers(ImmutableMap.of("foo", ImmutableList.of("bar"))));
    doCreateStreamRequestTest(request.build(), EMPTY_DATA);
  }

  @Test
  public void testCreateRestRequestWithJsonBody() throws Exception {
    byte[] data = jsonToBytes(Json.toJson(ImmutableMap.of("foo", new String[] {"bar"})));
    Http.RequestBuilder request = createMockRequest(data);
    doCreateStreamRequestTest(request.build(), data);
  }

  private void doCreateStreamRequestTest(Http.Request request, byte[] data) throws Exception {
    CompletableFuture<StreamRequest> future = new CompletableFuture<>();
    restliServer.createStreamRequest(request, new Callback<StreamRequest>() {
      @Override
      public void onError(Throwable e) {
        future.completeExceptionally(e);
      }

      @Override
      public void onSuccess(StreamRequest result) {
        future.complete(result);
      }
    });

    StreamRequest restRequest = future.join();

    assertEquals(request.method(), restRequest.getMethod());
    assertEquals(restliServer.toSimpleMap(request.getHeaders().toMap()), restRequest.getHeaders());

    assertEquals(new URI(request.uri()), restRequest.getURI());

    assertArrayEquals(data, readEntityStream(restRequest.getEntityStream()));
  }

  private byte[] readEntityStream(EntityStream entityStream) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    entityStream.setReader(new Reader() {
      private ReadHandle _readHandle;
      @Override
      public void onInit(ReadHandle rh) {
        _readHandle = rh;
        _readHandle.request(1);
      }

      @Override
      public void onDataAvailable(ByteString data) {
        try {
          data.write(baos);
        } catch (IOException e) {
          // ignore
        }
        _readHandle.request(1);
      }

      @Override
      public void onDone() {
        try {
          baos.close();
        } catch (IOException e) {
          // ignore
        }
      }

      @Override
      public void onError(Throwable e) {
        try {
          baos.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    });

    return baos.toByteArray();
  }

  private byte[] jsonToBytes(JsonNode json) throws UnsupportedEncodingException {
    return Json.stringify(json).getBytes(CHARSET);
  }

  private Http.RequestBuilder createMockRequest(byte[] data) {
    return new Http.RequestBuilder() {
      @Override
      public Http.RequestImpl build() {
        req = req.withBody(new Http.RequestBody(EntityStreams.newEntityStream(new Writer() {
          private WriteHandle _writeHandle;

          @Override
          public void onInit(WriteHandle wh) {
            _writeHandle = wh;
          }

          @Override
          public void onWritePossible() {
            _writeHandle.write(ByteString.copy(data));
            _writeHandle.done();
          }

          @Override
          public void onAbort(Throwable e) {
            // ignore
          }
        })));
        return super.build();
      }
    };
  }
}
