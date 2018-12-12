# Play :heart: ️Rest.li

Use the [Play Framework](https://playframework.com) as the backend for your [rest.li](https://rest.li) service with play-restli!

Setup
-----
Add play-restli to your plugin dependencies along with play and sbt-restli:
```scala
// project/plugins.sbt
addSbtPlugin("com.linkedin.play-restli" % "sbt-play-restli" % "<version>")
addSbtPlugin("com.linkedin.sbt-restli" % "sbt-restli" % "<version>")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "<version>")
```

Usage
-----
> See [sbt-restli](https://github.com/TylerHorth/sbt-restli) for detailed instructions on setting up a rest.li project using sbt.

Apply your preferred play plugin (PlayService, PlayJava, or PlayScala) to your server project alongside the RestliModelPlugin.

PlayService is a minimal play plugin delivered with play 2.6.8+, and is the recommended plugin for use with play-restli. If you choose to use one of other play plugins, we recommend disabling PlayLayoutPlugin in order to use the standard Maven project structure. 

Finally, create the play configuration file (`src/main/resources/application.conf` if using the default Maven structure (recommended), *or* `conf/application.conf` if using play structure) and specify the list of packages which contain rest.li resources.
```properties
restli.resourcePackages = ["com.example.fortune.impl"]
```

### Advanced Configuration

Play-restli allows you to fully configure rest.li through your play `application.conf`. All values shown are defaults, only `restli.resourcePackages` is required. 

```properties
restli {
  # List of packages containing rest.li resources.
  resourcePackages = []
  
  # Maximum request body size (non-streaming).
  memoryThresholdBytes = 1G
  
  # Stream rest.li request/response bodies.
  useStream = false

  # Apply filters outside the play context path (play.http.context).
  applyFiltersGlobally = false

  rest {
    # List of RestFilters to apply to each rest.li request and response.
    filters = []
    
    # RestliServerApi implementation. Converts play requests to rest.li requests.
    server = "com.linkedin.restli.server.play.RestliServerComponent"
  }
  
  stream {
    # List of StreamFilters to apply to each streaming rest.li request and response.
    filters = []
    
    # RestliServerStreamApi implementation. Converts play requests to rest.li requests.
    server = "com.linkedin.restli.server.play.RestliServerStreamComponent"
  }
  
  # ResourceFactory implementation. Factory which instantiates rest.li resources. 
  # Default resource factory delegates to the play injector.
  resourceFactory = "com.linkedin.restli.server.play.PlayResourceFactory"
  
  # RestliConfigProvider implementation. Creates the rest.li configuration. 
  # Default provider adds resource packages from the "restli.resourcePackages" setting. 
  configProvider = "com.linkedin.restli.server.play.DefaultRestliConfigProvider"
  
  # EngineProvider implementation. Creates the ParSeq engine.
  # Default provider creates an engine with numCores + 1 execution threads, and a 
  # single thread for timer scheduling.
  engineProvider = "com.linkedin.restli.server.play.DefaultEngineProvider"

  # HttpDispatcherProvider implementation. Creates the rest.li HttpDispatcher.
  # Default provider uses the config, resource factory, engine, and filters defined above.
  httpDispatcherProvider = "com.linkedin.restli.server.play.DefaultHttpDispatcherProvider"
}
```

Examples
--------
> **Note**: If you copy-paste an example, make sure to specify the play-restli version in `project/plugins.sbt`.

Complete examples are located in the [plugin sbt-test directory](sbt-play-restli/src/sbt-test/sbt-play-restli). 

- [Restli-structure-play-service](sbt-play-restli/src/sbt-test/sbt-play-restli/restli-structure-play-service) uses the recommended approach, leveraging the recently introduced PlayService plugin.
- [Restli-structure-play-java](sbt-play-restli/src/sbt-test/sbt-play-restli/restli-structure-play-java) uses the PlayJava plugin, but disables PlayLayoutPlugin in order to use the standard rest.li project structure. If using scala, PlayScala can be used instead.
- [Play-structure](sbt-play-restli/src/sbt-test/sbt-play-restli/play-structure) uses the PlayJava plugin, but does not disable PlayLayoutPlugin, thus using the default play project structure. This is unidiomatic for a rest.li service, but is nevertheless supported. 
