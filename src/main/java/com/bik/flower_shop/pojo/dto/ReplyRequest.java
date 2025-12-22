package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyRequest {
    @NotNull(message = "评论ID不能为空")
    private Integer commentId;

    @NotNull(message = "回复内容不能为空")
    private String replyContent;
}