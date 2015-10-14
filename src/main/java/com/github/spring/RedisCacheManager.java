package com.github.spring;

import com.github.jedis.support.BinaryJedisCmd;
import com.google.common.collect.Maps;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * 支持spring-3.1 的cache注解
 * Created by lirui on 2015/03/10 下午8:23.
 */
public class RedisCacheManager implements CacheManager {
  private ConcurrentMap<String, RedisCache> caches = Maps.newConcurrentMap();
  private int expiration = 0;
  private BinaryJedisCmd redis;

  public RedisCacheManager(int expiration, BinaryJedisCmd redis) {
    this.expiration = expiration;
    this.redis = redis;
  }

  public int getExpiration() {
    return expiration;
  }

  public void setExpiration(int expiration) {
    this.expiration = expiration;
  }

  @Override
  public Cache getCache(String name) {
    RedisCache cache = caches.get(name);
    if (cache == null) {
      cache = new RedisCache(name, expiration, redis);
      RedisCache old = caches.putIfAbsent(name, cache);
      if (old != null) {
        return old;
      }
    }
    return cache;
  }

  @Override
  public Collection<String> getCacheNames() {
    return caches.keySet();
  }
}
