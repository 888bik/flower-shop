package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.CommentDTO;
import com.bik.flower_shop.pojo.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    @Select("""
                SELECT
                    id,
                    order_id,
                    goods_id,
                    goods_name AS goodsTitle,
                    goods_cover AS goodsCover,
                    num,
                    price,
                    rating,
                    review,
                    review_time,
                    user_id,
                    status,
                    create_time
                FROM order_item
                WHERE order_id = #{orderId}
            """)
    List<OrderItem> selectByOrderId(@Param("orderId") Integer orderId);

    @Select("SELECT COUNT(*) FROM order_item WHERE order_id = #{orderId}")
    int countByOrderId(@Param("orderId") Integer orderId);

    @Select("SELECT COUNT(*) FROM order_item WHERE order_id = #{orderId} AND (review_time IS NOT NULL OR rating IS NOT NULL)")
    int countReviewedByOrderId(@Param("orderId") Integer orderId);

    // 获取某商品的平均评分（只计算已评价 review_status = 1）
    @Select("SELECT AVG(rating) FROM order_item WHERE goods_id = #{goodsId} AND review_status = 1")
    Double selectAvgRating(@Param("goodsId") Integer goodsId);

    // 获取某商品的评论总数（已评价）
    @Select("SELECT COUNT(1) FROM order_item WHERE goods_id = #{goodsId} AND review_status = 1")
    Long selectReviewCount(@Param("goodsId") Integer goodsId);


    @Select("""
                SELECT 
                    oi.id,
                    oi.order_id AS orderId,
                    oi.goods_id AS goodsId,
                    oi.goods_name AS goodsTitle,
                    oi.goods_cover AS goodsCover,
                    oi.user_id AS userId,
                    u.nickname,
                    u.avatar,
                    oi.review,
                    oi.rating,
                    oi.review_images AS reviewImages,
                    oi.review_append AS reviewAppend,
                    oi.review_time AS reviewTime,
                    oi.review_append_time AS reviewAppendTime,
                    oi.reply_content AS replyContent,
                    oi.review_status AS reviewStatus,
                    oi.reply_time AS replyTime,
                    oi.anonymous,
                    oi.status
                FROM order_item oi
                LEFT JOIN user u ON oi.user_id = u.id
                WHERE oi.review IS NOT NULL AND oi.review <> ''
                AND (#{keyword} IS NULL OR oi.goods_name LIKE CONCAT('%', #{keyword}, '%'))
                ORDER BY oi.review_time DESC
                LIMIT #{offset}, #{limit}
            """)
    List<CommentDTO> selectCommentList(@Param("offset") int offset,
                                       @Param("limit") int limit,
                                       @Param("keyword") String keyword);

    @Select("""
                SELECT COUNT(*) 
                FROM order_item oi
                WHERE oi.review IS NOT NULL AND oi.review <> ''
                AND (#{keyword} IS NULL OR oi.goods_name LIKE CONCAT('%', #{keyword}, '%'))
            """)
    Integer countComment(@Param("keyword") String keyword);

    /**
     * 更新评论的客服回复内容
     */
    @Update("""
                UPDATE order_item
                SET reply_content = #{replyContent},
                    reply_time = #{replyTime}
                WHERE id = #{commentId}
            """)
    int updateReply(@Param("commentId") Integer commentId,
                    @Param("replyContent") String replyContent,
                    @Param("replyTime") Integer replyTime);


    /**
     * 更新评论状态
     */
    @Update("""
                UPDATE order_item
                SET review_status = #{status}
                WHERE id = #{commentId}
            """)
    int updateStatus(@Param("commentId") Integer commentId,
                     @Param("status") Boolean status);
}
