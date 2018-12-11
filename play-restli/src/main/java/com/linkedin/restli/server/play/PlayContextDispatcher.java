package com.linkedin.restli.server.play;

import com.linkedin.jersey.api.uri.UriBuilder;
import com.linkedin.r2.message.Messages;
import com.linkedin.r2.message.Request;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


public class PlayContextDispatcher implements TransportDispatcher {
  private String _context;
  private TransportDispatcher _transportDispatcher;

  public PlayContextDispatcher(String context, TransportDispatcher transportDispatcher) {
    _context = context.endsWith("/") ? context.substring(0, context.length() - 1) : context;
    _transportDispatcher = transportDispatcher;
  }

  private RestResponse notFound(URI uri) {
    return RestStatus.responseForStatus(RestStatus.NOT_FOUND, "No resource for URI: " + uri);
  }

  private <T extends Request> Optional<T> stripContext(T request, Function<URI, T> builder) {
    if (_context.isEmpty()) {
      return Optional.of(request);
    }

    URI uri = request.getURI();
    String path = uri.getRawPath();
    if (path != null
        && path.startsWith(_context)
        && (path.length() == _context.length() || path.charAt(_context.length()) == '/')) {
      String newPath = path.substring(_context.length());
      URI newUri = UriBuilder.fromUri(uri).replacePath(newPath).build();

      return Optional.of(builder.apply(newUri));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void handleRestRequest(RestRequest req, Map<String, String> wireAttrs, RequestContext requestContext,
      TransportCallback<RestResponse> callback) {
    Optional<RestRequest> newRequest = stripContext(req, uri -> req.builder().setURI(uri).build());

    if (newRequest.isPresent()) {
      _transportDispatcher.handleRestRequest(newRequest.get(), wireAttrs, requestContext, callback);
    } else {
      callback.onResponse(TransportResponseImpl.success(notFound(req.getURI())));
    }
  }

  @Override
  public void handleStreamRequest(StreamRequest req, Map<String, String> wireAttrs, RequestContext requestContext,
      TransportCallback<StreamResponse> callback) {
    Optional<StreamRequest> newRequest = stripContext(req, uri -> req.builder().setURI(uri).build(req.getEntityStream()));

    if (newRequest.isPresent()) {
      _transportDispatcher.handleStreamRequest(newRequest.get(), wireAttrs, requestContext, callback);
    } else {
      callback.onResponse(TransportResponseImpl.success(Messages.toStreamResponse(notFound(req.getURI()))));
      req.getEntityStream().setReader(new DrainReader());
    }
  }
}
