package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class ReviewPageResponse {
    private long total;
    private int page;
    private int pageSize;
    private double avgRating;
    private List<ReviewDTO> list;
}
