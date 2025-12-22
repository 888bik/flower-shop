package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author bik
 */
@Data
public class StatusRequest {
    @NotNull(message = "评论ID不能为空")
    private Integer commentId;

    /**
     * 状态：1显示，0隐藏
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}