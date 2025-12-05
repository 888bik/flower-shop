package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Skus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface SkusMapper extends BaseMapper<Skus> {

    @Select("SELECT * FROM skus ORDER BY `order` DESC LIMIT #{offset}, #{pageSize}")
    List<Skus> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM skus")
    int countAll();
}
