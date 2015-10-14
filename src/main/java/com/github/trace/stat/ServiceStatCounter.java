package com.github.trace.stat;

public class ServiceStatCounter extends StatCounter {
  private final String module;
  private final String method;

  public ServiceStatCounter(String module, String method) {
    super();
    this.module = module;
    this.method = method;
  }

  public String getModule() {
    return module;
  }

  public String getMethod() {
    return method;
  }
}
