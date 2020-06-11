package com.linkedin.playrestli;

import com.linkedin.r2.message.QueryTunnelUtil;
import com.linkedin.r2.message.rest.RestRequest;
import com.linkedin.r2.message.rest.RestRequestBuilder;
import com.linkedin.r2.transport.common.bridge.common.TransportResponseImpl;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.api.http.CookiesConfiguration;
import play.api.http.HttpConfiguration;
import play.mvc.Http;


/**
 * Created by rli on 1/29/16.
 *
 * Manages the Rest.li resource dispatcher, which is what's responsible for routing and processing requests in a
 * Rest.li server.
 *
 */
@Singleton
public final class RestliServerComponent extends BaseRestliServerComponent<RestRequest> implements RestliServerApi {

  @Inject
  public RestliServerComponent(CookiesConfiguration cookiesConfiguration,
      HttpDispatcher httpDispatcher, RestliUriResolver restliUriResolver) {
    super(cookiesConfiguration, httpDispatcher, restliUriResolver);
  }

  /**
   * Take a Play request, convert it to a rest.li request, have the rest.li dispatcher process it.
   */
  @Override
  public void handleRequest(final Http.Request request, final RestliTransportCallback callback) throws Exception {
    RestRequest restRequest = createRestRequest(request);
    if (restRequest == null) {
      callback.onResponse(TransportResponseImpl.success(notFound(request.uri())));
    } else {
      _restliDispatcher.handleRequest(restRequest, createRequestContext(request), callback);
    }
  }

  RestRequest createRestRequest(Http.Request request) throws Exception {
    RestRequestBuilder builder = createRequestBuilder(request, RestRequestBuilder::new).orElse(null);
    if (builder == null) {
      return null;
    } else {
      builder.setEntity(request.body().asRaw().asBytes().toArray());
      return QueryTunnelUtil.decode(builder.build());
    }
  }
}
