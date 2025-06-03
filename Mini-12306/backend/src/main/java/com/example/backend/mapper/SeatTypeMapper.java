package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.model.SeatTypeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatTypeMapper extends BaseMapper<SeatTypeEntity> {
}