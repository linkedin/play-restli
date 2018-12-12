package com.linkedin.playrestli;

import play.mvc.Http;


/**
 * Created by rli on 2/1/16.
 *
 * The interface between the Play world and the rest.li world.
 */
public interface RestliServerApi {

  void handleRequest(Http.Request request, RestliTransportCallback callback) throws Exception;
}
