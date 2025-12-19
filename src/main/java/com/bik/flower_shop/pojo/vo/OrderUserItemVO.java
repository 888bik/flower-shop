package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class OrderUserItemVO {

    /** 商品 ID */
    private Integer goodsId;

    /** 商品名称（下单时快照） */
    private String goodsTitle;

    /** 商品主图 */
    private String goodsCover;

    /** 单价（下单时价格快照） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer num;

    /** 小计 = price * num（前端可直接用） */
    private BigDecimal subtotal;
}
