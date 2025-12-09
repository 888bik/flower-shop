package com.bik.flower_shop.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.GoodsSkus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
/**
 * @author bik
 */
@Mapper
public interface GoodsSkusMapper extends BaseMapper<GoodsSkus> {
    @Delete("DELETE FROM goods_skus WHERE goods_id = #{goodsId}")
    int deleteByGoodsId(Integer goodsId);
}