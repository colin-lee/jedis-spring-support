package com.github.trace.listener;

import com.github.trace.TraceContext;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录到磁盘
 * Created by lirui on 2015-10-14 10:35.
 */
public class LogDisk {
  private static final Logger LOG = LoggerFactory.getLogger(LogDisk.class);

  @Subscribe
  public void save(TraceContext c) {
    LOG.info("{}", c);
  }
}
