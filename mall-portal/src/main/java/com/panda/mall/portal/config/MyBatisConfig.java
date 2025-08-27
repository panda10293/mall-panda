package com.panda.mall.portal.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis相关配置
 * Created by panda on 2019/4/8.
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.panda.mall.mapper","com.panda.mall.portal.dao"})
public class MyBatisConfig {
}
