package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.model.PassengerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface PassengerMapper extends BaseMapper<PassengerEntity> {
    
    /**
     * 根据用户ID查询乘车人列表
     */
    @Select("SELECT * FROM passenger WHERE user_id = #{userId} ORDER BY is_default DESC, create_time ASC")
    List<PassengerEntity> selectByUserId(String userId);
    
    /**
     * 根据用户ID和身份证号查询乘车人
     */
    @Select("SELECT * FROM passenger WHERE user_id = #{userId} AND id_card = #{idCard}")
    PassengerEntity selectByUserIdAndIdCard(String userId, String idCard);
    
    /**
     * 取消用户的所有默认乘车人
     */
    @Update("UPDATE passenger SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultPassenger(String userId);
    
    /**
     * 设置默认乘车人
     */
    @Update("UPDATE passenger SET is_default = 1 WHERE id = #{passengerId}")
    int setDefaultPassenger(String passengerId);
    
    /**
     * 根据用户ID获取默认乘车人
     */
    @Select("SELECT * FROM passenger WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    PassengerEntity selectDefaultByUserId(String userId);
}