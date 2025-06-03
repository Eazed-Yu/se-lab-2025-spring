package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.dto.TrainScheduleDTO;
import com.example.backend.mapper.SeatAvailabilityMapper;
import com.example.backend.mapper.SeatTypeMapper;
import com.example.backend.mapper.TrainScheduleMapper;
import com.example.backend.model.SeatAvailabilityEntity;
import com.example.backend.model.SeatTypeEntity;
import com.example.backend.model.TrainScheduleEntity;
import com.example.backend.service.TrainScheduleService;
import com.example.backend.util.EntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 列车时刻表服务实现类
 */
@Service
public class TrainScheduleServiceImpl implements TrainScheduleService {

    @Autowired
    private TrainScheduleMapper trainScheduleMapper;
    
    @Autowired
    private SeatTypeMapper seatTypeMapper;
    
    @Autowired
    private SeatAvailabilityMapper seatAvailabilityMapper;

    /**
     * 查询列车时刻表
     */
    @Override
    public List<TrainScheduleDTO> querySchedules(String departureStation, String arrivalStation, LocalDate departureDate) {
        // 构建查询条件
        QueryWrapper<TrainScheduleEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(departureStation)) {
            queryWrapper.eq("departure_station", departureStation);
        }
        if (StringUtils.hasText(arrivalStation)) {
            queryWrapper.eq("arrival_station", arrivalStation);
        }
        if (departureDate != null) {
            queryWrapper.ge("departure_time", departureDate.atStartOfDay());
            queryWrapper.lt("departure_time", departureDate.plusDays(1).atStartOfDay());
        }

        // 查询车次
        List<TrainScheduleEntity> schedules = trainScheduleMapper.selectList(queryWrapper);
        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有座位类型
        List<SeatTypeEntity> seatTypes = seatTypeMapper.selectList(null);

        // 查询每个车次的座位可用性
        List<TrainScheduleDTO> result = new ArrayList<>();
        for (TrainScheduleEntity schedule : schedules) {
            List<SeatAvailabilityEntity> availabilities = seatAvailabilityMapper.selectList(
                    new QueryWrapper<SeatAvailabilityEntity>().eq("schedule_id", schedule.getId())
            );
            result.add(EntityConverter.toTrainScheduleDTO(schedule, availabilities, seatTypes));
        }

        return result;
    }

    /**
     * 根据ID查询列车时刻表
     */
    @Override
    public TrainScheduleDTO getScheduleById(String scheduleId) {
        TrainScheduleEntity schedule = trainScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return null;
        }
        
        List<SeatTypeEntity> seatTypes = seatTypeMapper.selectList(null);
        List<SeatAvailabilityEntity> availabilities = seatAvailabilityMapper.selectList(
                new QueryWrapper<SeatAvailabilityEntity>().eq("schedule_id", scheduleId)
        );
        
        return EntityConverter.toTrainScheduleDTO(schedule, availabilities, seatTypes);
    }

    /**
     * 添加列车时刻表
     */
    @Override
    @Transactional
    public String addSchedule(TrainScheduleEntity schedule, List<SeatTypeEntity> seatTypes) {
        // 设置时刻表ID
        String scheduleId = UUID.randomUUID().toString().replace("-", "");
        schedule.setId(scheduleId);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);
        schedule.setStatus("正常");
        
        // 保存时刻表
        trainScheduleMapper.insert(schedule);
        
        // 保存座位可用性
        for (SeatTypeEntity seatType : seatTypes) {
            SeatAvailabilityEntity seatAvailability = new SeatAvailabilityEntity();
            seatAvailability.setScheduleId(scheduleId);
            seatAvailability.setSeatTypeId(seatType.getId());
            
            // 根据座位类型设置默认可用数量和价格
            switch (seatType.getName()) {
                case "商务座":
                    seatAvailability.setAvailableCount(20);
                    seatAvailability.setPrice(seatType.getBasePrice().multiply(new BigDecimal("1.5")));
                    break;
                case "一等座":
                    seatAvailability.setAvailableCount(60);
                    seatAvailability.setPrice(seatType.getBasePrice().multiply(new BigDecimal("1.2")));
                    break;
                case "二等座":
                    seatAvailability.setAvailableCount(120);
                    seatAvailability.setPrice(seatType.getBasePrice());
                    break;
                case "硬卧":
                    seatAvailability.setAvailableCount(90);
                    seatAvailability.setPrice(seatType.getBasePrice().multiply(new BigDecimal("0.8")));
                    break;
                case "硬座":
                    seatAvailability.setAvailableCount(150);
                    seatAvailability.setPrice(seatType.getBasePrice().multiply(new BigDecimal("0.6")));
                    break;
                default:
                    seatAvailability.setAvailableCount(100);
                    seatAvailability.setPrice(seatType.getBasePrice());
            }
            
            seatAvailability.setCreateTime(now);
            seatAvailability.setUpdateTime(now);
            
            seatAvailabilityMapper.insert(seatAvailability);
        }
        
        return scheduleId;
    }

    /**
     * 更新列车时刻表
     */
    @Override
    public boolean updateSchedule(TrainScheduleEntity schedule) {
        // 检查时刻表是否存在
        TrainScheduleEntity existingSchedule = trainScheduleMapper.selectById(schedule.getId());
        if (existingSchedule == null) {
            return false;
        }
        
        // 设置更新时间
        schedule.setUpdateTime(LocalDateTime.now());
        
        // 更新时刻表
        trainScheduleMapper.updateById(schedule);
        
        return true;
    }

    /**
     * 删除列车时刻表
     */
    @Override
    @Transactional
    public boolean deleteSchedule(String scheduleId) {
        // 检查时刻表是否存在
        TrainScheduleEntity existingSchedule = trainScheduleMapper.selectById(scheduleId);
        if (existingSchedule == null) {
            return false;
        }
        
        // 删除座位可用性
        seatAvailabilityMapper.delete(
                new QueryWrapper<SeatAvailabilityEntity>().eq("schedule_id", scheduleId)
        );
        
        // 删除时刻表
        trainScheduleMapper.deleteById(scheduleId);
        
        return true;
    }

    /**
     * 获取所有座位类型
     */
    @Override
    public List<SeatTypeEntity> getAllSeatTypes() {
        return seatTypeMapper.selectList(null);
    }
}