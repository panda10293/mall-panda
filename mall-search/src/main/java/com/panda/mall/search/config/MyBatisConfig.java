package com.panda.mall.search.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis相关配置
 * Created by panda on 2019/4/8.
 */
@Configuration
@MapperScan({"com.panda.mall.mapper","com.panda.mall.search.dao"})
public class MyBatisConfig {
}
