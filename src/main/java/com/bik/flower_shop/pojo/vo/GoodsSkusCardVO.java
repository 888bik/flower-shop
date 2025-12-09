package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.entity.GoodsSkusCardValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bik
 */
@Data
public class GoodsSkusCardVO {
    private Integer id;
    private Integer goodsId;
    private String name;
    private Byte type;
    private Integer order;
    private List<GoodsSkusCardValue> goodsSkusCardValue = new ArrayList<>();
}
