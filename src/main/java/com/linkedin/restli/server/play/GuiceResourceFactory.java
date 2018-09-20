package com.linkedin.restli.server.play;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.linkedin.restli.internal.server.model.ResourceModel;
import com.linkedin.restli.server.resources.ResourceFactory;
import java.util.Map;

@Singleton
public class GuiceResourceFactory implements ResourceFactory {
  private final Injector _injector;

  @Inject
  GuiceResourceFactory(Injector guiceInjector) {
    _injector = guiceInjector;
  }

  @Override
  public void setRootResources(Map<String, ResourceModel> rootResources) { }

  @Override
  public <R> R create(Class<R> resourceClass)
  {
    return _injector.getInstance(resourceClass);
  }
}

