package com.github.trace.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.common.message.Message;
import com.github.trace.TraceContext;
import com.github.trace.sender.RocketMqSender;
import com.google.common.eventbus.Subscribe;

/**
 * trace跟踪,实时上报到消息总线
 * Created by lirui on 2015-10-14 10:44.
 */
public class OssTrace {
  @Subscribe
  public void saveElasticSearch(TraceContext c) {
    Message m = new Message("JinJingTrace", c.getTraceId(), JSON.toJSONBytes(c));
    RocketMqSender.getInstance().asyncSend(m);
  }
}
