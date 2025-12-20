package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class ReviewSubmitDTO {
    private Boolean anonymous = false;
    private List<ReviewItemDTO> items;
}