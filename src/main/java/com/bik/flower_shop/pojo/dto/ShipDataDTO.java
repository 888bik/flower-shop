package com.bik.flower_shop.pojo.dto;

import com.bik.flower_shop.pojo.dto.CompanyDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipDataDTO {

    private CompanyDTO company;

    @JsonProperty("trackingNo")
    private String trackingNo;

    private Map<String, Object> shipping;

    private List<TrackEventDTO> history;

}
