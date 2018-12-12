package com.linkedin.playrestli;

import java.util.List;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;


public interface WithFrontendFilters extends HttpFilters {

  /**
   * @return the list of filters that should filter frontend (non-restli) requests.
   */
  List<EssentialFilter> getFrontendFilters();
}
