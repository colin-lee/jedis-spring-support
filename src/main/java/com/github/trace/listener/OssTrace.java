package com.github.trace.listener;

import com.github.trace.TraceContext;
import com.google.common.eventbus.Subscribe;

/**
 * trace跟踪
 * Created by lirui on 2015-10-14 10:44.
 */
public class OssTrace {
  @Subscribe
  public void saveElasticSearch(TraceContext c) {

  }
}
