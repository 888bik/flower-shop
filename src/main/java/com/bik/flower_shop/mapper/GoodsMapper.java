package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.pojo.dto.GoodsQueryDTO;
import com.bik.flower_shop.pojo.entity.Goods;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    // 查询商品详情，同时查多分类
    @Select("SELECT g.* " +
            "FROM goods g " +
            "WHERE g.id = #{goodsId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "categories", javaType = List.class, column = "id",
                    many = @Many(select = "com.bik.flower_shop.mapper.CategoryMapper.selectByGoodsId"))
    })
    Goods selectGoodsWithCategories(@Param("goodsId") Integer goodsId);

    @Update("UPDATE goods SET like_count = like_count + 1 WHERE id = #{goodsId}")
    void incrementLikeCount(@Param("goodsId") Integer goodsId);

    @Update("UPDATE goods SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{goodsId}")
    void decrementLikeCount(@Param("goodsId") Integer goodsId);

    @Select("SELECT like_count FROM goods WHERE id = #{goodsId}")
    Integer selectLikeCount(@Param("goodsId") Integer goodsId);

    @Update("""
                UPDATE goods
                SET sale_count = IFNULL(sale_count, 0) + #{num}
                WHERE id = #{goodsId}
            """)
    void increaseSaleCount(@Param("goodsId") Integer goodsId,
                           @Param("num") Integer num);


    // 审核中
    @Select("""
                SELECT COUNT(*)
                FROM goods
                WHERE ischeck = 0
            """)
    Long countPending();

    // 销售中
    @Select("""
                SELECT COUNT(*)
                FROM goods
                WHERE ischeck = 1
                  AND status = 1
            """)
    Long countOnSale();

    // 已下架
    @Select("""
                SELECT COUNT(*)
                FROM goods
                WHERE ischeck = 1
                  AND status = 0
            """)
    Long countOffSale();

    // 库存预警
    @Select("""
                SELECT COUNT(*)
                FROM goods
                WHERE ischeck = 1
                  AND status = 1
                  AND stock <= min_stock
            """)
    Long countLowStock();


    @Select("""
            SELECT *
            FROM goods
            WHERE status = 1
              AND ischeck = 1
              AND category_id = #{categoryId}
            ORDER BY `order` DESC, id DESC
            LIMIT #{limit}
            """)
    List<Goods> selectHomeGoods(
            @Param("categoryId") Integer categoryId,
            @Param("limit") Integer limit
    );


}