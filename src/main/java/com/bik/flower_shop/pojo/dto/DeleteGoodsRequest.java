package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量删除商品请求体
 * @author bik
 */
@Data
public class DeleteGoodsRequest {
    private List<Integer> ids;
}
