package com.linkedin.restli.server.play;

import com.linkedin.parseq.Engine;
import com.linkedin.r2.filter.FilterChain;
import com.linkedin.r2.filter.FilterChains;
import com.linkedin.r2.filter.message.rest.RestFilter;
import com.linkedin.r2.filter.message.stream.StreamFilter;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import com.linkedin.restli.server.RestLiConfig;
import com.linkedin.restli.server.resources.ResourceFactory;
import com.typesafe.config.Config;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Provider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class RestliServerModule extends Module {
  private Config config;
  private ClassLoader classLoader;

  private <T> Class<? extends T> loadClass(Class<T> base, String subclass) {
    try {
      return classLoader.loadClass(subclass).asSubclass(base);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  private <T> T newInstance(Class<T> base, String subclass) {
    try {
      return loadClass(base, subclass).newInstance();
    } catch (ReflectiveOperationException ex) {
      throw new RuntimeException(ex);
    }
  }

  private <T> List<T> collectInstances(Class<T> base, String path) {
    return config.getStringList(path).stream()
        .map(klass -> newInstance(base, klass))
        .collect(Collectors.toList());
  }

  private <T> Binding<T> dynBinding(Class<T> from, String to) {
    return bind(from).to(loadClass(from, config.getString(to)));
  }

  private <T> Binding<T> dynProvider(Class<T> from, Class<? extends Provider<T>> provider, String to) {
    return bind(from).toProvider(loadClass(provider, config.getString(to)));
  }

  @Override
  public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
    config = configuration.underlying().getConfig("restli");
    classLoader = environment.classLoader();

    List<RestFilter> restFilters = collectInstances(RestFilter.class, "rest.filters");
    List<StreamFilter> streamFilters = collectInstances(StreamFilter.class, "stream.filters");
    FilterChain filterChain = FilterChains.create(restFilters, streamFilters);

    return seq(
        dynBinding(RestliServerApi.class, "rest.server"),
        dynBinding(RestliServerStreamApi.class, "stream.server"),
        bind(FilterChain.class).toInstance(filterChain),
        dynBinding(ResourceFactory.class, "resourceFactory"),
        dynProvider(Engine.class, EngineProvider.class, "engineProvider"),
        dynProvider(RestLiConfig.class, RestliConfigProvider.class, "configProvider"),
        dynProvider(HttpDispatcher.class, HttpDispatcherProvider.class, "httpDispatcherProvider")
    );
  }
}
