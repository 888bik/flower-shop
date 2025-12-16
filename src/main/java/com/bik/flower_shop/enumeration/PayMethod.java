package com.bik.flower_shop.enumeration;

/**
 * @author bik
 */

public enum PayMethod {
    WECHAT,
    ALIPAY;

    public static PayMethod fromString(String method) {
        if (method == null) {
            return null;
        }
        try {
            return PayMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
