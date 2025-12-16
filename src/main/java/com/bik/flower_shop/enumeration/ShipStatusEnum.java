package com.bik.flower_shop.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bik
 */

@Getter
@AllArgsConstructor
public enum ShipStatusEnum {

    PENDING("pending", "待发货"),
    SHIPPED("shipped", "已发货"),
    RECEIVED("received", "已收货");

    private final String code;
    private final String desc;

    public static ShipStatusEnum fromCode(String code) {
        for (ShipStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法物流状态: " + code);
    }
}
