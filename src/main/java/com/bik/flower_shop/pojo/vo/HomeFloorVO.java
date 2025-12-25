package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页楼层 VO
 *
 * @author bik
 */
@Data
public class HomeFloorVO {

    private Integer id;

    /**
     * 楼层标题
     */
    private String title;

    private Integer categoryId;


    /**
     * 左侧海报
     */
    private String bannerImage;

    /**
     * 商品列表
     */
    private List<GoodsSimpleVO> products;
}
