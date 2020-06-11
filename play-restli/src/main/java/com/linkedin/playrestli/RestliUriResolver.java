package com.linkedin.playrestli;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;


/**
 * Necessary process on Play's  request uri to form the uri to be used with restli request
 */
public interface RestliUriResolver {
  Optional<URI> getRestliUri(String uri, String path) throws URISyntaxException;
}
