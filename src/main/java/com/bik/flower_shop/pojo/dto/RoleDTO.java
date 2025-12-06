package com.bik.flower_shop.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoleDTO {

    private Integer id;

    private String name;

    private Byte status;

    @JsonProperty("desc")
    private String description;
}
