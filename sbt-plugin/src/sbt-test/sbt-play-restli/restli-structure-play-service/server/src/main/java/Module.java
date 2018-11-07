import com.google.inject.AbstractModule;
import com.linkedin.restli.server.RestLiConfig;
import java.util.Collection;

public class Module extends AbstractModule {
  @Override
  protected void configure() {
    RestLiConfig config = new RestLiConfig();

    config.addResourcePackageNames(PlayRestli.resourcePackages);

    bind(RestLiConfig.class).toInstance(config);
  }
}
