package com.linkedin.playrestli.mock;

import com.google.common.collect.ImmutableList;
import com.linkedin.data.ByteString;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.message.rest.RestResponseBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rli on 8/28/14.
 *
 */
public class MockRestResponse implements RestResponse {

  private int                 _status;
  private ByteString          _entity = ByteString.copy(new byte[0]);
  private Map<String, String> _headers = new HashMap<String, String>();

  public MockRestResponse withStatus(int status) {
    _status = status;
    return this;
  }

  public MockRestResponse withEntity(String entity) {
    if (entity != null) {
      _entity = ByteString.copyString(entity, "UTF-8");
    }
    return this;
  }

  public MockRestResponse withHeaders(Map<String, String> headers) {
    if (headers != null) {
      _headers = headers;
    }
    return this;
  }

  @Override
  public int getStatus() {
    return _status;
  }

  @Override
  public ByteString getEntity() {
    return _entity;
  }

  @Override
  public RestResponseBuilder builder() {
    RestResponseBuilder builder = new RestResponseBuilder();
    builder.setStatus(_status);
    builder.setHeaders(_headers);
    if (_entity != null) {
      builder.setEntity(_entity);
    }
    return builder;
  }

  @Override
  public String getHeader(String name) {
    return _headers.get(name);
  }

  @Override
  public List<String> getHeaderValues(String name) {
    return ImmutableList.of(_headers.get(name));
  }

  @Override
  public Map<String, String> getHeaders() {
    return _headers;
  }

  @Override
  public List<String> getCookies() {
    return Collections.emptyList();
  }
}
