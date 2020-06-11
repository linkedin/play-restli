package com.linkedin.playrestli;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.http.HttpConfiguration;


/**
 * A default implementation to prepare the restli request uri
 */
@Singleton
public class DefaultRestliUriResolver implements RestliUriResolver {
  private static final Logger.ALogger LOGGER = Logger.of(DefaultRestliUriResolver.class);
  private final String _playContext;

  @Inject
  public DefaultRestliUriResolver(HttpConfiguration httpConfiguration) {
    _playContext = StringUtils.removeEnd(httpConfiguration.context(), "/");
  }

  @Override
  public Optional<URI> getRestliUri(String uri, String path) throws URISyntaxException {
    if (!uri.contains(path)) {
      LOGGER.error(String.format("URI (%s) and path (%s) mismatch", uri, path));
      return Optional.empty();
    }
    String cleanUri = stripSchemeAuthority(uri, path);
    if (_playContext.isEmpty()) {
      return Optional.of(new URI(cleanUri));
    }
    if (path.startsWith(_playContext) && (path.length() == _playContext.length()
        || path.charAt(_playContext.length()) == '/')) {
      return Optional.of(new URI(cleanUri.substring(_playContext.length())));
    } else {
      LOGGER.error("Play context is not leading the path part of the URI: " + uri);
      return Optional.empty();
    }
  }

  /**
   * Strips scheme and authority parts from URI.
   */
  private String stripSchemeAuthority(String uri, String path) {
    int startIndex;
    if (!path.isEmpty()) {
      // If path exists, then starts with path
      startIndex = uri.indexOf(path);
    } else{
      // If query exists, then starts with query
      startIndex = uri.indexOf('?');
      if (startIndex == -1) {
        // If fragment exists, then starts with fragment
        startIndex = uri.indexOf('#');
        if (startIndex == -1) {
          // If none exists, then return empty
          return "";
        }
      }
    }
    return uri.substring(startIndex);
  }
}
