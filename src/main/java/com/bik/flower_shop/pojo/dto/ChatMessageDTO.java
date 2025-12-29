package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class ChatMessageDTO {
    private Integer id;
    private Integer sessionId;
    private Integer senderId;
    private Integer senderRole;
    private String content;
    private Long createTime;

    // 用户信息
    private String nickname;
    private String avatar;
}
