package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class TrackEvent {
    private String time;
    private String status;
    private String location;
    private String desc;

    public TrackEvent(String time, String status, String location, String desc) {
        this.time = time;
        this.status = status;
        this.location = location;
        this.desc = desc;
    }
}