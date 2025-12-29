package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author bik
 */
@Data
@TableName("chat_session")
public class ChatSession {

    private Integer id;
    private Integer userId;
    private Integer serviceId;
    private Integer status;
    private Integer createTime;
    private Integer updateTime;
}
