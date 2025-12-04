package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Image;
import com.bik.flower_shop.pojo.entity.ImageClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImageClassMapper extends BaseMapper<ImageClass> {

    // 查询某个分类下的图片数量
    @Select("SELECT COUNT(*) FROM image WHERE image_class_id = #{classId}")
    Integer countImagesByClassId(@Param("classId") Integer classId);


}
