package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.model.SeatAvailabilityEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatAvailabilityMapper extends BaseMapper<SeatAvailabilityEntity> {
}