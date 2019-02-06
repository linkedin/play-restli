package com.linkedin.playrestli;

import com.linkedin.common.callback.Callback;
import com.linkedin.r2.message.Messages;
import com.linkedin.r2.message.QueryTunnelUtil;
import com.linkedin.r2.message.stream.StreamRequest;
import com.linkedin.r2.message.stream.StreamRequestBuilder;
import com.linkedin.r2.message.stream.entitystream.DrainReader;
import com.linkedin.r2.message.stream.entitystream.EntityStream;
import com.linkedin.r2.transport.common.bridge.common.TransportResponseImpl;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.api.http.CookiesConfiguration;
import play.api.http.HttpConfiguration;
import play.mvc.Http;


/**
 * Created by qliu on 1/29/16.
 *
 * Manages the Rest.li resource dispatcher, which is what's responsible for routing and processing requests in a
 * Rest.li server using streaming.
 *
 */
@Singleton
public final class RestliServerStreamComponent extends BaseRestliServerComponent<StreamRequest>
    implements RestliServerStreamApi {
  @Inject
  public RestliServerStreamComponent(HttpConfiguration httpConfiguration, CookiesConfiguration cookiesConfiguration,
      HttpDispatcher httpDispatcher) {
    super(httpConfiguration, cookiesConfiguration, httpDispatcher);
  }

  /**
   * Take a Play request, convert it to a rest.li request, have the rest.li dispatcher process it.
   */
  @Override
  public void handleRequest(final Http.Request request, final RestliStreamTransportCallback callback) throws Exception {
    StreamRequest streamRequest = createStreamRequest(request);
    if (streamRequest == null) {
      callback.onResponse(TransportResponseImpl.success(Messages.toStreamResponse(notFound(request.uri()))));
      request.body().as(EntityStream.class).setReader(new DrainReader());
    } else {
      QueryTunnelUtil.decode(streamRequest, new Callback<StreamRequest>() {
        @Override
        public void onError(Throwable e) {
          callback.onResponse(TransportResponseImpl.error(e));
        }

        @Override
        public void onSuccess(StreamRequest result) {
          _restliDispatcher.handleRequest(result, createRequestContext(request), callback);
        }
      });
    }
  }

  StreamRequest createStreamRequest(Http.Request request) throws Exception {
    StreamRequestBuilder builder = createRequestBuilder(request, StreamRequestBuilder::new).orElse(null);
    if (builder == null) {
      return null;
    } else {
      return builder.build(request.body().as(EntityStream.class));
    }
  }
}
