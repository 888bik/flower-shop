package com.bik.flower_shop.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bik
 */

@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    NONE("none", "无退款"),
    // 用户申请，仅退款或待商家审核
    PENDING("pending", "待商家处理"),
    // 已同意仅退款
    AGREED("agreed", "商家已同意，仅退款完成中"),
    RETURN_REQUESTED("return_requested", "用户已申请退货"),
    RETURNING("returning", "用户已寄回"),
    RETURNED("returned", "商家已收到退货"),
    COMPLETED("completed", "退款完成"),
    REJECTED("rejected", "退款被拒");

    private final String code;
    private final String desc;

    public static RefundStatusEnum of(String code) {
        if (code == null || code.isBlank()) {
            return NONE;
        }
        for (RefundStatusEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法退款状态: " + code);
    }


    /**
     * 是否处于退款流程中（不可发货）
     */
    public boolean isProcessing() {
        return this == PENDING
                || this == AGREED
                || this == RETURN_REQUESTED
                || this == RETURNING
                || this == RETURNED;
    }
}
