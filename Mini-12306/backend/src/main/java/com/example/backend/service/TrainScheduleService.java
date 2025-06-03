package com.example.backend.service;

import com.example.backend.dto.TrainScheduleDTO;
import com.example.backend.model.SeatTypeEntity;
import com.example.backend.model.TrainScheduleEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 列车时刻表服务接口
 */
public interface TrainScheduleService {
    
    /**
     * 查询列车时刻表
     * 
     * @param departureStation 出发站
     * @param arrivalStation 到达站
     * @param departureDate 出发日期
     * @return 列车时刻表列表
     */
    List<TrainScheduleDTO> querySchedules(String departureStation, String arrivalStation, LocalDate departureDate);
    
    /**
     * 根据ID查询列车时刻表
     * 
     * @param scheduleId 时刻表ID
     * @return 列车时刻表
     */
    TrainScheduleDTO getScheduleById(String scheduleId);
    
    /**
     * 添加列车时刻表
     * 
     * @param schedule 列车时刻表
     * @param seatAvailabilities 座位可用性列表 (座位类型ID -> 可用数量)
     * @return 添加成功返回时刻表ID，失败返回null
     */
    String addSchedule(TrainScheduleEntity schedule, List<SeatTypeEntity> seatTypes);
    
    /**
     * 更新列车时刻表
     * 
     * @param schedule 列车时刻表
     * @return 更新成功返回true，失败返回false
     */
    boolean updateSchedule(TrainScheduleEntity schedule);
    
    /**
     * 删除列车时刻表
     * 
     * @param scheduleId 时刻表ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteSchedule(String scheduleId);
    
    /**
     * 获取所有座位类型
     * 
     * @return 座位类型列表
     */
    List<SeatTypeEntity> getAllSeatTypes();
}