package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author bik
 */
@Data
public class OrderAdminItemVO { ;
    private Integer goodsId;
    private String goodsTitle;
    private String goodsCover;
    private Integer num;
    private BigDecimal price;
    private Byte skusType;
    private Map goodsItem;
    private Map goodsSkus;
}
