package com.bik.flower_shop.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author bik
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {
    private final int errorCode;

    public BusinessException(String msg) {
        super(msg);
        this.errorCode = 400;
    }

    public BusinessException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

}
