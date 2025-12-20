package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
