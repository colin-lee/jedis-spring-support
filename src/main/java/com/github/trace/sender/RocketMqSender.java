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
import com.github.trace.NamedThreadFactory;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发送消息到RocketMQ
 * Created by lirui on 2015-10-14 18:52.
 */
public class RocketMqSender implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(RocketMqSender.class);
  private final ConcurrentLinkedQueue<Message> queue = Queues.newConcurrentLinkedQueue();
  private ExecutorService executor;
  private DefaultMQProducer sender;
  private boolean running;

  private static final RocketMqSender INSTANCE = new RocketMqSender();

  public static RocketMqSender getInstance() {
    return INSTANCE;
  }

  private RocketMqSender() {
    NamedThreadFactory factory = new NamedThreadFactory("trace-mq-sender", true);
    executor = Executors.newSingleThreadExecutor(factory);
    executor.submit(this);
    ConfigFactory.getInstance().getConfig("", new IChangeListener() {
      @Override
      public void changed(IConfig config) {
        reload(config);
      }
    });

    //增加退出回调功能
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        running = false;
        executor.shutdown();
        sender.shutdown();
      }
    }));
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
    running = true;
    while (running) {
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

  public void asyncSend(Message m) {
    queue.add(m);
  }
}
