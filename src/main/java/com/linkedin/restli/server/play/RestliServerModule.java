package com.linkedin.restli.server.play;

import com.linkedin.parseq.Engine;
import com.linkedin.r2.filter.FilterChain;
import com.linkedin.r2.filter.FilterChains;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import com.linkedin.restli.server.resources.ResourceFactory;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class RestliServerModule extends Module {
  @Override
  public Seq<Binding<?>> bindings(play.api.Environment environment, play.api.Configuration configuration) {
    return seq(bind(RestliServerApi.class).to(RestliServerComponent.class),
        bind(RestliServerStreamApi.class).to(RestliServerStreamComponent.class),
        bind(ResourceFactory.class).to(GuiceResourceFactory.class),
        bind(FilterChain.class).toInstance(FilterChains.empty()),
        bind(Engine.class).toProvider(EngineProvider.class),
        bind(HttpDispatcher.class).toProvider(HttpDispatcherProvider.class));
  }
}
