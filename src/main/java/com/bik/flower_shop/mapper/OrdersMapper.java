package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.DailyCountDTO;
import com.bik.flower_shop.pojo.dto.HourlyCountDTO;
import com.bik.flower_shop.pojo.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    /**
     * 管理端批量软删除订单
     *
     * @param ids 订单ID列表
     * @return 影响的行数
     */
    @Update({
            "<script>",
            "UPDATE orders",
            "SET deleted_by_admin = 1",
            "WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int markDeletedByAdmin(@Param("ids") List<Integer> ids);

    @Update({
            "<script>",
            "UPDATE orders",
            "SET deleted_by_user = 1",
            "WHERE user_id = #{userId}",
            "AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int markDeletedByUser(@Param("userId") Integer userId,
                          @Param("ids") List<Integer> ids);

    /**
     * 统计指定支付状态的订单数量（排除被管理员删除的）
     */
    @Select("""
                SELECT COUNT(*)
                FROM orders
                WHERE pay_status = #{status}
                  AND deleted_by_admin = 0
            """)
    long countByPayStatus(@Param("status") String status);

    /**
     * 统计已支付且待发货的订单数量
     */
    @Select("""
                SELECT COUNT(*)
                FROM orders
                WHERE pay_status = 'paid'
                  AND ship_status = 'pending'
                  AND deleted_by_admin = 0
            """)
    long countPaidAndShipPending();

    /**
     * 按发货状态统计数量（排除被管理员删除的）
     */
    @Select("""
                SELECT COUNT(*)
                FROM orders
                WHERE ship_status = #{status}
                  AND deleted_by_admin = 0
            """)
    long countByShipStatus(@Param("status") String status);

    /**
     * 按退款状态统计数量（排除被管理员删除的）
     */
    @Select("""
                SELECT COUNT(*)
                FROM orders
                WHERE refund_status = #{status}
                  AND deleted_by_admin = 0
            """)
    long countByRefundStatus(@Param("status") String status);

    /**
     * 统计（近似）销售额：选取已支付且未最终退款的订单总额
     * 这里排除了 refund_status 已同意或已退款的订单（避免把已退款金额算入销售额）
     */
    @Select("""
                SELECT COALESCE(SUM(total_price), 0)
                FROM orders
                WHERE pay_status = 'paid'
                  AND (refund_status IS NULL OR refund_status NOT IN ('agreed', 'refunded'))
                  AND deleted_by_admin = 0
            """)
    BigDecimal sumTotalSales();

    /**
     * 最近 N 天订单数
     */
    @Select("""
                SELECT DATE_FORMAT(FROM_UNIXTIME(create_time), '%m-%d') AS day,
                       COUNT(*) AS cnt
                FROM orders
                WHERE create_time >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL #{days} DAY))
                  AND deleted_by_admin = 0
                GROUP BY day
            """)
    List<DailyCountDTO> countRecentDays(@Param("days") int days);

    /**
     * 今天每小时订单数
     */
    @Select("""
                SELECT DATE_FORMAT(FROM_UNIXTIME(create_time), '%H') AS hour,
                       COUNT(*) AS cnt
                FROM orders
                WHERE DATE(FROM_UNIXTIME(create_time)) = CURDATE()
                  AND deleted_by_admin = 0
                GROUP BY hour
            """)
    List<HourlyCountDTO> countTodayByHour();
}
