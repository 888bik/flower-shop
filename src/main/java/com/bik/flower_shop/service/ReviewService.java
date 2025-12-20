package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.enumeration.ReviewStatusEnum;
import com.bik.flower_shop.mapper.OrderItemMapper;
import com.bik.flower_shop.mapper.UserMapper;
import com.bik.flower_shop.pojo.dto.ReviewDTO;
import com.bik.flower_shop.pojo.dto.ReviewPageResponse;
import com.bik.flower_shop.pojo.dto.ReviewerDTO;
import com.bik.flower_shop.pojo.entity.OrderItem;
import com.bik.flower_shop.pojo.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取商品评论（分页）
     *
     * @param goodsId  商品 id
     * @param page     当前页（1-based）
     * @param pageSize 每页大小
     * @param filter   过滤器: "withPhotos" | "positive" | "negative" | null
     * @return ReviewPageResponse
     */
    public ReviewPageResponse getReviews(Integer goodsId, int page, int pageSize, String filter) {
        Page<OrderItem> pg = new Page<>(page, pageSize);

        LambdaQueryWrapper<OrderItem> qw = new LambdaQueryWrapper<>();
        qw.eq(OrderItem::getGoodsId, goodsId) // 必须加上
                .in(OrderItem::getReviewStatus,
                        ReviewStatusEnum.REVIEWED.getCode(),
                        ReviewStatusEnum.APPENDED.getCode()
                );

        if ("withPhotos".equals(filter)) {
            qw.isNotNull(OrderItem::getReviewImages).ne(OrderItem::getReviewImages, "");
        } else if ("positive".equals(filter)) {
            qw.ge(OrderItem::getRating, 4);
        } else if ("negative".equals(filter)) {
            qw.le(OrderItem::getRating, 2);
        }

        IPage<OrderItem> pageRes = orderItemMapper.selectPage(pg, qw);

        // 批量查询用户
        List<Integer> userIds = pageRes.getRecords().stream()
                .map(OrderItem::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        } else {
            userMap = new HashMap<>();
        }

        List<ReviewDTO> dtos = pageRes.getRecords().stream().map(oi -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(oi.getId());
            dto.setRating(oi.getRating());
            dto.setContent(oi.getReview());
            dto.setAnonymous(Boolean.TRUE.equals(oi.getAnonymous()));
            // time: if reviewTime is seconds -> convert to millis. If it's already ms adjust as needed.
            Integer rt = oi.getReviewTime();
            if (rt != null) {
                long epochMilli = (rt < 1_000_000_000L) ? (rt.longValue() * 1000L) : rt.longValue();
                dto.setReviewTime(epochMilli);
            }

            String imgs = oi.getReviewImages();
            List<String> photos = new ArrayList<>();
            if (StringUtils.hasText(imgs)) {
                try {
                    if (imgs.trim().startsWith("[")) {
                        photos = objectMapper.readValue(imgs, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    } else {
                        // comma separated
                        photos = Arrays.stream(imgs.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                    }
                } catch (Exception ex) {
                    photos = new ArrayList<>();
                }
            }
            dto.setPhotos(photos);

            User u = userMap.get(oi.getUserId());
            ReviewerDTO rd = new ReviewerDTO();
            if (dto.getAnonymous()) {
                rd.setId(null);
                rd.setNickname("匿名用户");
                rd.setAvatar(null);
            } else if (u != null) {
                rd.setId(u.getId());
                rd.setNickname(u.getNickname() != null ? u.getNickname() : u.getUsername());
                rd.setAvatar(u.getAvatar());
            } else {
                rd.setId(oi.getUserId());
                rd.setNickname("用户");
                rd.setAvatar(null);
            }
            dto.setUser(rd);

            return dto;
        }).collect(Collectors.toList());

        // 统计：总数 & avgRating
        long total = pageRes.getTotal();
        Double avg = orderItemMapper.selectAvgRating(goodsId);
        double avgRating = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;

        ReviewPageResponse resp = new ReviewPageResponse();
        resp.setTotal(total);
        resp.setPage(page);
        resp.setPageSize(pageSize);
        resp.setAvgRating(avgRating);
        resp.setList(dtos);

        return resp;
    }
}
