package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.UserLevel;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author: bik
 * Date: 2025-12-13
 * Time: 16:31
 */
@Mapper
public interface UserLevelMapper extends BaseMapper<UserLevel> {

    @Select("""
                SELECT *
                FROM user_level
                WHERE status = 1
                  AND max_price <= #{totalPrice}
                  OR max_times <= #{orderCount}
                ORDER BY level DESC
                LIMIT 1
            """)
    UserLevel selectMatchedLevel(@Param("totalPrice") BigDecimal totalPrice,
                                 @Param("orderCount") Integer orderCount);

    @Select("""
                <script>
                    SELECT COUNT(*) FROM user_level
                    WHERE 1=1
                    <if test="keyword != null and keyword != ''">
                        AND name LIKE CONCAT('%', #{keyword}, '%')
                    </if>
                </script>
            """)
    int countLevelList(@Param("keyword") String keyword);

    @Select("""
                <script>
                    SELECT * FROM user_level
                    WHERE 1=1
                    <if test="keyword != null and keyword != ''">
                        AND name LIKE CONCAT('%', #{keyword}, '%')
                    </if>
                    ORDER BY level ASC
                    LIMIT #{offset}, #{limit}
                </script>
            """)
    List<UserLevel> selectLevelList(@Param("offset") int offset,
                                    @Param("limit") int limit,
                                    @Param("keyword") String keyword);


    @Insert("""
                INSERT INTO user_level(name, level, status, discount, max_price, max_times)
                VALUES(#{name}, #{level}, #{status}, #{discount}, #{maxPrice}, #{maxTimes})
            """)
    void insertUserLevel(UserLevel userLevel);

    @Update("""
                UPDATE user_level
                SET name = #{name},
                    level = #{level},
                    status = #{status},
                    discount = #{discount},
                    max_price = #{maxPrice},
                    max_times = #{maxTimes}
                WHERE id = #{id}
            """)
    void updateUserLevel(UserLevel userLevel);

    @Delete("DELETE FROM user_level WHERE id = #{id}")
    void deleteUserLevel(Integer id);

    @Update("UPDATE user_level SET status = #{status} WHERE id = #{id}")
    void updateUserLevelStatus(@Param("id") Integer id, @Param("status") Byte status);

}
