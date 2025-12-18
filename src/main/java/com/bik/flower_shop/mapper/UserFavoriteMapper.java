package com.bik.flower_shop.mapper;

import com.bik.flower_shop.pojo.entity.Goods;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface UserFavoriteMapper {

    // 判断是否存在
    @Select("SELECT COUNT(1) FROM user_favorite WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    boolean exists(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);

    // 添加收藏
    @Insert("INSERT INTO user_favorite(user_id, goods_id) VALUES(#{userId}, #{goodsId})")
    void insert(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);

    // 删除收藏
    @Delete("DELETE FROM user_favorite WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    void delete(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);

    // 返回受影响行数：如果已存在返回0；MySQL 用 INSERT IGNORE 或 INSERT ... ON DUPLICATE KEY
    @Insert("INSERT IGNORE INTO user_favorite(user_id, goods_id) VALUES(#{userId}, #{goodsId})")
    int insertIfNotExists(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);

    // 查询用户收藏的商品详细信息（可直接关联 goods 表）
    @Select("SELECT g.* FROM goods g " +
            "JOIN user_favorite uf ON g.id = uf.goods_id " +
            "WHERE uf.user_id = #{userId} " +
            "ORDER BY uf.create_time DESC")
    List<Goods> selectFavoriteGoods(@Param("userId") Integer userId);

    @Delete("DELETE FROM user_favorite WHERE user_id=#{userId} AND goods_id=#{goodsId}")
    int deleteByUserAndGoods(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
}
