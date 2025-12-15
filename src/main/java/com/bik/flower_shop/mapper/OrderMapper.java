package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
