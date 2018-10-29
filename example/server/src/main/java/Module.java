import com.google.inject.AbstractModule;
import com.linkedin.restli.server.RestLiConfig;

public class Module extends AbstractModule {
  @Override
  protected void configure() {
    RestLiConfig config = new RestLiConfig();
    config.addResourcePackageNames(
        "com.example.fortune"
    );

    bind(RestLiConfig.class).toInstance(config);
  }
}
