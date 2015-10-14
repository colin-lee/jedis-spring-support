package com.github.trace;

import com.github.autoconf.helper.ConfigHelper;
import com.github.trace.listener.LogDisk;
import com.github.trace.listener.OssStat;
import com.github.trace.listener.OssTrace;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * trace记录
 * Created by lirui on 2015-10-14 10:10.
 */
public class TraceRecorder implements InitializingBean {
  private EventBus eventBus;
  private boolean async;
  private String clientName;
  private String clientIp;

  public EventBus getEventBus() {
    return eventBus;
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public void post(TraceContext c) {
    c.setClientIp(clientIp);
    c.setClientName(clientName);
    eventBus.post(c);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    clientIp = ConfigHelper.getServerInnerIP();
    clientName = ConfigHelper.getApplicationConfig().get("process.name", "unknown");

    if (eventBus == null) {
      if (async) {
        ExecutorService executor = Executors.newFixedThreadPool(3, new NamedThreadFactory("trace", true));
        eventBus = new AsyncEventBus("asyncTraceEventBus", executor);
      } else {
        eventBus = new EventBus("traceEventBus");
      }
    }

    //记录到日志
    eventBus.register(new LogDisk());
    //每分钟统计
    eventBus.register(new OssStat());
    //trace跟踪
    eventBus.register(new OssTrace());
  }
}
