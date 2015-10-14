package com.github.jedis.support;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-redis.xml")
public class JedisSupportTest {
  private Logger log = LoggerFactory.getLogger(JedisSupportTest.class);
  @Resource(name = "testRedisSupport")
  private MergeJedisCmd jedis;

  @Test
  public void testGetResource() throws Exception {
    String key = "-lirui-";
    jedis.set(key, "1");
    jedis.expire(key, 300);
    assertEquals("1", jedis.get(key));
    jedis.del(key);
  }

  @Test
  public void testMulti() throws Exception {
    jedis.mset("mKey1", "1", "mKey2", "2");
    assertEquals("1", jedis.get("mKey1"));
    assertEquals("2", jedis.get("mKey2"));
    List<String> mget = jedis.mget("mKey1", "mKey2", "mKey3");
    assertEquals("1", mget.get(0));
    assertEquals("2", mget.get(1));
    assertEquals(null, mget.get(2));
    jedis.del("mKey1", "mKey2");
  }

  @Test
  public void testList() throws Exception {
    String key = "testList";
    jedis.rpush(key, "1");
    jedis.rpush(key, "2");
    List<String> items = jedis.lrange(key, 0, 1000);
    assertEquals("1", items.get(0));
    assertEquals("2", items.get(1));
    jedis.del(key);
  }

  @Test
  public void testPubSub() throws Exception {
    final String ch = "test-pub-sub";
    final List<String> messages = Lists.newArrayList();
    new Thread(new Runnable() {
      @Override
      public void run() {
        jedis.subscribe(new JedisPubSub() {
          @Override
          public void onMessage(String channel, String message) {
            log.info("onMessage, ch={}, msg={}", channel, message);
            messages.add(message);
          }

          @Override
          public void onSubscribe(String channel, int subscribedChannels) {
            log.info("onSubscribe, ch={}, num={}", channel, subscribedChannels);
          }
        }, ch);
      }
    }).start();
    Thread.sleep(200);
    jedis.publish(ch, "hello");
    Thread.sleep(200);
    assertEquals(1, messages.size());
    assertEquals("hello", messages.get(0));
  }
}
