package com.panda.mall.dto;

import com.panda.mall.model.OmsCompanyAddress;
import com.panda.mall.model.OmsOrderReturnApply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 申请信息封装
 * Created by panda on 2018/10/18.
 */
public class OmsOrderReturnApplyResult extends OmsOrderReturnApply {
    @Getter
    @Setter
    @Schema(title = "公司收货地址")
    private OmsCompanyAddress companyAddress;
}
