package com.github.jedis.support;

import com.github.autoconf.ConfigFactory;
import com.github.autoconf.api.IChangeListener;
import com.github.autoconf.api.IConfig;
import com.github.autoconf.api.IConfigFactory;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.*;

/**
 * 根据配置得到一个pool
 *
 * Created by lirui on 2015/03/10 下午7:56.
 */
public abstract class ConfigurableJedisPool implements InitializingBean, DisposableBean {
  private static final TimeZone chinaZone = TimeZone.getTimeZone("GMT+08:00");
  private static final Locale chinaLocale = Locale.CHINA;
	private static final Set<String> names = ImmutableSet.of("Boolean", "Character", "Byte", "Short", "Long", "Integer", "Byte", "Float", "Double", "Void", "String");
	private final CharMatcher matcher = CharMatcher.anyOf(", ;|");
	protected Logger log = LoggerFactory.getLogger(getClass());
  private IConfigFactory configFactory;
	private String configName;
	/**
	 * 若果有多个服务地址，用Sharded
	 */
	private ShardedJedisPool shardedPool = null;
	/**
	 * 只有一个服务地址的时候，就用jedis好了，省去很多工作
	 */
	private JedisPool pool = null;

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public ShardedJedisPool getShardedPool() {
		return shardedPool;
	}

	public void setShardedPool(ShardedJedisPool shardedPool) {
		this.shardedPool = shardedPool;
	}

	public JedisPool getPool() {
		return pool;
	}

	public void setPool(JedisPool pool) {
		this.pool = pool;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
    if (configFactory == null) {
      configFactory = ConfigFactory.getInstance();
    }
    configFactory.getConfig(configName, new IChangeListener() {
      @Override
      public void changed(IConfig config) {
        try {
          loadConfig(config);
        } catch (Exception e) {
          log.error("cannot load: {}", config.getName(), e);
        }
      }
    });
	}

	@Override
	public void destroy() throws Exception {
		shardedPool.close();
	}

	private void loadConfig(IConfig config) {
		String configServers = config.get("redis.servers");
		if (Strings.isNullOrEmpty(configServers)) return;
		if (!config.getBool("startup", true)) {
      shardedPool.close();
			shardedPool = null;
			return;
		}

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(config.getInt("pool.maxActive", 100));
		poolConfig.setMinIdle(config.getInt("pool.minIdle", 5));
		poolConfig.setMaxIdle(config.getInt("pool.maxIdle", 25));
		poolConfig.setMaxWaitMillis(config.getInt("pool.maxWaitMillis", 3000));
		poolConfig.setTestOnBorrow(config.getBool("pool.testOnBorrow", false));
		poolConfig.setTestOnReturn(config.getBool("pool.testOnReturn", false));
		poolConfig.setTestWhileIdle(config.getBool("pool.testWhileIdle", true));

		if (matcher.countIn(config.get("redis.servers")) > 0) {
			List<JedisShardInfo> servers = getJedisShardInfos(config);
			ShardedJedisPool shardedJedisPool = new ShardedJedisPool(poolConfig, servers);
			if (shardedPool != null) {
				ShardedJedisPool old = shardedPool;
				shardedPool = shardedJedisPool;
        old.close();
			} else {
				shardedPool = shardedJedisPool;
			}
		} else {
			JedisPool jedisPool = createJedisPool(poolConfig, config);
			if (pool != null) {
				JedisPool old = pool;
				pool = jedisPool;
        old.close();
			} else {
				pool = jedisPool;
			}
		}
	}

	private JedisPool createJedisPool(JedisPoolConfig poolConfig, IConfig config) {
		int timeout = config.getInt("redis.timeout", 5000);
		String password = config.get("redis.password");
		int defaultDbIndex = config.getInt("redis.dbIndex", 0);
		String server = config.get("redis.servers");
		List<String> paths = Splitter.on(CharMatcher.anyOf(":/")).splitToList(server);
		String host = paths.get(0);
		int port = 6379, db = defaultDbIndex;
		if (paths.size() > 1) {
			port = Integer.parseInt(paths.get(1), 6379);
		}
		if (paths.size() > 2) {
			db = Integer.parseInt(paths.get(2));
		}
		return new JedisPool(poolConfig, host, port, timeout, password, db);
	}

	private List<JedisShardInfo> getJedisShardInfos(IConfig config) {
		String configServers = config.get("redis.servers");
		List<JedisShardInfo> servers = Lists.newArrayList();
		List<String> items = Splitter.on(matcher).trimResults().omitEmptyStrings().splitToList(configServers);
		int timeout = config.getInt("redis.timeout", 5000);
		String password = config.get("redis.password");
		int defaultDbIndex = config.getInt("redis.dbIndex", 0);
		for (String i : items) {
			List<String> paths = Splitter.on(CharMatcher.anyOf(":/")).splitToList(i);
			String host = paths.get(0);
			int port = 6379, db = defaultDbIndex;
			if (paths.size() > 1) {
				port = Integer.parseInt(paths.get(1));
			}
			if (paths.size() > 2) {
				db = Integer.parseInt(paths.get(2));
			}
			URI redisUri = create(host, port, password, db);
			JedisShardInfo info = new JedisShardInfo(redisUri);
			info.setConnectionTimeout(timeout);
			info.setSoTimeout(timeout);
			if (password != null && password.contains("@")) {
				info.setPassword(password);
			}
			servers.add(info);
		}
		return servers;
	}

	/**
	 * redis的密码当中最好不要包含'@'符号，否则处理起来非常麻烦
	 *
	 * @param host
	 * @param port
	 * @param password
	 * @param dbIndex
	 * @return
	 */
	protected URI create(String host, int port, String password, int dbIndex) {
		StringBuilder sbd = new StringBuilder(32);
		sbd.append("redis://");
		if (!Strings.isNullOrEmpty(password) && !password.contains("@")) {
			sbd.append("user:").append(password).append('@');
		}
		sbd.append(host).append(':').append(port);
		if (dbIndex > 0) {
			sbd.append('/').append(dbIndex);
		}
		return URI.create(sbd.toString());
	}

	/**
	 * 函数参数信息
	 *
	 * @param args 参数列表
	 * @return 格式化输出
	 */
	protected String getParameters(Object[] args) {
		if (args == null) return "";
		StringBuilder sbd = new StringBuilder();
		if (args.length > 0) {
			for (Object i : args) {
				if (i == null) {
					sbd.append("null");
				} else {
					Class clz = i.getClass();
					if (isPrimitive(clz)) {
						sbd.append(evalPrimitive(i));
					} else if (clz.isArray()) {
						evalArray(i, sbd);
					} else if (Collection.class.isAssignableFrom(clz)) {
						Object[] arr = ((Collection<?>) i).toArray();
						evalArray(arr, sbd);
					} else if (i instanceof Date) {
						sbd.append('"').append(formatYmdHis(((Date) i))).append('"');
					} else {
						sbd.append(clz.getSimpleName()).append(":OBJ");
					}
				}
				sbd.append(',');
			}
			sbd.setLength(sbd.length() - 1);
		}
		return sbd.toString();
	}

	private boolean isPrimitive(Class clz) {
		return clz.isPrimitive() || names.contains(clz.getSimpleName());
	}

	private String evalPrimitive(Object obj) {
    String s = String.valueOf(obj);
    if (s.length() > 32) {
      return s.substring(0, 32);
    }
    return s;
	}

	private void evalArray(Object arr, StringBuilder sbd) {
		int sz = Array.getLength(arr);
		if (sz == 0) {
			sbd.append("[]");
			return;
		}
		if (isPrimitive(Array.get(arr, 0).getClass())) {
			sbd.append('[');
			int len = Math.min(sz, 10);
			for (int i = 0; i < len; i++) {
				Object obj = Array.get(arr, i);
				if (isPrimitive(obj.getClass())) {
					sbd.append(evalPrimitive(obj));
				} else {
					sbd.append(obj.getClass().getSimpleName()).append(":OBJ");
				}
				sbd.append(',');
			}
			if (sz > 10) {
				sbd.append(",...,len=").append(sz);
			}
			if (sbd.charAt(sbd.length() - 1) == ',') {
				sbd.setCharAt(sbd.length() - 1, ']');
			} else {
				sbd.append(']');
			}
		} else {
			sbd.append("[len=").append(sz).append(']');
		}
	}

  /**
   * 构造时间的显示，带上时分秒的信息，如 2013-06-11 03:14:25
   *
   * @param date 时间
   * @return 字符串表示
   */
  private String formatYmdHis(Date date) {
    Calendar ca = Calendar.getInstance(chinaZone, chinaLocale);
    ca.setTimeInMillis(date.getTime());
    StringBuilder sbd = new StringBuilder();
    sbd.append(ca.get(Calendar.YEAR)).append('-');
    int month = 1 + ca.get(Calendar.MONTH);
    if (month < 10) {
      sbd.append('0');
    }
    sbd.append(month).append('-');
    int day = ca.get(Calendar.DAY_OF_MONTH);
    if (day < 10) {
      sbd.append('0');
    }
    sbd.append(day).append(' ');
    int hour = ca.get(Calendar.HOUR_OF_DAY);
    if (hour < 10) {
      sbd.append('0');
    }
    sbd.append(hour).append(':');
    int minute = ca.get(Calendar.MINUTE);
    if (minute < 10) {
      sbd.append('0');
    }
    sbd.append(minute).append(':');
    int second = ca.get(Calendar.SECOND);
    if (second < 10) {
      sbd.append('0');
    }
    sbd.append(second);
    return sbd.toString();
  }
}
