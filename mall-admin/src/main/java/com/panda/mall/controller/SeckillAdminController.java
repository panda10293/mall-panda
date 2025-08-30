package com.panda.mall.controller;

import com.panda.mall.common.api.CommonResult;
import com.panda.mall.common.service.RedisService;
import com.panda.mall.mapper.PmsSkuStockMapper;
import com.panda.mall.model.PmsSkuStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/seckill")
public class SeckillAdminController {

    @Autowired
    private PmsSkuStockMapper pmsSkuStockMapper;

    @Autowired
    private RedisService redisService;

    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";

    /**
     * 预热秒杀商品库存到Redis
     * @param productId 商品ID
     * @param skuId SKU ID
     */
    @PostMapping("/warmup")
    @ResponseBody
    public CommonResult warmupStock(@RequestParam Long productId, @RequestParam Long skuId) {
        PmsSkuStock skuStock = pmsSkuStockMapper.selectByPrimaryKey(skuId);
        if (skuStock != null && skuStock.getProductId().equals(productId)) {
            String key = SECKILL_STOCK_KEY_PREFIX + skuId;
            redisService.set(key, skuStock.getStock());
            return CommonResult.success("库存预热成功，数量：" + skuStock.getStock());
        }
        return CommonResult.failed("商品或SKU不存在");
    }
}
