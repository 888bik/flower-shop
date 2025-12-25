package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.GoodsBanner;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@Mapper
public interface GoodsBannerMapper extends BaseMapper<GoodsBanner> {

    @Delete("DELETE FROM goods_banner WHERE goods_id = #{goodsId}")
    int deleteByGoodsId(@Param("goodsId") Integer goodsId);

    @Select("""
                select goods_id, url
                from goods_banner
                where goods_id in (${goodsIds})
                order by goods_id
            """)
    List<Map<String, Object>> selectByGoodsIds(
            @Param("goodsIds") String goodsIds
    );

}