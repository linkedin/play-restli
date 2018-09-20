import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.linkedin.restli.server.RestLiConfig;

public class Module extends AbstractModule {
  @Provides
  RestLiConfig restLiConfigProvider() {
    RestLiConfig config = new RestLiConfig();
    config.addResourcePackageNames(
        "com.example.fortune"
    );
    return config;
  }
}
