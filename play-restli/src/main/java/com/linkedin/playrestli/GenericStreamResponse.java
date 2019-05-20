package com.linkedin.playrestli;

import akka.stream.javadsl.Source;
import java.util.Collection;
import java.util.Map;
import play.api.http.HttpChunk;


/**
 * Created by qliu on 8/29/14.
 *
 * A plain POJO serves as an adapter between StreamResponses and Play SimpleResults.
 */
public class GenericStreamResponse extends BaseGenericResponse<Source<HttpChunk, ?>> {
  public GenericStreamResponse(int status, Map<String, String> headers) {
    super(status, headers);
  }

  public GenericStreamResponse(int status, Map<String, String> headers, Collection<String> cookies) {
    super(status, headers, cookies);
  }

  public GenericStreamResponse(int status, Map<String, String> headers, Source<HttpChunk, ?> body) {
    super(status, headers, body);
  }

  public GenericStreamResponse(int status, Map<String, String> headers, Collection<String> cookies, Source<HttpChunk, ?> body) {
    super(status, headers, cookies, body);
  }

  @Override
  protected Source<HttpChunk, ?> createEmptyBody() {
    return Source.empty();
  }
}
