package com.bik.flower_shop.pojo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class SetRoleRulesDTO {
    private Integer id;

    @JsonAlias({"ruleIds", "rule_ids"})
    private List<Integer> ruleIds;
}
