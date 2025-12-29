package com.bik.flower_shop.pojo.dto;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class ChatSessionDTO {

    private Integer id;           // 会话ID
    private Integer userId;       // 用户ID
    private Integer serviceId;    // 管理员ID
    private Integer status;       // 状态
    private Integer createTime;
    private Integer updateTime;

    private String nickname;      // 用户昵称
    private String avatar;        // 用户头像
}
