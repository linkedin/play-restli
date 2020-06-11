package com.linkedin.playrestli;

import com.linkedin.parseq.Engine;
import com.linkedin.r2.filter.FilterChain;
import com.linkedin.r2.filter.FilterChains;
import com.linkedin.r2.filter.message.rest.RestFilter;
import com.linkedin.r2.filter.message.stream.StreamFilter;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import com.linkedin.restli.server.RestLiConfig;
import com.linkedin.restli.server.resources.ResourceFactory;
import com.typesafe.config.Config;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Provider;
import play.Environment;
import play.inject.Binding;
import play.inject.Module;


public class RestliServerModule extends Module {
  private Config _restliConfig;
  private ClassLoader _classLoader;

  private <T> Class<? extends T> loadClass(Class<T> base, String subclass) {
    try {
      return _classLoader.loadClass(subclass).asSubclass(base);
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
    return _restliConfig.getStringList(path).stream()
        .map(klass -> newInstance(base, klass))
        .collect(Collectors.toList());
  }

  private <T> Binding<T> dynBinding(Class<T> from, String to) {
    return bindClass(from).to(loadClass(from, _restliConfig.getString(to)));
  }

  private <T> Binding<T> dynProvider(Class<T> from, Class<? extends Provider<T>> provider, String to) {
    return bindClass(from).toProvider(loadClass(provider, _restliConfig.getString(to)));
  }

  @Override
  public List<Binding<?>> bindings(Environment environment, Config config) {
    _restliConfig = config.getConfig("restli");
    _classLoader = environment.classLoader();

    List<RestFilter> restFilters = collectInstances(RestFilter.class, "rest.filters");
    List<StreamFilter> streamFilters = collectInstances(StreamFilter.class, "stream.filters");
    FilterChain filterChain = FilterChains.create(restFilters, streamFilters);

    return Arrays.asList(
        dynBinding(RestliServerApi.class, "rest.server"),
        dynBinding(RestliServerStreamApi.class, "stream.server"),
        bindClass(FilterChain.class).toInstance(filterChain),
        dynBinding(ResourceFactory.class, "resourceFactory"),
        dynProvider(Engine.class, EngineProvider.class, "engineProvider"),
        dynProvider(RestLiConfig.class, RestliConfigProvider.class, "configProvider"),
        dynProvider(HttpDispatcher.class, HttpDispatcherProvider.class, "httpDispatcherProvider"),
        bindClass(RestliUriResolver.class).to(DefaultRestliUriResolver.class)
    );
  }
}
