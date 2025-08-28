package com.panda.mall.canal.config;


import com.panda.mall.common.domain.QueueEnum;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CanalRabbitConfig {
    @Bean
    public DirectExchange productSyncExchange() {
        return new DirectExchange(QueueEnum.PRODUCT_SYNC.getExchange(), true, false);
    }

    @Bean
    public Queue productSyncQueue() {
        return new Queue(QueueEnum.PRODUCT_SYNC.getName(), true, false, false);
    }

    @Bean
    public Binding productSyncBinding() {
        return BindingBuilder.bind(productSyncQueue()).to(productSyncExchange()).with(QueueEnum.PRODUCT_SYNC.getRouteKey());
    }

    @Bean
    public DirectExchange cacheSyncExchange() {
        return new DirectExchange(QueueEnum.CACHE_SYNC.getExchange(), true, false);
    }

    // 缓存同步队列
    @Bean
    public Queue cacheSyncQueue() {
        return new Queue(QueueEnum.CACHE_SYNC.getName(), true, false, false);
    }

    // 绑定缓存同步的交换机和队列
    @Bean
    public Binding cacheSyncBinding() {
        return BindingBuilder.bind(cacheSyncQueue()).to(cacheSyncExchange()).with(QueueEnum.CACHE_SYNC.getRouteKey());
    }
}
