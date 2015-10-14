package com.github.trace.stat;

public class RpcStatCounter extends StatCounter {
  private final String module;
  private final String server;

  public RpcStatCounter(String module, String server) {
    this.module = module;
    this.server = server;
  }

  public String getModule() {
    return module;
  }

  public String getServer() {
    return server;
  }
}
