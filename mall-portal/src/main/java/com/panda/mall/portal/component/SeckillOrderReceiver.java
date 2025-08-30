package com.panda.mall.portal.component;

import com.panda.mall.common.dto.SeckillOrderMessage;
import com.panda.mall.portal.domain.OrderParam;
import com.panda.mall.portal.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues = "mall.order.cancel.ttl")
public class SeckillOrderReceiver {

    @Autowired
    private OmsPortalOrderService omsPortalOrderService;

    @RabbitHandler
    public void handle(SeckillOrderMessage message) {
        log.info("接收到秒杀异步下单消息: {}", message);
        try {
            // 将消息DTO转换为原来的OrderParam
            // 注意：这里需要根据实际情况构造OrderParam，可能需要查询数据库获取一些信息
            OrderParam orderParam = new OrderParam();
            // ... 根据 message 填充 orderParam ...

            // 调用原有的同步方法完成数据库操作
            // 注意：需要处理幂等性，防止重复消费
            // 可以在下单前检查是否已为该用户和商品创建过订单
            omsPortalOrderService.generateOrder(orderParam);

            log.info("秒杀订单处理成功！用户ID: {}, 商品ID: {}", message.getUserId(), message.getProductId());
        } catch (Exception e) {
            log.error("处理秒杀订单失败: {}", message, e);
            // 失败处理逻辑，例如记录失败日志，或者进行库存归还
            // redisService.incr("seckill:stock:" + message.getSkuId(), 1);
        }
    }
}
