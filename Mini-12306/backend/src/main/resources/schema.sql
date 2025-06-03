-- 创建数据库表结构
-- 数据库名：mini12306
CREATE DATABASE IF NOT EXISTS `mini12306`;
USE `mini12306`;
-- 初始化数据库表结构

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` varchar(36) NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 列车时刻表
CREATE TABLE IF NOT EXISTS `train_schedule` (
  `id` varchar(36) NOT NULL COMMENT '时刻表ID',
  `train_number` varchar(20) NOT NULL COMMENT '车次号',
  `departure_station` varchar(50) NOT NULL COMMENT '出发站',
  `arrival_station` varchar(50) NOT NULL COMMENT '到达站',
  `departure_time` datetime NOT NULL COMMENT '出发时间',
  `arrival_time` datetime NOT NULL COMMENT '到达时间',
  `status` varchar(20) NOT NULL DEFAULT '正常' COMMENT '状态：正常、晚点、取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_train_number` (`train_number`),
  KEY `idx_departure_time` (`departure_time`),
  KEY `idx_stations` (`departure_station`, `arrival_station`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='列车时刻表';

-- 座位类型表
CREATE TABLE IF NOT EXISTS `seat_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '座位类型ID',
  `code` varchar(20) NOT NULL COMMENT '座位类型编码',
  `name` varchar(20) NOT NULL COMMENT '座位类型名称',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础票价',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位类型表';

-- 座位余票表
CREATE TABLE IF NOT EXISTS `seat_availability` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `schedule_id` varchar(36) NOT NULL COMMENT '时刻表ID',
  `seat_type_id` int NOT NULL COMMENT '座位类型ID',
  `available_count` int NOT NULL COMMENT '可用座位数',
  `price` decimal(10,2) NOT NULL COMMENT '票价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_seat_type` (`schedule_id`, `seat_type_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_seat_type_id` (`seat_type_id`),
  CONSTRAINT `fk_seat_availability_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `train_schedule` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_seat_availability_seat_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位余票表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` varchar(36) NOT NULL COMMENT '订单ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `order_type` varchar(20) NOT NULL COMMENT '订单类型：购票、退票、改签',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `payment_status` varchar(20) NOT NULL COMMENT '支付状态：未支付、支付成功、支付失败',
  `order_status` varchar(20) NOT NULL COMMENT '订单状态：处理中、已完成、已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 车票表
CREATE TABLE IF NOT EXISTS `ticket` (
  `id` varchar(36) NOT NULL COMMENT '车票ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `order_id` varchar(36) NOT NULL COMMENT '订单ID',
  `schedule_id` varchar(36) NOT NULL COMMENT '时刻表ID',
  `passenger_name` varchar(50) NOT NULL COMMENT '乘客姓名',
  `passenger_id_card` varchar(18) NOT NULL COMMENT '乘客身份证号',
  `seat_type_id` int NOT NULL COMMENT '座位类型ID',
  `carriage_number` varchar(10) DEFAULT NULL COMMENT '车厢号',
  `seat_number` varchar(20) DEFAULT NULL COMMENT '座位号',
  `ticket_type` varchar(20) NOT NULL DEFAULT '成人票' COMMENT '票种：成人票、学生票、儿童票',
  `price_paid` decimal(10,2) NOT NULL COMMENT '支付金额',
  `ticket_status` varchar(20) NOT NULL COMMENT '车票状态：待支付、已支付、已出票、已检票、已退票、已改签、已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_passenger_id_card` (`passenger_id_card`),
  CONSTRAINT `fk_ticket_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
  CONSTRAINT `fk_ticket_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `train_schedule` (`id`),
  CONSTRAINT `fk_ticket_seat_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_type` (`id`),
  CONSTRAINT `fk_ticket_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车票表';

-- 插入初始数据

-- 座位类型数据
INSERT INTO `seat_type` (`code`, `name`, `base_price`) VALUES
('FIRST_CLASS', '一等座', 800.00),
('SECOND_CLASS', '二等座', 550.00),
('BUSINESS_CLASS', '商务座', 1200.00);

-- 用户数据
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `id_card`, `phone`, `email`) VALUES
('u001', 'user1', 'password123', '张三', '110101199001011234', '13800138001', 'zhangsan@example.com'),
('u002', 'user2', 'password123', '李四', '110101199002022345', '13800138002', 'lisi@example.com'),
('u003', 'user3', 'password123', '王五', '110101199003033456', '13800138003', 'wangwu@example.com');

-- 列车时刻表数据
INSERT INTO `train_schedule` (`id`, `train_number`, `departure_station`, `arrival_station`, `departure_time`, `arrival_time`, `status`) VALUES
('G1234_20250520', 'G1234', '北京南', '上海虹桥', '2025-05-20 09:00:00', '2025-05-20 14:30:00', '正常'),
('G1001_20250520', 'G1001', '武汉', '广州南', '2025-05-20 10:00:00', '2025-05-20 14:00:00', '正常'),
('G7501_20250521', 'G7501', '上海虹桥', '杭州东', '2025-05-21 08:30:00', '2025-05-21 09:20:00', '正常'),
('C6002_20250520', 'C6002', '成都东', '重庆北', '2025-05-20 11:00:00', '2025-05-20 12:30:00', '晚点'),
('G87_20250521', 'G87', '北京西', '西安北', '2025-05-21 13:00:00', '2025-05-21 17:30:00', '正常'),
('G1235_20250520', 'G1235', '上海虹桥', '北京南', '2025-05-20 15:30:00', '2025-05-20 21:00:00', '正常'),
('G1235_20250521', 'G1235', '上海虹桥', '北京南', '2025-05-21 09:00:00', '2025-05-21 14:30:00', '正常');

-- 座位余票数据
INSERT INTO `seat_availability` (`schedule_id`, `seat_type_id`, `available_count`, `price`) VALUES
('G1234_20250520', 1, 10, 800.00),  -- 北京南-上海虹桥 一等座
('G1234_20250520', 2, 50, 550.00),  -- 北京南-上海虹桥 二等座
('G1001_20250520', 2, 25, 480.00),  -- 武汉-广州南 二等座
('G1001_20250520', 3, 5, 1500.00),  -- 武汉-广州南 商务座
('G7501_20250521', 1, 20, 117.50),  -- 上海虹桥-杭州东 一等座
('G7501_20250521', 2, 100, 73.00),  -- 上海虹桥-杭州东 二等座
('C6002_20250520', 1, 15, 246.00),  -- 成都东-重庆北 一等座
('C6002_20250520', 2, 80, 154.00),  -- 成都东-重庆北 二等座
('G87_20250521', 2, 60, 515.50),    -- 北京西-西安北 二等座
('G87_20250521', 3, 8, 1627.50),    -- 北京西-西安北 商务座
('G1235_20250520', 1, 15, 800.00),  -- 上海虹桥-北京南 一等座
('G1235_20250520', 2, 45, 550.00),  -- 上海虹桥-北京南 二等座
('G1235_20250521', 1, 8, 820.00),   -- 上海虹桥-北京南 一等座
('G1235_20250521', 2, 35, 570.00),  -- 上海虹桥-北京南 二等座
('G1235_20250521', 3, 3, 1600.00);  -- 上海虹桥-北京南 商务座

-- 订单和车票示例数据
INSERT INTO `order` (`id`, `user_id`, `order_type`, `total_amount`, `payment_status`, `order_status`, `create_time`) VALUES
('o001', 'u001', '购票', 550.00, '支付成功', '已完成', '2025-05-15 10:30:00'),
('o002', 'u002', '购票', 800.00, '支付成功', '已完成', '2025-05-16 14:20:00'),
('o003', 'u001', '退票', 500.00, '支付成功', '已完成', '2025-05-17 09:15:00');

INSERT INTO `ticket` (`id`, `user_id`, `order_id`, `schedule_id`, `passenger_name`, `passenger_id_card`, `seat_type_id`, `carriage_number`, `seat_number`, `ticket_type`, `price_paid`, `ticket_status`, `create_time`) VALUES
('t001', 'u001', 'o001', 'G1234_20250520', '张三', '110101199001011234', 2, '05', '05A', '成人票', 550.00, '已出票', '2025-05-15 10:30:00'),
('t002', 'u002', 'o002', 'G1235_20250520', '李四', '110101199002022345', 1, '03', '03F', '成人票', 800.00, '已出票', '2025-05-16 14:20:00'),
('t003', 'u001', 'o003', 'G1001_20250520', '张三', '110101199001011234', 2, '08', '08C', '成人票', 480.00, '已退票', '2025-05-17 09:15:00');