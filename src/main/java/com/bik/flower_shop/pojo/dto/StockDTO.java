package com.bik.flower_shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author bik
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer total;     // 总库存
    private Boolean display;   // 是否显示库存给用户
    private Integer minStock;  // 库存预警阈值
}
