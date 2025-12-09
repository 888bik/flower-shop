package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class ChangeGoodsStatusDTO {
    private List<Integer> ids;
    // 0 下架 / 1 上架
    private Byte status;
}
