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
    PENDING("pending", "退款中"),
    AGREED("agreed", "已退款"),
    REJECTED("rejected", "已拒绝"),
    COMPLETED("completed", "退款完成");

    private final String code;
    private final String desc;

    public static RefundStatusEnum of(String code) {
        for (RefundStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法退款状态: " + code);
    }

    /**
     * 是否处于退款流程中（不可发货）
     */
    public boolean isProcessing() {
        return this == PENDING || this == AGREED;
    }
}
