package com.linkedin.playrestli.mock;

import com.google.common.collect.ImmutableList;
import com.linkedin.data.ByteString;
import com.linkedin.r2.message.stream.StreamResponse;
import com.linkedin.r2.message.stream.StreamResponseBuilder;
import com.linkedin.r2.message.stream.entitystream.EntityStream;
import com.linkedin.r2.message.stream.entitystream.EntityStreams;
import com.linkedin.r2.message.stream.entitystream.WriteHandle;
import com.linkedin.r2.message.stream.entitystream.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by qliu on 3/1/16.
 */
public class MockStreamResponse implements StreamResponse {
  private int                 _status;
  private ByteString          _entity = ByteString.copy(new byte[0]);
  private EntityStream        _entityStream = EntityStreams.emptyStream();
  private Map<String, String> _headers = new HashMap<String, String>();

  public MockStreamResponse withStatus(int status) {
    _status = status;
    return this;
  }

  public MockStreamResponse withEntity(String entity) {
    if (entity != null) {
      _entity = ByteString.copyString(entity, "UTF-8");
      _entityStream = EntityStreams.newEntityStream(new Writer() {
        private WriteHandle _writeHandle;

        @Override
        public void onInit(WriteHandle wh) {
          _writeHandle = wh;
        }

        @Override
        public void onWritePossible() {
          _writeHandle.write(_entity);
          _writeHandle.done();
        }

        @Override
        public void onAbort(Throwable e) {

        }
      });
    }
    return this;
  }

  public MockStreamResponse withHeaders(Map<String, String> headers) {
    if (headers != null) {
      _headers = headers;
    }
    return this;
  }

  public ByteString getEntity() {
    return _entity;
  }

  @Override
  public int getStatus() {
    return _status;
  }

  @Override
  public EntityStream getEntityStream() {
    return _entityStream;
  }

  @Override
  public StreamResponseBuilder builder() {
    StreamResponseBuilder builder = new StreamResponseBuilder();
    builder.setStatus(_status);
    builder.setHeaders(_headers);
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
