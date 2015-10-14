package com.github.trace.stat;

import com.github.trace.TraceContext;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

/**
 * n分钟内的rpc调用统计快照
 *
 * Created by lirui on 2015/07/17 上午11:26.
 */
public class Snapshot {
	private final long stamp = System.currentTimeMillis();
	private final ConcurrentMap<String, RpcStatCounter> rpc = Maps.newConcurrentMap();
	private final ConcurrentMap<String, ServiceStatCounter> service = Maps.newConcurrentMap();
	public static final char SEPARATOR = 0x03;

	public long getStamp() {
		return stamp;
	}

	public ConcurrentMap<String, RpcStatCounter> getRpc() {
		return rpc;
	}

	public ConcurrentMap<String, ServiceStatCounter> getService() {
		return service;
	}

	public void add(TraceContext c) {
		// 统计rpc
		String key = c.getServerName() + SEPARATOR + c.getUrl();
		RpcStatCounter c1 = rpc.get(key);
		if (c1 == null) {
			c1 = new RpcStatCounter(c.getServerName(), c.getUrl());
			RpcStatCounter real = rpc.putIfAbsent(key, c1);
			if (real != null) c1 = real;
		}
		int state = c.isFail() ? -1 : 0;
		int cost = (int) c.getCost();
		c1.report(state, cost);

		// 统计service
		String m = c.getIface() + '.' + c.getMethod();
		key = c.getServerName() + SEPARATOR + m;
		ServiceStatCounter c2 = service.get(key);
		if (c2 == null) {
			c2 = new ServiceStatCounter(c.getServerName(), m);
			ServiceStatCounter real = service.put(key, c2);
			if (real != null) c2 = real;
		}
		c2.report(state, cost);
	}
}
