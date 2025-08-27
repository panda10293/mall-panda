package com.panda.mall.canal.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CanalRabbitConfig {
    public static final String PRODUCT_SYNC_EXCHANGE = "mall.product.sync.exchange";
    public static final String PRODUCT_SYNC_QUEUE = "mall.product.sync.queue";
    public static final String PRODUCT_SYNC_ROUTING_KEY = "mall.product.sync.routing.key";


    @Bean
    public DirectExchange productSyncExchange() {
        return new DirectExchange(PRODUCT_SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue productSyncQueue() {
        return new Queue(PRODUCT_SYNC_QUEUE, true, false, false);
    }

    @Bean
    public Binding productSyncBinding() {
        return BindingBuilder.bind(productSyncQueue()).to(productSyncExchange()).with(PRODUCT_SYNC_ROUTING_KEY);
    }
}
