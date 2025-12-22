package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.UserListDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.pojo.vo.UserSimpleVO;
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
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT id, username, nickname, avatar FROM user WHERE id = #{id}")
    UserSimpleVO selectSimpleById(Integer id);

    @Select("""
                SELECT
                    u.id,
                    u.username,
                    u.nickname,
                    u.avatar,
                    u.phone,
                    u.email,
                    u.status,
                    u.user_level_id AS userLevelId,
                    ul.name AS userLevelName,
                    u.share_num,
                    u.share_order_num,
                    u.order_price,
                    u.commission,
                    u.create_time
                FROM user u
                LEFT JOIN user_level ul ON u.user_level_id = ul.id
                WHERE
                    (#{keyword} IS NULL
                     OR u.username LIKE CONCAT('%', #{keyword}, '%')
                     OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
                     OR u.phone LIKE CONCAT('%', #{keyword}, '%'))
                ORDER BY u.id DESC
                LIMIT #{offset}, #{limit}
            """)
    List<UserListDTO> selectUserList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("keyword") String keyword
    );

    @Select("""
                SELECT COUNT(*)
                FROM user u
                WHERE
                    (#{keyword} IS NULL
                     OR u.username LIKE CONCAT('%', #{keyword}, '%')
                     OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
                     OR u.phone LIKE CONCAT('%', #{keyword}, '%'))
            """)
    Integer countUser(@Param("keyword") String keyword);


    @Update("""
                UPDATE user
                SET order_price = IFNULL(order_price, 0) + #{price}
                WHERE id = #{userId}
            """)
    int increaseOrderPrice(@Param("userId") Integer userId,
                           @Param("price") BigDecimal price);

    // 累加消费次数
    @Update("""
                UPDATE user 
                SET order_count = order_count + 1
                WHERE id = #{userId}
            """)
    void increaseOrderCount(@Param("userId") Integer userId);
}
