package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    @Select("SELECT c.id, c.name " +
            "FROM category c " +
            "INNER JOIN goods_category gc ON c.id = gc.category_id " +
            "WHERE gc.goods_id = #{goodsId}")
    List<Category> selectByGoodsId(@Param("goodsId") Integer goodsId);
}