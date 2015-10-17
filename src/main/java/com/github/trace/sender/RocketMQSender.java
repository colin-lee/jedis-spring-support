package com.github.trace.sender;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.message.Message;
import com.github.autoconf.ConfigFactory;
import com.github.autoconf.api.IChangeListener;
import com.github.autoconf.api.IConfig;
import com.github.autoconf.api.IConfigFactory;
import com.github.trace.NamedThreadFactory;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发送消息到RocketMQ
 * Created by lirui on 2015-10-14 18:52.
 */
public class RocketMQSender implements Runnable, InitializingBean, DisposableBean {
  private static final Logger LOG = LoggerFactory.getLogger(RocketMQSender.class);
  private final ConcurrentLinkedQueue<Message> queue = Queues.newConcurrentLinkedQueue();
  private ExecutorService executor;
  private DefaultMQProducer sender;
  private IConfigFactory configFactory;

  public IConfigFactory getConfigFactory() {
    return configFactory;
  }

  public void setConfigFactory(IConfigFactory configFactory) {
    this.configFactory = configFactory;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    NamedThreadFactory factory = new NamedThreadFactory("rocketmq-sender", true);
    executor = Executors.newSingleThreadExecutor(factory);
    executor.submit(this);
    if (configFactory == null) {
      configFactory = ConfigFactory.getInstance();
    }
    configFactory.getConfig("", new IChangeListener() {
      @Override
      public void changed(IConfig config) {
        reload(config);
      }
    });
  }

  @Override
  public void destroy() throws Exception {
    if (sender != null) {
      sender.shutdown();
      sender = null;
    }
    if (executor != null) {
      executor.shutdown();
      executor = null;
    }
  }

  private void reload(IConfig config) {
    String nameSrv = config.get("NAMESRV_ADDR");
    System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, nameSrv);
    if (sender == null) {
      sender = new DefaultMQProducer();
      sender.setNamesrvAddr(nameSrv);
      try {
        sender.start();
      } catch (MQClientException e) {
        LOG.error("cannot start trace-producer", e);
      }
    } else {
      sender.setNamesrvAddr(nameSrv);
      sender.getDefaultMQProducerImpl().getmQClientFactory().getDefaultMQProducer().setNamesrvAddr(nameSrv);
      sender.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().updateNameServerAddressList(nameSrv);
    }
  }

  @Override
  public void run() {
    for (;;) {
      Message msg = queue.poll();
      if (msg == null || sender == null) {
        continue;
      }
      SendResult sendResult = null;
      try {
        sendResult = sender.send(msg);
      } catch (Exception e) {
        LOG.error("Send error, {}", msg, e);
        e.printStackTrace();
      }
      if (sendResult == null) {
        LOG.error("sendResult=null");
      } else {
        SendStatus status = sendResult.getSendStatus();
        if (status.equals(SendStatus.SEND_OK)) {
          LOG.debug("msgId={}, status={}", sendResult.getMsgId(), status);
        } else {
          LOG.error("msgId={}, status={}", sendResult.getMsgId(), status);
        }
      }
    }
  }
}
