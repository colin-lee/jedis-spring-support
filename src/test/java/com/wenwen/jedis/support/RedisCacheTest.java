package com.wenwen.jedis.support;

import com.github.jedis.support.JedisCmd;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-redis-cache.xml")
public class RedisCacheTest {
	@Autowired
	private CacheService service;

	@Resource(name = "testRedisCache")
	private JedisCmd jedis;

	@Test
	public void testCache() throws Exception {
		service.clearAll();

		String s = service.concat("hello", "World");
		assertEquals("helloWorld", s);

		assertEquals(1, service.getC1());

		//验证只执行一次
		s = service.concat("hello", "World");
		assertEquals("helloWorld", s);
		assertEquals(1, service.getC1());

		String upper = service.upper("hello");
		assertEquals("HELLO", upper);
		assertEquals(1, service.getC2());

		//验证命中cache
		upper = service.upper("hello");
		assertEquals("HELLO", upper);
		assertEquals(1, service.getC2());

		//验证清理cache
		service.clear("hello");
		upper = service.upper("hello");
		assertEquals("HELLO", upper);
		assertEquals(2, service.getC2());

		//验证全部清空
		service.clearAll();
		s = service.concat("hello", "World");
		assertEquals("helloWorld", s);
		assertEquals(2, service.getC1());

		upper = service.upper("hello");
		assertEquals("HELLO", upper);
		assertEquals(3, service.getC2());
	}
}
