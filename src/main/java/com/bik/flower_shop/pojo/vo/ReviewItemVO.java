package com.bik.flower_shop.pojo.vo;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class ReviewItemVO {
    private Integer orderItemId;
    private Integer goodsId;
    private String goodsTitle;
    private String goodsCover;
    private Integer num;
    private Boolean canReview;
}