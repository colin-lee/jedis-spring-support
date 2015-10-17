package com.github.trace;

import com.github.autoconf.helper.ConfigHelper;
import com.github.trace.listener.OssStat;
import com.github.trace.listener.OssTrace;
import com.github.trace.listener.RpcLog;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * trace记录
 * Created by lirui on 2015-10-14 10:10.
 */
public class TraceRecorder {
  private static final TraceRecorder INSTANCE = new TraceRecorder();
  private ExecutorService executor;
  private EventBus eventBus;
  private String clientName;
  private String clientIp;

  public static TraceRecorder getInstance() {
    return INSTANCE;
  }

  private TraceRecorder() {
    clientIp = ConfigHelper.getServerInnerIP();
    clientName = ConfigHelper.getApplicationConfig().get("process.name", "unknown");

    executor = Executors.newFixedThreadPool(3, new NamedThreadFactory("trace", true));
    eventBus = new AsyncEventBus("asyncTraceEventBus", executor);
    //增加退出回调函数
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        executor.shutdown();
      }
    }));

    //记录到日志
    eventBus.register(new RpcLog());
    //每分钟统计
    eventBus.register(new OssStat());
    //trace跟踪
    eventBus.register(new OssTrace());
  }

  public void post(TraceContext c) {
    c.setClientIp(clientIp);
    c.setClientName(clientName);
    eventBus.post(c);
  }
}
