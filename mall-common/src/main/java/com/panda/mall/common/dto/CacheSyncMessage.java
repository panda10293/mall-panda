package com.panda.mall.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CacheSyncMessage implements Serializable {

    /**
     * 消息类型，用于消费者区分不同的业务
     * 例如: "PRODUCT", "PRODUCT_CATEGORY", "USER_INFO"
     */
    private String type;

    /**
     * 关联的主键ID
     */
    private String keyId;

    /**
     * 其他需要传递的附加数据，可以是一个JSON字符串或者Map
     * 例如，可以存放旧的分类ID，用于清理旧列表缓存
     */
    private Object payload;
}
