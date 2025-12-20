package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bik.flower_shop.enumeration.ReviewStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单id
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 购买数量
     */
    @TableField("num")
    private Integer num;

    /**
     * 购买价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 评分
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价
     */
    @TableField("review")
    private String review;

    /**
     * 评价时间
     */
    @TableField("review_time")
    private Integer reviewTime;

    @TableField("create_time")
    private Integer createTime;

    @TableField("review_status")
    private Integer reviewStatus;

    @TableField("anonymous")
    private Boolean anonymous;

    @TableField("review_images")
    private String reviewImages;

    @TableField("review_append")
    private String reviewAppend;

    @TableField("review_append_time")
    private Integer reviewAppendTime;

    @TableField("reply_content")
    private String replyContent;

    @TableField("reply_time")
    private Integer replyTime;

    /**
     * 规格类型
     */
    @TableField("skus_type")
    private Byte skusType;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("goods_num")
    private Integer goodsNum;

    @TableField("user_id")
    private Integer userId;

    @TableField("extra")
    private String extra;

    /**
     * 是否可用,1：可用，0不可用
     */
    @TableField("status")
    private Boolean status;

    @TableField("goods_name")
    private String goodsTitle;

    @TableField("goods_cover")
    private String goodsCover;


    public ReviewStatusEnum getReviewStatusEnum() {
        return ReviewStatusEnum.of(this.reviewStatus);
    }

    public void setReviewStatusEnum(ReviewStatusEnum status) {
        this.reviewStatus = status.getCode();
    }
}
