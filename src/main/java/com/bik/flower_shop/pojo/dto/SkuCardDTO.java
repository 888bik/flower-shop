package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author bik
 * 用于规格选择器：每个 card 表示一个规格维度（颜色 / 尺寸）
 */
@Data
public class SkuCardDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer cardId;
    private String name;
    private List<SkuCardValueDTO> values;
}