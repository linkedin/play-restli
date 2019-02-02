package com.linkedin.playrestli;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.linkedin.r2.filter.R2Constants;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.rest.RestRequest;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import play.api.http.CookiesConfiguration;
import play.libs.Json;
import play.mvc.Http;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


/**
 * Created by rli on 8/27/14.
 *
 */
@RunWith(DataProviderRunner.class)
public class TestRestliServerComponent {

  private static final String CHARSET = "UTF-8";
  private RestliServerComponent restliServer;
  private HttpDispatcher _httpDispatcher;

  @Before
  public void setUp() {
    _httpDispatcher = createMock(HttpDispatcher.class);
    restliServer = new RestliServerComponent(_httpDispatcher, CookiesConfiguration.apply(true));
  }

  @Test
  @UseDataProvider("testRemoteAddressData")
  public void testRemoteAddress(final String remoteAddress) throws Exception {
    Capture<RequestContext> captureContext  = newCapture();
    _httpDispatcher.handleRequest(EasyMock.<RestRequest> anyObject(), capture(captureContext), anyObject());
    expectLastCall();
    replay(_httpDispatcher);

    Http.Request request = new Http.RequestBuilder().remoteAddress(remoteAddress).bodyRaw(new byte[0]).build();
    restliServer.handleRequest(request, new RestliTransportCallback());

    verify(_httpDispatcher);
    assertEquals(remoteAddress, captureContext.getValue().getLocalAttr(R2Constants.REMOTE_ADDR));
  }

  @DataProvider
  public static Object[][] testRemoteAddressData(){
    return new String[][]{{"127.0.0.1"}, {null}};
  }


  @Test
  public void testCreateRestRequestEmpty() throws Exception {
    Http.Request request = new Http.RequestBuilder().bodyRaw(new byte[0]).build();
    doCreateRestRequestTest(request);
  }

  @Test
  public void testCreateRestRequestWithHeaders() throws Exception {
    Http.Headers headers = new Http.Headers(ImmutableMap.of("foo", ImmutableList.of("bar")));
    Http.Request request = new Http.RequestBuilder().method("POST").uri("/foo/bar").headers(headers).bodyRaw(new byte[0]).build();
    doCreateRestRequestTest(request);
  }

  @Test
  public void testCreateRestRequestWithJsonBody() throws Exception {
    Http.Request request = new Http.RequestBuilder().bodyRaw(jsonToBytes(Json.toJson(ImmutableMap.of("foo", new String[] {"bar"})))).build();
    doCreateRestRequestTest(request);
  }

  private void doCreateRestRequestTest(Http.Request request) throws Exception {
    Capture<RestRequest> captureRestRequest = newCapture();
    _httpDispatcher.handleRequest(capture(captureRestRequest), anyObject(RequestContext.class), anyObject(RestliTransportCallback.class));
    expectLastCall();
    replay(_httpDispatcher);

    restliServer.handleRequest(request, new RestliTransportCallback());
    verify(_httpDispatcher);

    RestRequest restRequest = captureRestRequest.getValue();

    assertEquals(request.method(), restRequest.getMethod());

    for (Map.Entry<String, List<String>> entry : request.getHeaders().toMap().entrySet()) {
      assertEquals(entry.getValue(), restRequest.getHeaderValues(entry.getKey()));
    }

    assertEquals(new URI(request.uri()), restRequest.getURI());

    String entityString = restRequest.getEntity().asString(CHARSET);
    assertEquals(request.body().asRaw().asBytes().utf8String(), entityString);
  }

  private byte[] jsonToBytes(JsonNode json) throws UnsupportedEncodingException {
    return Json.stringify(json).getBytes(CHARSET);
  }
}
