package com.bik.flower_shop.pojo.dto;

import com.bik.flower_shop.pojo.dto.TrackEvent;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@Data
public class ShipData {
    private Map<String, Object> company;
    private String trackingNo;
    private Map<String, Object> shipping;
    private List<TrackEvent> history;
    private Long shippedTime;
}