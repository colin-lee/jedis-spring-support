package com.github.trace.listener;

import com.github.trace.TraceContext;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录到磁盘
 * Created by lirui on 2015-10-14 10:35.
 */
public class RpcLog {
  private static final Logger LOG = LoggerFactory.getLogger(RpcLog.class);
  private long threshold = 100;

  public long getThreshold() {
    return threshold;
  }

  public void setThreshold(long threshold) {
    this.threshold = threshold;
  }

  @Subscribe
  public void save(TraceContext c) {
    if (c.isFail()) {
      LOG.error("{}", c);
    } else if (c.getCost() > threshold) {
      LOG.warn("{}", c);
    } else {
      LOG.info("{}", c);
    }
  }
}
