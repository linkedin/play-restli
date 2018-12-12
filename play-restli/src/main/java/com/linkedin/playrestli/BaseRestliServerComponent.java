package com.linkedin.playrestli;

import com.linkedin.r2.filter.R2Constants;
import com.linkedin.r2.message.BaseRequestBuilder;
import com.linkedin.r2.message.Request;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.transport.http.server.HttpDispatcher;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Http;


/**
 * Created by qliu on 2/29/16.
 */
public abstract class BaseRestliServerComponent<T extends Request> {
  protected final HttpDispatcher _restliDispatcher;

  protected BaseRestliServerComponent(HttpDispatcher restliDispatcher) {
    _restliDispatcher = restliDispatcher;
  }

  /**
   * Transform the request headers to a Map by ignoring multiple values.
   */
  protected Map<String, String> toSimpleMap(Map<String, List<String>> map) {
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      String commaSeparatedValues = StringUtils.join(entry.getValue(), ",");
      result.put(entry.getKey(), commaSeparatedValues);
    }
    return result;
  }

  protected <B extends BaseRequestBuilder<B>> B createRequestBuilder(
      Http.Request request, Function<URI, B> createBuilder) throws Exception {


    B builder = createBuilder.apply(new URI(request.uri()));
    Map<String, List<String>> headers = request.getHeaders().toMap();

    builder.setMethod(request.method());
    Map<Boolean, List<Map.Entry<String, List<String>>>> cookiesVsHeaders = headers.entrySet().stream()
        .collect(Collectors
            .partitioningBy(entry -> Http.HeaderNames.COOKIE.toLowerCase().equals(entry.getKey().toLowerCase())));
    builder.setCookies(cookiesVsHeaders.get(true).stream()
        .map(Map.Entry::getValue)
        .flatMap(List::stream)
        .collect(Collectors.toList()));
    builder.setHeaders(toSimpleMap(cookiesVsHeaders.get(false).stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
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
