package com.github.trace.sender;

import com.google.common.eventbus.EventBus;

/**
 * 发送消息到内部总线上
 * Created by lirui on 2015-10-17 18:24.
 */
public class Sender {
  private static final Sender INSTANCE = new Sender();
  private final EventBus bus;

  public static Sender getInstance() {
    return INSTANCE;
  }

  private Sender() {
    bus = new EventBus("sender-bus");
  }

  public EventBus getBus() {
    return bus;
  }
}
