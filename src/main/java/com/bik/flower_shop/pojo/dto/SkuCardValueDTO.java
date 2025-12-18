package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author bik
 */
@Data
public class SkuCardValueDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String value;
}