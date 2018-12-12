package com.linkedin.playrestli;

import com.linkedin.r2.message.rest.RestResponse;
import java.util.List;
import java.util.Map;


/**
 * Created by rli on 10/14/14.
 *
 * Restli callback. Wraps a promise and redeems it both on error and success so the calling end
 * can act on Restli errors.
 *
 */
public class RestliTransportCallback extends BaseRestliTransportCallback<RestResponse, GenericResponse, byte[]> {
  @Override
  protected GenericResponse createErrorResponse(int status, Map<String, String> headers) {
    return new GenericResponse(status, headers);
  }

  @Override
  protected GenericResponse createResponse(int status, Map<String, String> headers, List<String> cookies,
      RestResponse response) {
    return new GenericResponse(status, headers, cookies, response.getEntity().copyBytes());
  }
}