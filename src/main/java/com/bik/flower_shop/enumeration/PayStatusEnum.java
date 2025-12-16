package com.bik.flower_shop.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bik
 */

@Getter
@AllArgsConstructor
public enum PayStatusEnum {

    UNPAID("unpaid", "未支付"),
    PAID("paid", "已支付"),
    REFUNDED("refunded", "已退款"),
    CLOSED("closed", "已关闭");

    private final String code;
    private final String desc;

    public static PayStatusEnum fromCode(String code) {
        for (PayStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法支付状态: " + code);
    }
}
