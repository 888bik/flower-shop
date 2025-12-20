package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class ReviewItemDTO {
    private Integer orderItemId;
    private Integer rating;
    private String content;
    private List<String> images;
}