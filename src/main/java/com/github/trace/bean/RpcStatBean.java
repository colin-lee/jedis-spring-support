package com.github.trace.bean;

/**
 * RPC调用信息
 * Created by lirui on 2015-10-14 14:29.
 */
public class RpcStatBean extends BaseStatBean {
  private long stamp;
  private String module;
  private String caller;
  private String server;

  public RpcStatBean() {
  }

  public RpcStatBean(long stamp, String module, String caller, String server,
                     int totalCount, long totalCost, int failCount, int slowCount) {
    this.stamp = stamp;
    this.module = module;
    this.caller = caller;
    this.server = server;
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

  public String getCaller() {
    return caller;
  }

  public void setCaller(String caller) {
    this.caller = caller;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("RpcStatBean{stamp=").append(stamp);
    sb.append(", module='").append(module).append('\'');
    sb.append(", caller='").append(caller).append('\'');
    sb.append(", server='").append(server).append('\'');
    sb.append(", totalCount=").append(totalCount);
    sb.append(", totalCost=").append(totalCost);
    sb.append(", failCount=").append(failCount);
    sb.append(", slowCount=").append(slowCount);
    sb.append('}');
    return sb.toString();
  }
}
