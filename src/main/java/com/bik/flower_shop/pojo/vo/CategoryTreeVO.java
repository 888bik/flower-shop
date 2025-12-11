package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bik
 */
@Data
public class CategoryTreeVO {
    private Integer id;
    private String name;
    private Byte status;
    private Integer categoryId;
    private Integer order;
    private Integer createTime;
    private Integer updateTime;
    private String type;
    private List<CategoryTreeVO> children = new ArrayList<>();
}
