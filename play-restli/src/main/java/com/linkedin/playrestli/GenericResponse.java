package com.linkedin.playrestli;

import java.util.Collection;
import java.util.Map;


/**
 * Created by rli on 8/29/14.
 *
 * A plain POJO serves as an adapter between RestResponses and Play SimpleResults.
 */
public class GenericResponse extends BaseGenericResponse<byte[]> {
  public GenericResponse(int status, Map<String, String> headers) {
    super(status, headers);
  }

  public GenericResponse(int status, Map<String, String> headers, Collection<String> cookies) {
    super(status, headers, cookies);
  }

  public GenericResponse(int status, Map<String, String> headers, byte[] body) {
    super(status, headers, body);
  }

  public GenericResponse(int status, Map<String, String> headers, Collection<String> cookies, byte[] body) {
    super(status, headers, cookies, body);
  }

  @Override
  protected byte[] createEmptyBody() {
    return new byte[0];
  }
}
