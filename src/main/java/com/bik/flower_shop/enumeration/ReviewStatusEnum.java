package com.bik.flower_shop.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单商品评价状态
 * 0 未评价
 * 1 已评价
 * 2 已追评
 * @author bik
 */
@Getter
@AllArgsConstructor
public enum ReviewStatusEnum {

    NOT_REVIEWED(0, "未评价"),
    REVIEWED(1, "已评价"),
    APPENDED(2, "已追评");

    private final int code;
    private final String desc;

    public static ReviewStatusEnum of(Integer code) {
        if (code == null) {
            return NOT_REVIEWED;
        }
        for (ReviewStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return NOT_REVIEWED;
    }

    public boolean isReviewed() {
        return this == REVIEWED || this == APPENDED;
    }

    public boolean canAppend() {
        return this == REVIEWED;
    }
}
