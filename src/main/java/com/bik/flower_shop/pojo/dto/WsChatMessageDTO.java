package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class WsChatMessageDTO {
    private String type;
    private Integer sessionId;
    private String content;
}
