package com.hmdp.utils;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

@Component
public class QueueInitializer {

    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public QueueInitializer(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
        initQueue();
    }

    private void initQueue() {
        Queue myQueue = new Queue("seckill.order", true); // 第一个参数是队列名称，第二个参数是是否持久化
        rabbitAdmin.declareQueue(myQueue); // 创建队列
    }
}