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

    Page<Goods> selectGoods(Page<?> page, @Param("dto") GoodsQueryDTO dto);

    Long countGoods(@Param("dto") GoodsQueryDTO dto);
}