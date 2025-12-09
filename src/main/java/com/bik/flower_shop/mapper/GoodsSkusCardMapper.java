package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.dto.UpdateGoodsSkusOrderDTO;
import com.bik.flower_shop.pojo.entity.GoodsSkusCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface GoodsSkusCardMapper extends BaseMapper<GoodsSkusCard> {

    @Update({
            "<script>",
            "UPDATE goods_skus_card",
            "SET `order` = CASE id",
            "<foreach collection='sortdata' item='item'>",
            "WHEN #{item.id} THEN #{item.order} ",
            "</foreach>",
            "END",
            "WHERE id IN ",
            "<foreach collection='sortdata' item='item' open='(' separator=',' close=')'>",
            "#{item.id}",
            "</foreach>",
            "</script>"
    })
    int updateBatchOrder(@Param("sortdata") List<UpdateGoodsSkusOrderDTO.SortItem> sortdata);

}
