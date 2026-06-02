-- 为 teaching_class 表添加达成度计算相关字段
USE graduation_achievement;

-- 添加计算状态字段
ALTER TABLE teaching_class
ADD COLUMN calculated_status TINYINT DEFAULT 0 COMMENT '计算状态：0-未计算，1-已计算';

-- 添加锁定状态字段
ALTER TABLE teaching_class
ADD COLUMN locked_status TINYINT DEFAULT 0 COMMENT '锁定状态：0-未锁定，1-已锁定';

-- 添加计算时间字段
ALTER TABLE teaching_class
ADD COLUMN calculate_time DATETIME COMMENT '计算时间';

-- 添加锁定时间字段
ALTER TABLE teaching_class
ADD COLUMN lock_time DATETIME COMMENT '锁定时间';
