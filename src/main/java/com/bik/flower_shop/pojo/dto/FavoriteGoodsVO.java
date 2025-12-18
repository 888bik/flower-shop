package com.bik.flower_shop.pojo.dto;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class FavoriteGoodsVO {
    private Integer id;
    private String title;
    private Integer categoryId;
    private String cover;
    private BigDecimal minPrice;
    private BigDecimal minOprice;
    private String unit;
    private Integer stock;
    private Integer likeCount;
}
