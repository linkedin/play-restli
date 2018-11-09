package com.linkedin.restli.server.play;

import com.linkedin.restli.internal.server.model.ResourceModel;
import com.linkedin.restli.server.resources.ResourceFactory;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.inject.Injector;


@Singleton
public class PlayResourceFactory implements ResourceFactory {
  private final Injector _injector;

  @Inject
  PlayResourceFactory(Injector playInjector) {
    _injector = playInjector;
  }

  @Override
  public void setRootResources(Map<String, ResourceModel> rootResources) { }

  @Override
  public <R> R create(Class<R> resourceClass)
  {
    return _injector.instanceOf(resourceClass);
  }
}

