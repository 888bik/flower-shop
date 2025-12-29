package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author bik
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    private Integer id;
    // 会话ID
    private Integer sessionId;
    // 发送者ID
    private Integer senderId;
    // 1=用户, 2=客服
    private Integer senderRole;
    // 消息内容
    private String content;
    private Long createTime;
}
