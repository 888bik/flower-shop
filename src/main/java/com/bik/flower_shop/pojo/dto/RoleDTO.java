package com.bik.flower_shop.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class RoleDTO {

    private String name;

    private Byte status;

    @JsonProperty("desc")
    private String description;
}
