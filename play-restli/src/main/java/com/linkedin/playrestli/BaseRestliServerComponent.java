package com.linkedin.playrestli;

import com.linkedin.r2.filter.R2Constants;
import com.linkedin.r2.message.BaseRequestBuilder;
import com.linkedin.r2.message.Request;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import play.api.http.CookiesConfiguration;
import play.core.netty.utils.ClientCookieEncoder;
import play.mvc.Http;


/**
 * Created by qliu on 2/29/16.
 */
public abstract class BaseRestliServerComponent<T extends Request> {
  protected final HttpDispatcher _restliDispatcher;
  private final ClientCookieEncoder _cookieEncoder;

  protected BaseRestliServerComponent(HttpDispatcher restliDispatcher, CookiesConfiguration cookiesConfiguration) {
    _restliDispatcher = restliDispatcher;
    _cookieEncoder = cookiesConfiguration.clientEncoder();
  }

  protected <B extends BaseRequestBuilder<B>> B createRequestBuilder(
      Http.Request request, Function<URI, B> createBuilder) throws Exception {
    B builder = createBuilder.apply(new URI(request.uri()));
    builder.setMethod(request.method());

    for (Map.Entry<String, List<String>> header : request.getHeaders().toMap().entrySet()) {
      String key = header.getKey();

      if (key.equalsIgnoreCase(Http.HeaderNames.COOKIE)) {
        continue; // Cookie header and request.cookies may be out of sync; request.cookies is the source of truth.
      }

      for (String value : header.getValue()) {
        builder.addHeaderValue(key, value);
      }
    }

    for (Http.Cookie cookie : request.cookies()) {
      builder.addCookie(_cookieEncoder.encode(cookie.name(), cookie.value()));
    }

    return builder;
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
}
