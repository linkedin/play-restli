import com.google.inject.AbstractModule;
import com.linkedin.restli.server.RestLiConfig;
import java.util.Collection;
import sbtrestli.BuildInfo;

import scala.collection.JavaConverters;


public class Module extends AbstractModule {
  @Override
  protected void configure() {
    RestLiConfig config = new RestLiConfig();
    Collection<String> resourcePackages = JavaConverters.asJavaCollection(BuildInfo.restliModelResourcePackages());
    config.addResourcePackageNames(resourcePackages);

    bind(RestLiConfig.class).toInstance(config);
  }
}
