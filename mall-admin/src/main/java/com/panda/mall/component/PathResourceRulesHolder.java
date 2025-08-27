package com.panda.mall.component;

import com.panda.mall.service.UmsResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 路径与资源访问对应关系操作组件
 * Created by panda on 2020/7/17.
 */
@Component
public class PathResourceRulesHolder {

    @Autowired
    private UmsResourceService resourceService;

    @PostConstruct
    public void initPathResourceMap(){
        resourceService.initPathResourceMap();
    }
}
