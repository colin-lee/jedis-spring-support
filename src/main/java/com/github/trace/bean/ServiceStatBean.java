package com.github.trace.bean;

/**
 * 服务调用信息
 * Created by lirui on 2015-10-14 14:30.
 */
public class ServiceStatBean extends BaseStatBean {
  private long stamp;
  private String module;
  private String method;
  private String caller;

  public ServiceStatBean() {
  }

  public ServiceStatBean(long stamp, String module, String method, String caller,
                         int totalCount, long totalCost, int failCount, int slowCount) {
    this.stamp = stamp;
    this.module = module;
    this.method = method;
    this.caller = caller;
    this.totalCount = totalCount;
    this.totalCost = totalCost;
    this.failCount = failCount;
    this.slowCount = slowCount;
  }

  public long getStamp() {
    return stamp;
  }

  public void setStamp(long stamp) {
    this.stamp = stamp;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getCaller() {
    return caller;
  }

  public void setCaller(String caller) {
    this.caller = caller;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("ServiceStatBean{stamp=").append(stamp);
    sb.append(", module=").append(module);
    sb.append(", method=").append(method);
    sb.append(", caller=").append(caller);
    sb.append(", totalCount=").append(totalCount);
    sb.append(", totalCost=").append(totalCost);
    sb.append(", failCount=").append(failCount);
    sb.append(", slowCount=").append(slowCount);
    sb.append('}');
    return sb.toString();
  }
}
