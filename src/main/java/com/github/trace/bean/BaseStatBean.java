package com.github.trace.bean;

import java.io.Serializable;

/**
 * 计数器相关代码
 * Created by lirui on 2015-10-14 14:30.
 */
public abstract class BaseStatBean implements Serializable {
  protected int totalCount = 0;
  protected int failCount = 0;
  protected int slowCount = 0;
  protected long totalCost = 0;

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public int getFailCount() {
    return failCount;
  }

  public void setFailCount(int failCount) {
    this.failCount = failCount;
  }

  public int getSlowCount() {
    return slowCount;
  }

  public void setSlowCount(int slowCount) {
    this.slowCount = slowCount;
  }

  public long getTotalCost() {
    return totalCost;
  }

  public void setTotalCost(long totalCost) {
    this.totalCost = totalCost;
  }

  public String getFailPercent() {
    return getPercent(failCount, totalCount);
  }

  public String getSlowPercent() {
    return getPercent(slowCount, totalCount);
  }

  public String getAverageCost() {
    if (totalCount == 0 || totalCost == 0) {
      return "0";
    }
    return formatNumber(totalCost * 1.0 / totalCount);
  }

  private String getPercent(int top, int bottom) {
    if (bottom == 0 || top == 0) {
      return "0";
    }

    return formatNumber(top * 100.0 / bottom);
  }

  private String formatNumber(double number) {
    String ret = String.format("%.2f", number);
    if (ret.endsWith(".00")) {
      ret = ret.substring(0, ret.length() - 3);
    }
    return ret;
  }

  public void copy(BaseStatBean counter) {
    totalCount = counter.getTotalCount();
    totalCost = counter.getTotalCost();
    failCount = counter.getFailCount();
    slowCount = counter.getSlowCount();
  }

  public void add(BaseStatBean counter) {
    totalCount += counter.getTotalCount();
    totalCost += counter.getTotalCost();
    failCount += counter.getFailCount();
    slowCount += counter.getSlowCount();
  }
}
