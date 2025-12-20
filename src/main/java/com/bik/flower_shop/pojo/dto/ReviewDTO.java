package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewDTO {
    private Integer id;
    private Integer rating;
    private String content;
    private List<String> photos;
    private Long reviewTime;
    private Boolean anonymous;
    private ReviewerDTO user;
}
