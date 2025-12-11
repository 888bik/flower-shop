package com.bik.flower_shop.pojo.vo;

import lombok.Data;

/**
 * 分类类型 VO
 * 用于返回给前端 [{ code, name }]
 */
@Data
public class CategoryTypeVO {

    /** 分类类型唯一标识（推荐用英文 code，如: usage / generic） */
    private String code;

    /** 类型中文名，如：用途、品种、场景等 */
    private String name;
}
