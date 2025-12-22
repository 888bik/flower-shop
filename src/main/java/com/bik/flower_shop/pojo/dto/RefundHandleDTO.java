package com.bik.flower_shop.pojo.dto;


import lombok.Data;

/**
 * @author bik
 */
@Data
public class RefundHandleDTO {
    private Integer orderId;
    // true = 同意退款, false = 拒绝
    private Boolean agree;
    // 管理员备注（拒绝理由或同意时的说明）
    private String reason;

    /**
     * 退款类型：
     * only_refund      仅退款（未发货）
     * return_refund    退货退款（已发货）
     */
    private String refundType;
}