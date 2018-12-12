package com.linkedin.playrestli;

import play.mvc.Http;


/**
 * Created by qliu on 2/1/16.
 *
 * The streaming interface between the Play world and the rest.li world.
 */
public interface RestliServerStreamApi {

  void handleRequest(Http.Request request, RestliStreamTransportCallback callback) throws Exception;
}
