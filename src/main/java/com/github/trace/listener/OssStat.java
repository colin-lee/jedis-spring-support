package com.github.trace.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.Message;
import com.github.autoconf.helper.ConfigHelper;
import com.github.trace.NamedThreadFactory;
import com.github.trace.TraceContext;
import com.github.trace.bean.RpcStatBean;
import com.github.trace.bean.ServiceStatBean;
import com.github.trace.sender.RocketMqSender;
import com.github.trace.stat.RpcStatCounter;
import com.github.trace.stat.ServiceStatCounter;
import com.github.trace.stat.Snapshot;
import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 每分钟统计,并上报到消息总线
 * Created by lirui on 2015-10-14 10:43.
 */
public class OssStat implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(OssStat.class);
  private Snapshot current = new Snapshot();
  private ScheduledExecutorService executor;

  public OssStat() {
    NamedThreadFactory factory = new NamedThreadFactory("oss-stat", true);
    executor = Executors.newSingleThreadScheduledExecutor(factory);
    executor.schedule(this, 1, TimeUnit.MINUTES);
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        executor.shutdown();
      }
    }));
  }

  @Subscribe
  public void stat(TraceContext c) {
    current.add(c);
  }

  /**
   * 获取上次的快照，并重置一个新的
   * @return 上次的快照
   */
  public Snapshot getAndReset() {
    Snapshot old = current;
    current = new Snapshot();
    return old;
  }

  /**
   * 每分钟上报统计信息
   */
  @Override
  public void run() {
    Snapshot s = getAndReset();
    reportRpc(s);
    reportService(s);
  }


  /**
   * 上报service调用统计
   *
   * @param s 当前快照
   */
  private void reportService(Snapshot s) {
    ConcurrentMap<String, ServiceStatCounter> service = s.getService();
    if (service.size() == 0) return;
    long stamp = s.getStamp();
    String caller = ConfigHelper.getServerInnerIP();
    for (Map.Entry<String, ServiceStatCounter> i: service.entrySet()) {
      ServiceStatCounter cnt = i.getValue();
      if (cnt.getTotalCost() == 0) continue;
      String key = i.getKey();
      int pos = key.indexOf(Snapshot.SEPARATOR);
      String module = key.substring(0, pos), method = key.substring(pos + 1);
      ServiceStatBean e = new ServiceStatBean(stamp, module, method, caller, cnt.getTotalCount(), cnt.getTotalCost(), cnt.getFailCount(), cnt.getSlowCount());
      Message m = new Message("JinJingOss", "service", JSON.toJSONBytes(e));
      RocketMqSender.getInstance().asyncSend(m);
      String avg = String.format("%.2f", cnt.getTotalCost() * 1.0 / cnt.getTotalCount());
      String msg = Joiner.on('\t').join(module, method, avg, cnt.getTotalCount(), cnt.getFailCount());
      if (cnt.getFailCount() > 0) {
        LOG.warn(msg);
      } else {
        LOG.info(msg);
      }
    }
  }

  /**
   * 上报rpc调用统计
   *
   * @param s 当前快照
   */
  private void reportRpc(Snapshot s) {
    ConcurrentMap<String, RpcStatCounter> rpc = s.getRpc();
    if (rpc.size() == 0) return;
    long stamp = s.getStamp();
    String caller = ConfigHelper.getServerInnerIP();
    for (Map.Entry<String, RpcStatCounter> i: rpc.entrySet()) {
      RpcStatCounter cnt = i.getValue();
      if (cnt.getTotalCost() == 0) continue;
      String key = i.getKey();
      int pos = key.indexOf(Snapshot.SEPARATOR);
      String module = key.substring(0, pos), url = key.substring(pos + 1);
      RpcStatBean e = new RpcStatBean(stamp, module, caller, url, cnt.getTotalCount(), cnt.getTotalCost(), cnt.getFailCount(), cnt.getSlowCount());
      Message m = new Message("JinJingOss", "rpc", JSON.toJSONBytes(e));
      RocketMqSender.getInstance().asyncSend(m);
      String avg = String.format("%.2f", cnt.getTotalCost() * 1.0 / cnt.getTotalCount());
      String msg = Joiner.on('\t').join(module, url, avg, cnt.getTotalCount(), cnt.getFailCount());
      if (cnt.getFailCount() > 0) {
        LOG.warn(msg);
      } else {
        LOG.info(msg);
      }
    }
  }
}
