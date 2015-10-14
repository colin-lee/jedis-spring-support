package com.github.trace;

import com.google.common.base.Strings;

/**
 * Trace信息
 * Created by lirui on 2015-10-13 17:54.
 */
public class TraceContext {
  private static final ThreadLocal<TraceContext> context = new ThreadLocal<TraceContext>();
  private long stamp;
  private long cost;
  private String uid;
  private String traceId;
  private String rpcId;
  private String clientIp;
  private String iface;
  private String method;
  private String clientName;
  private String serverName;
  private String url;
  private String parameter;
  private String reason;
  private String extra;
  private String parentRpcId;
  private boolean spider;
  private boolean fail;
  private boolean color;
  private volatile int count = 0;

  public static TraceContext get() {
    TraceContext c = context.get();
    if (c == null) {
      c = new TraceContext();
      c.setParentRpcId("0");
      context.set(c);
    }
    return c;
  }

  public static void remove() {
    context.remove();
  }

  public TraceContext inc() {
    int nr = ++count;
    if (parentRpcId != null) {
      rpcId = parentRpcId + '.' + nr;
    }
    return this;
  }

  public long getStamp() {
    return stamp;
  }

  public TraceContext setStamp(long stamp) {
    this.stamp = stamp;
    return this;
  }

  public long getCost() {
    return cost;
  }

  public TraceContext setCost(long cost) {
    this.cost = cost;
    return this;
  }

  public String getUid() {
    return uid;
  }

  public TraceContext setUid(String uid) {
    this.uid = uid;
    return this;
  }

  public String getTraceId() {
    return traceId;
  }

  public TraceContext setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public String getRpcId() {
    return rpcId;
  }

  public TraceContext setRpcId(String rpcId) {
    this.rpcId = rpcId;
    return this;
  }

  public String getClientIp() {
    return clientIp;
  }

  public TraceContext setClientIp(String clientIp) {
    this.clientIp = clientIp;
    return this;
  }

  public String getIface() {
    return iface;
  }

  public TraceContext setIface(Class<?> iface) {
    this.iface = iface.getSimpleName();
    return this;
  }

  public TraceContext setIface(String iface) {
    this.iface = iface;
    return this;
  }

  public String getMethod() {
    return method;
  }

  public TraceContext setMethod(String method) {
    this.method = method;
    return this;
  }

  public String getClientName() {
    return clientName;
  }

  public TraceContext setClientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  public String getServerName() {
    return serverName;
  }

  public TraceContext setServerName(String serverName) {
    this.serverName = serverName;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public TraceContext setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getParameter() {
    return parameter;
  }

  public TraceContext setParameter(String parameter) {
    this.parameter = parameter;
    return this;
  }

  public String getReason() {
    return reason;
  }

  public TraceContext setReason(String reason) {
    this.reason = reason;
    return this;
  }

  public String getExtra() {
    return extra;
  }

  public TraceContext setExtra(String extra) {
    this.extra = extra;
    return this;
  }

  public boolean isFail() {
    return fail;
  }

  public TraceContext setFail(boolean fail) {
    this.fail = fail;
    return this;
  }

  public boolean isSpider() {
    return spider;
  }

  public TraceContext setSpider(boolean spider) {
    this.spider = spider;
    return this;
  }

  public boolean isColor() {
    return color;
  }

  public TraceContext setColor(boolean color) {
    this.color = color;
    return this;
  }

  public String getParentRpcId() {
    return parentRpcId;
  }

  public TraceContext setParentRpcId(String parentRpcId) {
    this.parentRpcId = parentRpcId;
    return this;
  }

  public int getCount() {
    return count;
  }

  public TraceContext setCount(int count) {
    this.count = count;
    return this;
  }

  public TraceContext reset() {
    this.cost = 0;
    this.iface = null;
    this.method = null;
    this.parameter = null;
    this.reason = null;
    this.extra = null;
    this.fail = false;
    this.serverName = null;
    this.url = null;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(256);
    sb.append(stamp).append('\t');
    sb.append(cost).append('\t');
    sb.append(spider ? 1 : 0).append('\t');
    sb.append(fail ? 1 : 0).append('\t');
    if (Strings.isNullOrEmpty(uid)) {
      sb.append('-').append('\t');
    } else {
      sb.append(uid).append('\t');
    }
    if (Strings.isNullOrEmpty(traceId)) {
      sb.append('-').append('\t');
    } else {
      sb.append(traceId).append('\t');
    }
    if (Strings.isNullOrEmpty(rpcId)) {
      sb.append('-').append('\t');
    } else {
      sb.append(rpcId).append('\t');
    }
    sb.append(clientIp).append('\t');
    sb.append(clientName).append('\t');
    sb.append(serverName).append('\t');
    sb.append(url).append('\t');
    sb.append(iface).append('\t');
    sb.append(method).append('\t');
    sb.append(removeTabChar(parameter)).append('\t');
    sb.append(removeTabChar(reason)).append('\t');
    sb.append(removeTabChar(extra));
    return sb.toString();
  }

  public TraceContext copy() {
    TraceContext n = new TraceContext();
    n.setStamp(stamp)
     .setCost(cost)
     .setTraceId(traceId)
     .setRpcId(rpcId)
     .setSpider(spider)
     .setColor(color)
     .setFail(fail)
     .setIface(iface)
     .setMethod(method)
     .setClientName(clientName)
     .setClientIp(clientIp)
     .setServerName(serverName)
     .setUrl(url)
     .setParameter(parameter)
     .setReason(reason)
     .setExtra(extra)
     .setUid(uid);
    return n;
  }

  private String removeTabChar(String s) {
    if (s == null)
      return "";
    int pos = s.indexOf('\t');
    if (pos == -1)
      return s;
    int length = s.length();
    StringBuilder sb = new StringBuilder(length);
    if (pos > 0) {
      sb.append(s.substring(0, pos)).append(' ');
    }
    for (int i = pos + 1; i < length; i++) {
      char c = s.charAt(i);
      if (c == '\t') {
        sb.append(' ');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
