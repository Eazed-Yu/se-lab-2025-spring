package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.model.TrainScheduleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainScheduleMapper extends BaseMapper<TrainScheduleEntity> {
}