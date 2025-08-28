package com.panda.mall.portal.component;


import com.panda.mall.common.dto.CacheSyncMessage;
import com.panda.mall.common.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues = "mall.cache.sync.queue")
public class CacheSyncReceiver {

    @Value("${redis.key.prefix.product}")
    private String REDIS_KEY_PREFIX_PRODUCT;
    @Value("${redis.database}")
    private String REDIS_DATABASE;

    @Autowired
    private RedisService redisService;

    @RabbitHandler
    public void handler(CacheSyncMessage message) {
        try {
            // 根据消息类型进行不同的处理
            switch (message.getType()) {
                case "PRODUCT":
                    // 清理商品详情缓存
                    String productCacheKey = REDIS_DATABASE + ":" + REDIS_KEY_PREFIX_PRODUCT + ":" + message.getKeyId();
                    redisService.del(productCacheKey);
                    log.info("商品详情缓存已清理，Key: {}", productCacheKey);
                    break;

                case "PRODUCT_CATEGORY":
                    // 清理商品分类相关的缓存
                    // 比如，清理分类列表缓存
                    // redisService.del(CATEGORY_LIST_CACHE_KEY);
                    log.info("商品分类缓存已清理，ID: {}", message.getKeyId());
                    break;

                // 在这里可以无限扩展 case
                default:
                    log.warn("未知的缓存消息类型: {}", message.getType());
                    break;
            }

        } catch (Exception e) {
            log.error("清理缓存时发生错误: {}", message);
        }
    }

}
