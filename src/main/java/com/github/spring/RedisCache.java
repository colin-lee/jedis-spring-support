package com.github.spring;

import com.github.autoconf.helper.ConfigHelper;
import com.github.jedis.support.BinaryJedisCmd;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;

/**
 * 用redis做cache存储
 * Created by lirui on 2015/03/10 下午8:32.
 */
public class RedisCache implements Cache {
  private static final Logger LOG = LoggerFactory.getLogger(RedisCache.class);
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final byte[] EMPTY_ARRAY = new byte[0];
  private static final int PAGE_SIZE = 128;
  private final String name;
  private final int expiration;
  private final BinaryJedisCmd redis;
  /**
   * 给一个固定前缀，避免和其他有冲突
   */
  private final byte[] prefix;
  /**
   * 把所有设定的key记录下来，方便做clearAll操作
   */
  private final byte[] setName;

  private Converter<Object, byte[]> serializer = new SerializingConverter();
  private Converter<byte[], Object> deserializer = new DeserializingConverter();

  public RedisCache(String name, int expiration, BinaryJedisCmd redis) {
    String profile = ConfigHelper.getApplicationConfig().get("process.profile", "");
    this.name = Strings.isNullOrEmpty(name) ? "default" : name;
    this.expiration = expiration;
    this.redis = redis;
    this.prefix = (profile + ':' + name).getBytes(UTF8);
    // name of the set holding the keys
    this.setName = (profile + ':' + name + "~keys").getBytes(UTF8);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getNativeCache() {
    return redis;
  }

  @Override
  public ValueWrapper get(Object key) {
    Object value = deserialize(redis.get(computeKey(key)));
    return value == null ? null : new SimpleValueWrapper(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Object key, Class<T> type) {
    ValueWrapper wrapper = get(key);
    return wrapper == null ? null : (T) wrapper.get();
  }

  @Override
  public void put(Object key, Object value) {
    final byte[] keyBytes = computeKey(key);
    final byte[] valueBytes = serialize(value);
    redis.set(keyBytes, valueBytes);
    redis.zadd(setName, 0, keyBytes);
    if (expiration > 0) {
      redis.expire(keyBytes, expiration);
      redis.expire(setName, expiration);
    }
  }

  @Override
  public ValueWrapper putIfAbsent(Object key, Object value) {
    final byte[] keyBytes = computeKey(key);
    final byte[] valueBytes = serialize(value);
    final Long nx = redis.setnx(keyBytes, valueBytes);
    if (nx == 1) {
      redis.zadd(setName, 0, keyBytes);
      if (expiration > 0) {
        redis.expire(keyBytes, expiration);
        redis.expire(setName, expiration);
      }
      value = deserialize(redis.get(keyBytes));
    }
    return new SimpleValueWrapper(value);
  }

  @Override
  public void evict(Object key) {
    final byte[] keyBytes = computeKey(key);
    redis.del(keyBytes);
    redis.zrem(setName, keyBytes);
  }

  @Override
  public void clear() {
    int offset = 0;
    boolean finished;
    do {
      // need to paginate the keys
      Set<byte[]> keys = redis.zrange(setName, (offset) * PAGE_SIZE, (offset + 1) * PAGE_SIZE - 1);
      finished = keys.size() < PAGE_SIZE;
      offset++;
      if (!keys.isEmpty()) {
        for (byte[] i : keys) {
          redis.del(i);
        }
      }
    } while (!finished);
    redis.del(setName);
  }

  private byte[] computeKey(Object key) {
    byte[] keyBytes = convertToBytesIfNecessary(key);
    byte[] result = Arrays.copyOf(prefix, prefix.length + keyBytes.length);
    System.arraycopy(keyBytes, 0, result, prefix.length, keyBytes.length);
    return result;
  }

  private byte[] convertToBytesIfNecessary(Object value) {
    if (value instanceof byte[]) {
      return (byte[]) value;
    } else {
      return String.valueOf(value).getBytes(UTF8);
    }
  }

  private Object deserialize(byte[] bytes) {
    if (bytes != null && bytes.length > 0) {
      try {
        return deserializer.convert(bytes);
      } catch (Exception ex) {
        LOG.error("cannot deserialize", ex);
      }
    }
    return null;
  }

  private byte[] serialize(Object object) {
    if (object != null) {
      try {
        return serializer.convert(object);
      } catch (Exception ex) {
        LOG.error("cannot serialize: {}", object, ex);
      }
    }
    return EMPTY_ARRAY;
  }
}
