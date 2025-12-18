package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class FavoriteResultDTO {
    private boolean IsFavorite;
    private Integer likeCount;
    private String message;
}
