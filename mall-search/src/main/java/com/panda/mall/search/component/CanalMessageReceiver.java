package com.panda.mall.search.component;

import com.panda.mall.search.service.EsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "mall.product.sync.queue")
public class CanalMessageReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanalMessageReceiver.class);

    @Autowired
    private EsProductService esProductService;

    @RabbitHandler
    public void handle(String productId) {
        LOGGER.info("接收到来自Canal的商品变更消息，商品ID：{}", productId);
        try {
            Long id = Long.parseLong(productId);
            esProductService.importProductById(id);
            LOGGER.info("商品id:{} 的数据已同步到ES", productId);
        } catch (NumberFormatException e) {
            LOGGER.error("无效商品id");
        } catch (Exception e) {
            LOGGER.error("同步至ES出错，商品id:{}", productId);
        }
    }

}
