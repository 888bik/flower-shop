package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.GoodsCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {
    @Delete("DELETE FROM goods_category WHERE goods_id = #{goodsId}")
    int deleteByGoodsId(@Param("goodsId") Integer goodsId);

    @Select("SELECT category_id FROM goods_category WHERE goods_id = #{goodsId}")
    List<Integer> selectCategoryIdsByGoodsId(@Param("goodsId") Integer goodsId);

    @Select({
            "<script>",
            "SELECT goods_id, category_id FROM goods_category",
            "WHERE goods_id IN",
            "<foreach collection='goodsIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<GoodsCategory> selectByGoodsIds(@Param("goodsIds") List<Integer> goodsIds);

    @Select("SELECT goods_id FROM goods_category WHERE category_id = #{categoryId}")
    List<Integer> selectGoodsIdsByCategoryId(@Param("categoryId") Integer categoryId);

    @Insert({
            "<script>",
            "INSERT INTO goods_category (goods_id, category_id) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.goodsId}, #{item.categoryId})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(@Param("list") List<GoodsCategory> list);

    /**
     * 根据多个 categoryId 查询关联的 distinct goods_id 列表
     */
    @Select({
            "<script>",
            "select distinct goods_id from goods_category",
            "where category_id in",
            "<foreach collection='list' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<Integer> selectGoodsIdsByCategoryIds(@Param("list") List<Integer> categoryIds);
}
