package com.github.jedis.support;

import com.github.trace.TraceContext;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.Reflection;
import org.springframework.beans.factory.FactoryBean;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.ConnectException;
import java.util.Set;

/**
 * 通过cms系统配置redis，并接入监控系统
 * <p/>
 * Created by lirui on 15/1/18.
 */
public class JedisFactory<T> extends ConfigurableJedisPool implements FactoryBean<T> {
  private Set<String> names = ImmutableSet.of("hashCode", "toString", "equals");
  private Class<T> clazz;

  @SuppressWarnings("unchecked")
  public JedisFactory() {
    this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  @Override
  public T getObject() throws Exception {
    return Reflection.newProxy(clazz, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (names.contains(method.getName())) {
          JedisPool pool = getPool();
          if (pool != null) {
            return method.invoke(pool.getResource(), args);
          } else {
            return method.invoke(getShardedPool().getResource(), args);
          }
        }

        String configName = getConfigName();
        TraceContext context = TraceContext.get();
        context.reset();
        context.inc();
        context.setIface("redis");
        context.setMethod(method.getName());
        context.setParameter(getParameters(args));
        context.setServerName("redis:" + configName);
        JedisPool pool = getPool();
        if (pool != null) {
          return exchangeRedis(pool, method, args, configName, context);
        } else {
          return exchangeRedis(getShardedPool(), method, args, configName, context);
        }
      }
    });
  }

  private <T> Object exchangeRedis(Pool<T> pool, Method method, Object[] args, String configName, TraceContext context) throws ConnectException {
    Object ret = null;
    boolean fail = true;
    long start = System.currentTimeMillis();
    try {
      T redis;
      try {
        redis = pool.getResource();
      } catch (Exception e) {
        log.error("cannot getResource from:{}", configName, e);
        return null;
      }
      if (redis != null) {
        try {
          ret = method.invoke(redis, args);
          pool.returnResource(redis);
          fail = false;
        } catch (Exception e) {
          pool.returnBrokenResource(redis);
          context.setReason(e.getMessage());
          log.error("cannot exchange Redis: " + configName, e);
          return null;
        } finally {
          Client client = null;
          if (redis instanceof Jedis) {
            client = ((Jedis) redis).getClient();
          } else {
            //TODO: 获取真实地址
            /*
            Jedis real = (Jedis) Sharded.ACTIVE.get();
						if (real != null) {
							client = real.getClient();
						}
						Sharded.ACTIVE.remove();
						*/
          }
          if (client != null) {
            String url = "redis://" + client.getHost() + ':' + client.getPort();
            context.setUrl(url);
          }
        }
      }
    } finally {
      long cost = System.currentTimeMillis() - start;
      context.setStamp(start);
      context.setFail(fail);
      context.setCost(cost);
      context.report();
    }
    return ret;
  }

  @Override
  public Class<?> getObjectType() {
    return clazz;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}
