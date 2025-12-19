package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class OrderAdminPageVO {
    private List<OrderAdminListVO> list;
    private long totalCount;
}
