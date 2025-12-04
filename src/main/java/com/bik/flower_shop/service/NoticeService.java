package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.NoticeMapper;
import com.bik.flower_shop.pojo.entity.Notice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    /**
     * 添加公告
     */
    public Notice addNotice(String title, String content) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        int now = (int) Instant.now().getEpochSecond();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);

        if (notice.getOrder() == null) {
            notice.setOrder(50);
        }
        noticeMapper.insert(notice);
        return notice;
    }

    public Map<String, Object> listNotices(int page, int limit) {
        int offset = (page - 1) * limit;
        List<Notice> list = noticeMapper.selectPage(limit, offset);
        int totalCount = noticeMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    public boolean updateNotice(Integer id, String title, String content) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return false;
        }
        notice.setTitle(title);
        notice.setContent(content);
        notice.setUpdateTime((int) Instant.now().getEpochSecond());
        return noticeMapper.updateById(notice) > 0;
    }

    // 删除公告
    public boolean deleteNotice(Integer id) {
        return noticeMapper.deleteById(id) > 0;
    }

}
