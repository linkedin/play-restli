package com.linkedin.playrestli;

import com.linkedin.parseq.Engine;
import com.linkedin.r2.filter.FilterChain;
import com.linkedin.r2.filter.transport.FilterChainDispatcher;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import com.linkedin.restli.server.DelegatingTransportDispatcher;
import com.linkedin.restli.server.RestLiConfig;
import com.linkedin.restli.server.RestLiServer;
import com.linkedin.restli.server.resources.ResourceFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.api.http.HttpConfiguration;


@Singleton
public class DefaultHttpDispatcherProvider implements HttpDispatcherProvider {
  private HttpDispatcher httpDispatcher;

  @Inject
  public DefaultHttpDispatcherProvider(
      HttpConfiguration httpConfiguration,
      RestLiConfig restLiConfig,
      ResourceFactory resourceFactory,
      Engine engine,
      FilterChain filterChain
  ) {
    RestLiServer restLiServer = new RestLiServer(restLiConfig, resourceFactory, engine);
    httpDispatcher = new HttpDispatcher(
        new FilterChainDispatcher(
            new PlayContextDispatcher(
                httpConfiguration.context(),
                new DelegatingTransportDispatcher(restLiServer, restLiServer)
            ), filterChain));
  }

  @Override
  public HttpDispatcher get() {
    return httpDispatcher;
  }
}
