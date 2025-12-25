package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.entity.Category;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bik
 */
@Data
public class CategoryVO {
    // 一级分类名称
    private String name;
    private Integer typeId;
    // 二级分类列表
    private List<Category> children = new ArrayList<>();
}
