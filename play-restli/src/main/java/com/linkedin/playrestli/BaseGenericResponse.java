package com.linkedin.playrestli;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


/**
 * Created by qliu on 3/1/16.
 */
public abstract class BaseGenericResponse<T> {

  private final int                 _status;
  private final Map<String, String> _headers;
  private final Collection<String> _cookies;
  private final T                   _body;

  protected BaseGenericResponse(int status, Map<String, String> headers) {
    this(status, headers, null, null);
  }

  protected BaseGenericResponse(int status, Map<String, String> headers, Collection<String> cookies) {
    this(status, headers, cookies, null);
  }

  protected BaseGenericResponse(int status, Map<String, String> headers, T body) {
    this(status, headers, null, body);
  }

  protected BaseGenericResponse(int status, Map<String, String> headers, Collection<String> cookies, T body) {
    _status = status;
    _headers = headers == null ? Collections.emptyMap() : headers;
    _cookies = cookies == null ? Collections.emptyList() : cookies;
    _body = body == null ? createEmptyBody() : body;
  }

  public int getStatus() {
    return _status;
  }

  public Map<String, String> getHeaders() {
    return _headers;
  }

  public Collection<String> getCookies() {
    return _cookies;
  }

  public T getBody() {
    return _body;
  }

  protected abstract T createEmptyBody();
}
