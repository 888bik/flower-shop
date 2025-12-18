package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class SkuVO {
    /** 0 单规格 1 多规格 */
    private Byte type;
    private List<SkuItemVO> list;
}
