package com.panda.mall.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SeckillOrderMessage implements Serializable {

    private Long userId;

    private Long productId;

    private Long skuId;
}
