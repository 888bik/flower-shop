package com.bik.flower_shop.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.GoodsBanner;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author bik
 */
@Mapper
public interface GoodsBannerMapper extends BaseMapper<GoodsBanner> {

    @Delete("DELETE FROM goods_banner WHERE goods_id = #{goodsId}")
    int deleteByGoodsId(@Param("goodsId") Integer goodsId);

}