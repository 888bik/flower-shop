package com.bik.flower_shop.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author bik
 */
@Data
public class RuleDTO {

    private Integer ruleId;
    private Byte menu;
    private String name;
    private String condition;
    private String method;
    private Byte status;
    private Integer order;
    private String icon;
    private String frontpath;
}
