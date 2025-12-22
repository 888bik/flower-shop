package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class CommentDTO {

    private Integer id;
    private Integer orderId;
    private Integer goodsId;
    private String goodsTitle;
    private String goodsCover;

    private Integer userId;
    private String nickname;
    private String avatar;

    private String review;
    private Integer rating;
    private String reviewImages;
    private Integer reviewTime;
    private Byte reviewStatus;

    private String reviewAppend;
    private Integer reviewAppendTime;

    private String replyContent;
    private Integer replyTime;

    private Byte anonymous;
    private Byte status;
}
