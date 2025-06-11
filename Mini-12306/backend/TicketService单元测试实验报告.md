# TicketService 单元测试实验报告

## 实验目的
使用分支测试法为 TicketService 编写全面的单元测试，确保覆盖所有主要分支和边界条件。

## 实验方法
采用分支测试法（Branch Testing），确保测试用例覆盖代码中的每一个分支路径，包括：
- 正常执行路径
- 异常处理路径
- 边界条件
- 错误情况

## 被测试类
- **类名**: `TicketServiceImpl`
- **包路径**: `com.example.backend.service.impl.TicketServiceImpl`
- **测试接口**: `TicketService`

## 测试的方法
1. `querySchedules` - 查询车次时刻表
2. `purchaseTicket` - 购买火车票
3. `refundTicket` - 退票
4. `changeTicket` - 改签车票
5. `getUserTickets` - 获取用户车票列表

## 测试用例设计

### 1. querySchedules 方法测试

#### 1.1 成功场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| QS_001 | 提供完整查询条件 | departureStation="北京南", arrivalStation="上海虹桥", departureDate="2025-06-15" | 返回符合条件的车次列表 | 主流程分支 |
| QS_002 | 仅提供出发站 | departureStation="北京南", arrivalStation=null, departureDate=null | 返回从北京南出发的所有车次 | 部分条件分支 |
| QS_003 | 无查询条件 | departureStation=null, arrivalStation=null, departureDate=null | 返回所有车次 | 空条件分支 |
| QS_004 | 无匹配结果 | departureStation="不存在的出发站", arrivalStation="不存在的到达站" | 返回空列表 | 无结果分支 |

#### 1.2 异常场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| QS_E01 | 日期格式错误 | departureDate="2025/06/15" | 抛出 IllegalArgumentException | 日期解析异常分支 |

### 2. purchaseTicket 方法测试

#### 2.1 成功场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| PT_001 | 正常购票流程 | 有效的用户ID、车次ID、座位类型、乘车人ID | 返回订单信息，状态为"已完成" | 主流程分支 |

#### 2.2 异常场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| PT_E01 | 车次不存在 | scheduleId="INVALID_SCHEDULE" | 抛出 IllegalArgumentException | 车次验证失败分支 |
| PT_E02 | 座位类型无效 | seatType="无效座位类型" | 抛出 IllegalArgumentException | 座位类型验证失败分支 |
| PT_E03 | 乘车人不存在 | passengerId="INVALID_PASSENGER" | 抛出 IllegalArgumentException | 乘车人验证失败分支 |
| PT_E04 | 无权使用乘车人信息 | 使用其他用户的乘车人ID | 抛出 RuntimeException | 权限验证失败分支 |
| PT_E05 | 余票不足 | 选择已售完的车次和座位 | 抛出 RuntimeException | 余票检查失败分支 |
| PT_E06 | 身份验证失败 | passengerId="FAIL_ID_12345" | 抛出 RuntimeException | 身份验证失败分支 |

### 3. refundTicket 方法测试

#### 3.1 成功场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| RT_001 | 正常退票流程 | 有效的用户ID和车票ID | 返回退票成功信息，状态为"已退票" | 主流程分支 |

#### 3.2 异常场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| RT_E01 | 车票不存在 | ticketId="INVALID_TICKET_ID" | 抛出 IllegalArgumentException | 车票查询失败分支 |
| RT_E02 | 车票状态不符合退票条件 | 已改签的车票 | 抛出 RuntimeException | 状态检查失败分支 |

### 4. changeTicket 方法测试

#### 4.1 成功场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| CT_001 | 正常改签流程 | 有效的原车票ID、新车次ID、新座位类型 | 返回改签成功信息，包含新旧车票信息 | 主流程分支 |

#### 4.2 异常场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| CT_E01 | 原车票不存在 | ticketId="INVALID_TICKET_ID" | 抛出 IllegalArgumentException | 原车票查询失败分支 |
| CT_E02 | 原车票状态不符合改签条件 | 已退票的车票 | 抛出 RuntimeException | 原车票状态检查失败分支 |
| CT_E03 | 新车次不存在 | newScheduleId="INVALID_NEW_SCHEDULE" | 抛出 IllegalArgumentException | 新车次查询失败分支 |
| CT_E04 | 新座位类型无效 | seatType="无效座位类型" | 抛出 IllegalArgumentException | 新座位类型验证失败分支 |
| CT_E05 | 新车次余票不足 | 选择已售完的新车次 | 抛出 RuntimeException | 新车次余票检查失败分支 |

### 5. getUserTickets 方法测试

#### 5.1 成功场景测试
| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| GT_001 | 有车票记录 | 已购票用户的用户ID | 返回用户的车票列表 | 主流程分支 |
| GT_002 | 无车票记录 | 从未购票用户的用户ID | 返回空列表 | 空结果分支 |
| GT_003 | 按创建时间倒序排列 | 购买多张票的用户ID | 返回按时间倒序的车票列表 | 排序分支 |

### 6. 边界条件测试

| 测试用例ID | 测试场景 | 输入参数 | 预期结果 | 覆盖分支 |
|------------|----------|----------|----------|----------|
| BC_001 | 空字符串参数 | 各方法的字符串参数为空字符串 | 正常处理，不抛出异常 | 空字符串处理分支 |
| BC_002 | null参数 | 各方法的参数为null | 正常处理或抛出适当异常 | null值处理分支 |

## 测试环境
- **测试框架**: JUnit 5
- **Spring Boot版本**: 2.x+
- **数据库**: 使用Spring Boot Test的事务回滚机制
- **Mock策略**: 使用真实的Spring容器和服务

## 测试执行方式
```bash
# 在项目根目录执行
mvn test -Dtest=TicketServiceBranchTest
```

## 预期的测试覆盖率
- **分支覆盖率**: 目标 95%+
- **行覆盖率**: 目标 90%+
- **方法覆盖率**: 100%

## 测试数据准备
测试使用以下预置数据：
- 车次信息：G1235_20250530, G1235_20250531, G1236_20250531
- 座位类型：二等座、一等座、商务座
- 测试乘车人：passenger_001
- 特殊测试ID：FAIL_ID_12345（用于身份验证失败测试）

## 分支测试法的应用
1. **控制流分析**: 分析每个方法的控制流图，识别所有分支点
2. **分支枚举**: 列出每个分支的真值和假值路径
3. **测试用例设计**: 为每个分支路径设计至少一个测试用例
4. **异常路径覆盖**: 确保所有异常处理分支都被测试
5. **边界条件测试**: 测试输入参数的边界值和特殊值

## 测试函数详细总结

### querySchedules方法测试函数

1. testQuerySchedules_Success_WithAllParams
   测试提供完整查询条件的情况。输入出发站、到达站和出发日期，验证能够正确返回符合条件的车次列表。覆盖主流程分支，确保完整查询条件的正确处理。

2. testQuerySchedules_Success_DepartureStationOnly
   测试仅提供出发站的部分查询条件。验证系统能够正确处理部分查询参数，返回从指定出发站出发的所有车次。覆盖部分条件分支。

3. testQuerySchedules_Success_NoConditions
   测试无任何查询条件的情况。验证当所有查询参数为null时，系统能够返回所有可用车次。覆盖空条件分支。

4. testQuerySchedules_Fail_InvalidDateFormat
   测试日期格式错误的异常处理。输入错误格式的日期字符串，验证系统能够正确抛出IllegalArgumentException异常并包含相应错误信息。覆盖日期解析异常分支。

5. testQuerySchedules_Success_NoResults
   测试查询不存在车站组合的情况。验证当查询条件不匹配任何车次时，系统能够正确返回空列表而不是抛出异常。覆盖无结果分支。

### purchaseTicket方法测试函数

6. testPurchaseTicket_Success_Normal
   测试正常购票流程。使用有效的用户ID、车次ID、座位类型和乘车人ID，验证能够成功创建订单并返回完整的订单信息。覆盖主流程分支。

7. testPurchaseTicket_Fail_ScheduleNotFound
   测试车次不存在的异常处理。使用无效的车次ID，验证系统能够正确抛出IllegalArgumentException异常。覆盖车次验证失败分支。

8. testPurchaseTicket_Fail_InvalidSeatType
   测试座位类型无效的异常处理。使用不存在的座位类型，验证系统能够正确识别并抛出相应异常。覆盖座位类型验证失败分支。

9. testPurchaseTicket_Fail_PassengerNotFound
   测试乘车人不存在的异常处理。使用无效的乘车人ID，验证系统能够正确抛出IllegalArgumentException异常。覆盖乘车人验证失败分支。

10. testPurchaseTicket_Fail_UnauthorizedPassenger
    测试乘车人权限验证。使用其他用户的乘车人信息，验证系统能够正确检查乘车人归属并抛出权限异常。覆盖权限验证失败分支。

11. testPurchaseTicket_Fail_InsufficientSeats
    测试余票不足的异常处理。选择没有余票的车次和座位类型，验证系统能够正确检查余票并抛出相应异常。覆盖余票检查失败分支。

12. testPurchaseTicket_Fail_IdentityVerificationFailed
    测试身份验证失败的异常处理。使用特殊的测试ID触发身份验证失败，验证系统能够正确处理身份验证异常。覆盖身份验证失败分支。

### refundTicket方法测试函数

13. testRefundTicket_Success_Normal
    测试正常退票流程。先购买一张票，然后进行退票操作，验证能够成功退票并更新车票状态。覆盖主流程分支。

14. testRefundTicket_Fail_TicketNotFound
    测试车票不存在的异常处理。使用无效的车票ID，验证系统能够正确抛出IllegalArgumentException异常。覆盖车票查询失败分支。

15. testRefundTicket_Fail_InvalidStatus
    测试车票状态不符合退票条件的异常处理。先改签车票使其状态变为已改签，然后尝试退票，验证系统能够正确检查车票状态。覆盖状态检查失败分支。

### changeTicket方法测试函数

16. testChangeTicket_Success_Normal
    测试正常改签流程。先购买一张票，然后进行改签操作，验证能够成功改签并返回新旧车票信息。覆盖主流程分支。

17. testChangeTicket_Fail_OriginalTicketNotFound
    测试原车票不存在的异常处理。使用无效的原车票ID，验证系统能够正确抛出IllegalArgumentException异常。覆盖原车票查询失败分支。

18. testChangeTicket_Fail_InvalidOriginalTicketStatus
    测试原车票状态不符合改签条件的异常处理。先退票使车票状态变为已退票，然后尝试改签，验证系统能够正确检查原车票状态。覆盖原车票状态检查失败分支。

19. testChangeTicket_Fail_NewScheduleNotFound
    测试新车次不存在的异常处理。使用无效的新车次ID，验证系统能够正确抛出IllegalArgumentException异常。覆盖新车次查询失败分支。

20. testChangeTicket_Fail_InvalidNewSeatType
    测试新座位类型无效的异常处理。使用无效的新座位类型，验证系统能够正确识别并抛出相应异常。覆盖新座位类型验证失败分支。

21. testChangeTicket_Fail_InsufficientNewSeats
    测试新车次余票不足的异常处理。使用没有余票的新车次，验证系统能够正确检查新车次余票情况。覆盖新车次余票检查失败分支。

### getUserTickets方法测试函数

22. testGetUserTickets_Success_WithTickets
    测试有车票记录的用户查询。先购买多张票，然后查询用户车票，验证能够正确返回完整的车票列表和车票信息。覆盖主流程分支。

23. testGetUserTickets_Success_NoTickets
    测试无车票记录的用户查询。使用从未购票的用户ID，验证系统能够正确返回空列表而不是抛出异常。覆盖空结果分支。

24. testGetUserTickets_Success_OrderedByCreateTime
    测试车票按创建时间倒序排列。按时间顺序购买多张票，验证返回的车票列表能够按创建时间倒序排列。覆盖排序分支。

### 边界条件测试函数

25. testBoundaryConditions_EmptyStrings
    测试空字符串参数的处理。使用空字符串作为查询参数，验证系统能够优雅处理而不会崩溃。覆盖空字符串处理分支。

26. testBoundaryConditions_NullParams
    测试null参数的处理。使用null作为查询参数，验证系统能够正确处理null值并返回适当的结果。覆盖null值处理分支。

## 分支测试方法总结

1. querySchedules方法分支测试
   该方法主要验证车次查询功能的各种输入条件分支。包括完整查询条件分支、部分条件分支、空条件分支和无结果分支。异常分支主要测试日期格式错误的情况。预期结果是能够正确处理各种查询条件组合，在输入无效时抛出适当的异常。

2. purchaseTicket方法分支测试
   该方法是最复杂的业务流程，包含多个验证分支。主流程分支验证正常购票流程。异常分支包括车次不存在、座位类型无效、乘车人不存在、权限验证失败、余票不足和身份验证失败等情况。预期结果是在正常情况下成功创建订单，在各种异常情况下抛出相应的异常并保持数据一致性。

3. refundTicket方法分支测试
   该方法验证退票业务的各种状态分支。主流程分支测试正常退票流程。异常分支包括车票不存在和车票状态不符合退票条件的情况。预期结果是在符合条件时成功退票并释放座位资源，在不符合条件时抛出相应异常。

4. changeTicket方法分支测试
   该方法验证改签业务的复杂分支逻辑。主流程分支测试正常改签流程。异常分支包括原车票不存在、原车票状态不符合改签条件、新车次不存在、新座位类型无效和新车次余票不足等情况。预期结果是在满足改签条件时成功创建新车票并更新原车票状态，在不满足条件时抛出相应异常。

5. getUserTickets方法分支测试
   该方法验证用户车票查询的不同数据状态分支。包括有车票记录分支、无车票记录分支和按时间排序分支。预期结果是能够正确返回用户的车票列表，对于没有车票的用户返回空列表，并且结果按创建时间倒序排列。

6. 边界条件分支测试
   验证各方法对特殊输入值的处理分支。包括空字符串参数分支和null参数分支。预期结果是系统能够优雅地处理这些边界情况，不会导致系统崩溃。

## 实验结论
通过分支测试法，我们设计了26个测试用例，覆盖了TicketService的所有主要功能分支：
- 正常业务流程分支
- 各种异常处理分支
- 边界条件处理分支
- 数据验证分支

这些测试用例能够：
1. 验证业务逻辑的正确性
2. 确保异常情况得到正确处理
3. 提高代码质量和稳定性
4. 为后续功能修改提供回归测试保障

## 改进建议
1. 增加性能测试用例
2. 添加并发场景测试
3. 考虑使用Mock对象减少对外部依赖
4. 增加集成测试覆盖完整的业务流程
