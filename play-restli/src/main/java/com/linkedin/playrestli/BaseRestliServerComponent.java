package com.linkedin.playrestli;

import com.linkedin.r2.filter.R2Constants;
import com.linkedin.r2.message.BaseRequestBuilder;
import com.linkedin.r2.message.Request;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.message.rest.RestStatus;
import com.linkedin.r2.transport.http.common.HttpProtocolVersion;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import javax.ws.rs.core.UriBuilder;
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
  private static final Logger.ALogger LOGGER = Logger.of(BaseRestliServerComponent.class);
  protected final HttpDispatcher _restliDispatcher;
  private final ClientCookieEncoder _cookieEncoder;
  private final String _playContext;

  protected BaseRestliServerComponent(HttpConfiguration httpConfiguration, CookiesConfiguration cookiesConfiguration,
      HttpDispatcher restliDispatcher) {
    // Normalize the context path by removing the trailing slash
    _playContext = StringUtils.removeEnd(httpConfiguration.context(), "/");
    _cookieEncoder = cookiesConfiguration.clientEncoder();
    _restliDispatcher = restliDispatcher;
  }

  protected <B extends BaseRequestBuilder<B>> Optional<B> createRequestBuilder(Http.Request request,
      Function<URI, B> createBuilder) throws Exception {
    return stripUri(new URI(request.uri())).map(uri -> {
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
    final String remoteAddress = request.remoteAddress();
    final RequestContext requestContext = new RequestContext();
    if (remoteAddress != null) {
      requestContext.putLocalAttr(R2Constants.REMOTE_ADDR, remoteAddress);
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
    return requestContext;
  }

  protected static RestResponse notFound(String uri) {
    return RestStatus.responseForStatus(RestStatus.NOT_FOUND, "No resource for URI: " + uri);
  }

  /**
   * Strips scheme and authority parts from URI, also strips Play context from path part, to make URI relative.
   */
  private Optional<URI> stripUri(URI uri) {
    if (_playContext.isEmpty()) {
      return Optional.of(stripSchemeAuthority(UriBuilder.fromUri(uri)));
    }
    String path = uri.getRawPath();
    if (path != null && path.startsWith(_playContext) && (path.length() == _playContext.length()
        || path.charAt(_playContext.length()) == '/')) {
      return Optional.of(
          stripSchemeAuthority(UriBuilder.fromUri(uri).replacePath(path.substring(_playContext.length()))));
    } else {
      LOGGER.error("Play context is not leading the path part of the URI: " + uri);
      return Optional.empty();
    }
  }

  /**
   * Strips scheme and authority parts from URI.
   */
  private URI stripSchemeAuthority(UriBuilder uriBuilder) {
    return uriBuilder.scheme(null).userInfo(null).host(null).port(-1).build();
  }
}
