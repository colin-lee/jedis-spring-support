package com.github.trace.listener;

import com.github.trace.TraceContext;
import com.google.common.eventbus.Subscribe;

/**
 * 每分钟统计
 * Created by lirui on 2015-10-14 10:43.
 */
public class OssStat {
  @Subscribe
  public void stat(TraceContext c) {

  }
}
