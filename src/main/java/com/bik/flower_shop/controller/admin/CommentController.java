package com.bik.flower_shop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.ReplyRequest;
import com.bik.flower_shop.pojo.dto.StatusRequest;
import com.bik.flower_shop.pojo.entity.OrderItem;
import com.bik.flower_shop.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/comments")
public class CommentController {

    private final CommentService commentService;


    @GetMapping("/list")
    public ApiResult<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.ok(commentService.getCommentList(page, size, keyword));
    }

    @PostMapping("/reply")
    public ApiResult<Void> reply(@RequestBody ReplyRequest request) {
        commentService.replyComment(request.getCommentId(), request.getReplyContent());
        return ApiResult.ok();
    }

    /**
     * 修改评论状态
     */
    @PostMapping("/status")
    public ApiResult<Void> updateStatus(@RequestBody StatusRequest request) {
        commentService.updateCommentStatus(request.getCommentId(), request.getStatus());
        return ApiResult.ok();
    }
}
