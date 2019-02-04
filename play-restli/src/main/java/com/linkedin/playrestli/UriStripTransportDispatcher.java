package com.linkedin.playrestli;

import com.linkedin.r2.message.Messages;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.rest.RestRequest;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.message.rest.RestStatus;
import com.linkedin.r2.message.stream.StreamRequest;
import com.linkedin.r2.message.stream.StreamResponse;
import com.linkedin.r2.message.stream.entitystream.DrainReader;
import com.linkedin.r2.transport.common.bridge.common.TransportCallback;
import com.linkedin.r2.transport.common.bridge.common.TransportResponseImpl;
import com.linkedin.r2.transport.common.bridge.server.TransportDispatcher;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import play.Logger;


/**
 * A TransportDispatcher, wrapping around the delegate, strips scheme and authority parts from URI to make it relative,
 * then strips Play's HTTP context from URI path to form new URI.
 */
public class UriStripTransportDispatcher implements TransportDispatcher {
  private static final Logger.ALogger LOGGER = Logger.of(UriStripTransportDispatcher.class);
  private String _playContext;
  private TransportDispatcher _transportDispatcher;

  public UriStripTransportDispatcher(String playContext, TransportDispatcher transportDispatcher) {
    // Normalize the context path by removing the trailing slash
    _playContext = playContext.endsWith("/") ? playContext.substring(0, playContext.length() - 1) : playContext;
    _transportDispatcher = transportDispatcher;
  }

  private RestResponse notFound(URI uri) {
    return RestStatus.responseForStatus(RestStatus.NOT_FOUND, "No resource for URI: " + uri);
  }

  private Optional<URI> stripUri(URI uri) {
    try {
      if (_playContext.isEmpty()) {
        return Optional.of(new URI(null, null, uri.getRawPath(), uri.getRawQuery(), uri.getRawFragment()));
      }
      String path = uri.getRawPath();
      if (path != null && path.startsWith(_playContext) && (path.length() == _playContext.length()
          || path.charAt(_playContext.length()) == '/')) {
        return Optional.of(
            new URI(null, null, path.substring(_playContext.length()), uri.getRawQuery(), uri.getRawFragment()));
      } else {
        return Optional.empty();
      }
    } catch (URISyntaxException e) {
      LOGGER.error("Failed to construct URI.", e);
      return Optional.empty();
    }
  }

  @Override
  public void handleRestRequest(RestRequest req, Map<String, String> wireAttrs, RequestContext requestContext,
      TransportCallback<RestResponse> callback) {
    Optional<URI> newUri = stripUri(req.getURI());
    if (newUri.isPresent()) {
      _transportDispatcher.handleRestRequest(req.builder().setURI(newUri.get()).build(), wireAttrs, requestContext,
          callback);
    } else {
      callback.onResponse(TransportResponseImpl.success(notFound(req.getURI())));
    }
  }

  @Override
  public void handleStreamRequest(StreamRequest req, Map<String, String> wireAttrs, RequestContext requestContext,
      TransportCallback<StreamResponse> callback) {
    Optional<URI> newUri = stripUri(req.getURI());
    if (newUri.isPresent()) {
      _transportDispatcher.handleStreamRequest(req.builder().setURI(newUri.get()).build(req.getEntityStream()),
          wireAttrs, requestContext, callback);
    } else {
      callback.onResponse(TransportResponseImpl.success(Messages.toStreamResponse(notFound(req.getURI()))));
      req.getEntityStream().setReader(new DrainReader());
    }
  }
}
