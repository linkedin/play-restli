package com.linkedin.playrestli;

import com.linkedin.r2.filter.R2Constants;
import com.linkedin.r2.message.BaseRequestBuilder;
import com.linkedin.r2.message.Request;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.message.rest.RestStatus;
import com.linkedin.r2.transport.http.common.HttpProtocolVersion;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import java.lang.NullPointerException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.http.CookiesConfiguration;
import play.api.http.HttpConfiguration;
import play.core.netty.utils.ClientCookieEncoder;
import play.mvc.Http;


/**
 * Created by qliu on 2/29/16.
 */
public abstract class BaseRestliServerComponent<T extends Request> {
  public static final String PLAY_REQUEST_ID_KEY = "PLAY_REQUEST_ID";
  private static final Logger.ALogger LOGGER = Logger.of(BaseRestliServerComponent.class);
  protected final HttpDispatcher _restliDispatcher;
  private final ClientCookieEncoder _cookieEncoder;
  private final RestliUriResolver _restliUriResolver;

  protected BaseRestliServerComponent(CookiesConfiguration cookiesConfiguration,
      HttpDispatcher restliDispatcher, RestliUriResolver restliUriResolver) {
    _cookieEncoder = cookiesConfiguration.clientEncoder();
    _restliDispatcher = restliDispatcher;
    _restliUriResolver = restliUriResolver;
  }

  protected <B extends BaseRequestBuilder<B>> Optional<B> createRequestBuilder(Http.Request request,
      Function<URI, B> createBuilder) throws Exception {
    return _restliUriResolver.getRestliUri(request.uri(), request.path()).map(uri -> {
      B builder = createBuilder.apply(uri);
      builder.setMethod(request.method());

      request.getHeaders()
          .toMap()
          .entrySet()
          .stream()
          // Cookie header and request.cookies may be out of sync; request.cookies is the source of truth.
          .filter(entry -> !entry.getKey().equalsIgnoreCase(Http.HeaderNames.COOKIE))
          .forEach(entry -> entry.getValue().forEach(value -> builder.addHeaderValue(entry.getKey(), value)));

      request.cookies().forEach(cookie -> builder.addCookie(_cookieEncoder.encode(cookie.name(), cookie.value())));

      return builder;
    });
  }

  /**
   * Create request context by the given http request and save the remote address in it.
   * The remote address saved here is the internal address not client IP.
   * For more information, refer {@link R2Constants#REMOTE_ADDR}
   **/
  protected static RequestContext createRequestContext(final Http.Request request) {
    final RequestContext requestContext = new RequestContext();
    try {
      final String remoteAddress = request.remoteAddress();

      if (remoteAddress != null) {
        requestContext.putLocalAttr(R2Constants.REMOTE_ADDR, remoteAddress);
      }
    } catch (NullPointerException ex) {
      // TODO - remove this protection once Play Netty implementation can guard against the NPE
      LOGGER.warn("Caught NPE From play-netty-server when accessing remote address in a Netty Channel");
    }

    requestContext.putLocalAttr(R2Constants.HTTP_PROTOCOL_VERSION, HttpProtocolVersion.parse(request.version()));
    if (request.secure()) {
      request.clientCertificateChain().ifPresent(chain -> {
        if (!chain.isEmpty()) {
          requestContext.putLocalAttr(R2Constants.CLIENT_CERT, chain.get(0));
        }
      });
      requestContext.putLocalAttr(R2Constants.IS_SECURE, true);
    } else {
      requestContext.putLocalAttr(R2Constants.IS_SECURE, false);
    }
    requestContext.putLocalAttr(PLAY_REQUEST_ID_KEY, request.asScala().id());
    return requestContext;
  }

  protected static RestResponse notFound(String uri) {
    return RestStatus.responseForStatus(RestStatus.NOT_FOUND, "No resource for URI: " + uri);
  }
}
