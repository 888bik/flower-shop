package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class UpdateGoodsSkusOrderDTO {

    @Data
    public static class SortItem {
        private Long id;     // 规格ID
        private Integer order; // 新的顺序
    }

    private List<SortItem> sortData;
}
