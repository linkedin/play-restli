package com.linkedin.playrestli;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import java.util.Collection;
import java.util.Map;


/**
 * Created by qliu on 8/29/14.
 *
 * A plain POJO serves as an adapter between StreamResponses and Play SimpleResults.
 */
public class GenericStreamResponse extends BaseGenericResponse<Source<ByteString, ?>> {
  public GenericStreamResponse(int status, Map<String, String> headers) {
    super(status, headers);
  }

  public GenericStreamResponse(int status, Map<String, String> headers, Collection<String> cookies) {
    super(status, headers, cookies);
  }

  public GenericStreamResponse(int status, Map<String, String> headers, Source<ByteString, ?> body) {
    super(status, headers, body);
  }

  public GenericStreamResponse(int status, Map<String, String> headers, Collection<String> cookies, Source<ByteString, ?> body) {
    super(status, headers, cookies, body);
  }

  @Override
  protected Source<ByteString, ?> createEmptyBody() {
    return Source.empty();
  }
}
