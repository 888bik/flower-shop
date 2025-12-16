package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class OrderListResponse {
    private List<OrderListVO> list;
    private long totalCount;
}
