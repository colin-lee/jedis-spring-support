package com.github.jedis.support;

import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;

/**
 * 支持String类型的存取
 * <p/>
 * Created by lirui on 15/2/9.
 */
public interface JedisCmd extends JedisCommands, MultiKeyCommands {
}
