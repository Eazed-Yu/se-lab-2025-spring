package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.TrainScheduleDTO;
import com.example.backend.model.SeatTypeEntity;
import com.example.backend.model.TrainScheduleEntity;
import com.example.backend.service.TrainScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 列车时刻表控制器
 */
@RestController
@RequestMapping("/api/schedules")
public class TrainScheduleController {

    @Autowired
    private TrainScheduleService trainScheduleService;

    /**
     * 查询列车时刻表
     */
    @GetMapping
    public ApiResponse<List<TrainScheduleDTO>> querySchedules(
            @RequestParam(required = false) String departureStation,
            @RequestParam(required = false) String arrivalStation,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
        
        List<TrainScheduleDTO> schedules = trainScheduleService.querySchedules(departureStation, arrivalStation, departureDate);
        return ApiResponse.success(schedules);
    }

    /**
     * 根据ID查询列车时刻表
     */
    @GetMapping("/{scheduleId}")
    public ApiResponse<TrainScheduleDTO> getScheduleById(@PathVariable String scheduleId) {
        TrainScheduleDTO schedule = trainScheduleService.getScheduleById(scheduleId);
        if (schedule == null) {
            return ApiResponse.error("列车时刻表不存在");
        }
        return ApiResponse.success(schedule);
    }

    /**
     * 添加列车时刻表（管理员功能）
     */
    @PostMapping
    public ApiResponse<String> addSchedule(@RequestBody TrainScheduleEntity schedule) {
        // 获取所有座位类型
        List<SeatTypeEntity> seatTypes = trainScheduleService.getAllSeatTypes();
        if (seatTypes.isEmpty()) {
            return ApiResponse.error("座位类型不存在，请先添加座位类型");
        }
        
        // 添加列车时刻表
        String scheduleId = trainScheduleService.addSchedule(schedule, seatTypes);
        return ApiResponse.success(scheduleId, "添加成功");
    }

    /**
     * 更新列车时刻表（管理员功能）
     */
    @PutMapping("/{scheduleId}")
    public ApiResponse<Boolean> updateSchedule(@PathVariable String scheduleId, @RequestBody TrainScheduleEntity schedule) {
        // 设置时刻表ID
        schedule.setId(scheduleId);
        
        // 更新列车时刻表
        boolean success = trainScheduleService.updateSchedule(schedule);
        if (!success) {
            return ApiResponse.error("更新失败，列车时刻表不存在");
        }
        
        return ApiResponse.success(true, "更新成功");
    }

    /**
     * 删除列车时刻表（管理员功能）
     */
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Boolean> deleteSchedule(@PathVariable String scheduleId) {
        boolean success = trainScheduleService.deleteSchedule(scheduleId);
        if (!success) {
            return ApiResponse.error("删除失败，列车时刻表不存在");
        }
        
        return ApiResponse.success(true, "删除成功");
    }

    /**
     * 获取所有座位类型
     */
    @GetMapping("/seat-types")
    public ApiResponse<List<SeatTypeEntity>> getAllSeatTypes() {
        List<SeatTypeEntity> seatTypes = trainScheduleService.getAllSeatTypes();
        return ApiResponse.success(seatTypes);
    }
}