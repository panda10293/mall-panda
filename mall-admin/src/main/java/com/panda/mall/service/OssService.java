package com.panda.mall.service;

import com.panda.mall.dto.OssCallbackResult;
import com.panda.mall.dto.OssPolicyResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * oss上传管理Service
 * Created by panda on 2018/5/17.
 */
public interface OssService {
    /**
     * oss上传策略生成
     */
    OssPolicyResult policy();
    /**
     * oss上传成功回调
     */
    OssCallbackResult callback(HttpServletRequest request);
}
