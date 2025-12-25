package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.pojo.dto.NoticeDTO;
import com.bik.flower_shop.pojo.entity.Notice;
import com.bik.flower_shop.service.NoticeService;
import com.bik.flower_shop.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RequiredArgsConstructor
@RestController
@AuthRequired(role = "admin")
@RequestMapping("/admin/notice")
public class NoticeController {

    private final NoticeService noticeService;


    /**
     * 添加公告
     */
    @PostMapping
    public ApiResult<Notice> createNotice(@RequestBody NoticeDTO dto) {

        Notice notice = noticeService.addNotice(dto.getTitle(), dto.getContent());
        return ApiResult.ok(notice);
    }

    /**
     * 公告列表
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> list(@PathVariable int page,
                                               @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = noticeService.listNotices(page, limit);
        return ApiResult.ok(result);
    }

    /**
     * 修改公告
     */
    @PostMapping("/{id}")
    public ApiResult<Boolean> updateNotice(@PathVariable Integer id,
                                           @RequestBody NoticeDTO dto) {
        boolean success = noticeService.updateNotice(id, dto.getTitle(), dto.getContent());
        return ApiResult.ok(success);
    }

    /**
     * 删除公告
     */
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteNotice(@PathVariable Integer id) {
        boolean success = noticeService.deleteNotice(id);
        return ApiResult.ok(success);
    }

}
