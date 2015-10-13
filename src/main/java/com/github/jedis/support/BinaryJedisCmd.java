package com.github.jedis.support;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.MultiKeyBinaryCommands;

/**
 * 支持binary类型的存取
 * <p/>
 * Created by lirui on 15/2/9.
 */
public interface BinaryJedisCmd extends BinaryJedisCommands, MultiKeyBinaryCommands {
}
