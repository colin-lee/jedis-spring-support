package com.wenwen.jedis.support;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * 测试cache注解
 * Created by lirui on 2015/03/10 下午9:58.
 */
public class CacheService {
  private static final String NAME = "TestCache";
  private volatile int c1 = 0;
  private volatile int c2 = 0;

  public int getC1() {
    return c1;
  }

  public int getC2() {
    return c2;
  }

  @Cacheable(value = NAME)
  public String concat(String a, String b) {
    ++c1;
    return a + b;
  }

  @Cacheable(value = NAME)
  public String upper(String a) {
    ++c2;
    return a.toUpperCase();
  }

  @CacheEvict(value = NAME)
  public void clear(String a) {
  }

  @CacheEvict(value = NAME, allEntries = true)
  public void clearAll() {
  }
}
