jedis-spring-support
===
目的
===
1. 用cms系统配置redis参数
1. 提供jedis的全部操作接口，可以使用list/set/map等命令
1. 接入监控系统

使用配置
===
配置maven
=====
    <dependency>
	    <groupId>com.github.colin-lee</groupId>
	    <artifactId>jedis-spring-support</artifactId>
	    <version>1.0.0</version>
    </dependency>

配置spring
====
    <bean id="testRedisSupport" class="com.github.jedis.support.JedisFactoryBean" p:configName="redis-test"/>

使用范例
===
    @Autowired
    @Qualifier("testRedisSupport")
    private JedisCmd jedis;

    若果是二进制协议，可以用
    private BinaryJedisCmd jedis
