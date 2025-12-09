package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class CheckGoodsDTO {
    // 1 = 同意，2 = 拒绝
    private Byte ischeck;
}
