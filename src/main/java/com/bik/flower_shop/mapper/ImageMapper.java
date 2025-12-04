package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {

    @Select("SELECT * FROM image WHERE image_class_id = #{classId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Image> selectByClassIdPage(@Param("classId") Integer classId,
                                    @Param("limit") int limit,
                                    @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM image WHERE image_class_id = #{classId}")
    Integer countByClassId(@Param("classId") Integer classId);
}
