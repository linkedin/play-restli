package com.linkedin.restli.server.play;

import com.linkedin.restli.server.RestLiConfig;
import com.typesafe.config.Config;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;


@Singleton
public class RestliConfigProvider implements Provider<RestLiConfig> {
  private RestLiConfig restliConfig;

  @Inject
  RestliConfigProvider(Config playConfig) {
    restliConfig = new RestLiConfig();

    List<String> resourcePackages = playConfig.getStringList("play.restli.resourcePackages");

    restliConfig.addResourcePackageNames(resourcePackages);
  }

  @Override
  public RestLiConfig get() {
    return restliConfig;
  }
}
