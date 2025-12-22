package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.OrderItemMapper;
import com.bik.flower_shop.mapper.UserMapper;
import com.bik.flower_shop.pojo.dto.CommentDTO;
import com.bik.flower_shop.pojo.entity.OrderItem;
import com.bik.flower_shop.pojo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final OrderItemMapper orderItemMapper;

    public Map<String, Object> getCommentList(Integer page, Integer limit, String keyword) {
        int offset = (page - 1) * limit;

        List<CommentDTO> list = orderItemMapper.selectCommentList(offset, limit, keyword);
        Integer totalCount = orderItemMapper.countComment(keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("totalCount", totalCount);
        return data;
    }

    /**
     * 回复或修改客服回复
     */
    public void replyComment(Integer commentId, String replyContent) {
        if (commentId == null || replyContent == null) {
            throw new IllegalArgumentException("评论ID或回复内容不能为空");
        }

        // 当前时间戳秒
        int now = (int) Instant.now().getEpochSecond();

        int updated = orderItemMapper.updateReply(commentId, replyContent, now);
        if (updated == 0) {
            throw new RuntimeException("评论不存在或已被删除");
        }
    }


    /**
     * 修改评论状态
     */
    public void updateCommentStatus(Integer commentId, Boolean status) {
        if (commentId == null || status == null) {
            throw new IllegalArgumentException("评论ID或状态不能为空");
        }

        int updated = orderItemMapper.updateStatus(commentId, status);
        if (updated == 0) {
            throw new RuntimeException("评论不存在或已被删除");
        }
    }

}
