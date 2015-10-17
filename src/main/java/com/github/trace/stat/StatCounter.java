package com.github.trace.stat;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 封装了公共的计数器逻辑
 *
 * @author colinli
 */
public abstract class StatCounter {
  public static final int SLOW_TIME_LIMIT = 100;
  //依次是总调用次数，失败数，流控失败数，超时数，总耗时
  private AtomicIntegerArray counters = new AtomicIntegerArray(5);

  /**
   * 上报执行状态
   *
   * @param state -1:fail, 0:succ, 1:流控获取不到client
   * @param time  本次调用耗时
   */
  public void report(int state, int time) {
    counters.getAndIncrement(0); //增加总调用次数
    counters.addAndGet(4, time);
    if (state != 0) {
      counters.getAndIncrement(1); //增加失败数
      if (state == 1) {
        counters.getAndIncrement(2); //增加流控失败数
      }
    }
    if (time >= SLOW_TIME_LIMIT) {
      counters.getAndIncrement(3); //增加超时数
    }
  }

  public int getTotalCount() {
    return counters.get(0);
  }

  public int getFailCount() {
    return counters.get(1);
  }

  public int getFlowLimitFailCount() {
    return counters.get(2);
  }

  public int getSlowCount() {
    return counters.get(3);
  }

  public int getTotalCost() {
    return counters.get(4);
  }
}
