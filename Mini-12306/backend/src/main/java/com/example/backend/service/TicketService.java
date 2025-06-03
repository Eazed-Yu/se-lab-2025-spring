package com.example.backend.service;

import com.example.backend.dto.*;
import java.util.List;

/**
 * 车票服务接口
 * 提供查询车次、购票、退票、改签和查询用户车票等功能
 */
public interface TicketService {
    
    /**
     * 查询车次时刻表
     * @param departureStation 出发站 (可选)
     * @param arrivalStation 到达站 (可选)
     * @param departureDateStr 出发日期 (可选, YYYY-MM-DD)
     * @return 符合条件的车次列表
     */
    List<TrainScheduleDTO> querySchedules(String departureStation, String arrivalStation, String departureDateStr);

    /**
     * 购买火车票业务流程实现
     * 
     * 完整流程：
     * 1. 查询车次信息并验证座位类型
     * 2. 验证乘客身份信息
     * 3. 检查余票并锁定车票
     * 4. 创建订单和车票（初始状态为待支付）
     * 5. 调用支付服务进行支付
     * 6. 根据支付结果更新订单和车票状态
     * 
     * @param request 购票请求DTO，包含用户信息、车次信息和座位类型等
     * @return 订单DTO，包含订单信息和车票详情
     * @throws IllegalArgumentException 当车次不存在或座位类型无效时
     * @throws RuntimeException 当身份验证失败、余票不足或支付失败时
     */
    OrderDTO purchaseTicket(PurchaseRequestDTO request);

    /**
     * 退票业务流程实现
     * 
     * 完整流程：
     * 1. 查询车票信息和订单信息
     * 2. 检查退票条件（车票状态是否符合退票要求）
     * 3. 调用支付服务处理退款
     * 4. 更新车票状态和订单状态
     * 5. 释放座位资源（增加余票数量）
     * 
     * @param request 退票请求DTO，包含用户ID和车票ID
     * @return 退票响应DTO，包含退票结果信息
     * @throws IllegalArgumentException 当车票不存在时
     * @throws IllegalStateException 当订单不存在时（数据不一致）
     * @throws RuntimeException 当不符合退票条件或退款处理失败时
     */
    RefundResponseDTO refundTicket(RefundRequestDTO request);

    /**
     * 改签车票业务流程实现
     * 
     * 完整流程：
     * 1. 查询原车票信息并验证
     * 2. 查询新车次信息
     * 3. 验证新座位类型
     * 4. 检查新车次余票
     * 5. 处理差价支付
     * 6. 创建新车票
     * 7. 更新原车票状态
     * 
     * @param request 改签请求DTO，包含原车票ID、新车次ID和新座位类型等
     * @return 改签响应DTO，包含改签结果信息
     * @throws IllegalArgumentException 当车票不存在或新车次不存在时
     * @throws RuntimeException 当不符合改签条件、余票不足或支付失败时
     */
    ChangeTicketResponseDTO changeTicket(ChangeTicketRequestDTO request);

    /**
     * 获取用户的车票列表
     * 
     * @param userId 用户ID
     * @return 用户车票列表
     */
    List<TicketDTO> getUserTickets(String userId);
}