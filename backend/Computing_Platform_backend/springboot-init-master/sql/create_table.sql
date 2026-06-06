-- ----------------------------
-- 1. 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS graduation_achievement
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE graduation_achievement;

-- ----------------------------
-- 2. 用户表（四大角色）
-- ----------------------------
CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          username VARCHAR(64) NOT NULL UNIQUE COMMENT '账号',
                          password VARCHAR(128) NOT NULL COMMENT '密码(加密)',
                          role_code VARCHAR(32) NOT NULL COMMENT '角色编码：admin/edu/leader/teacher',
                          college_id BIGINT COMMENT '所属学院ID（关联sys_dict_college）',
                          status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户表';

-- ----------------------------
-- 3. 学年学期表
-- ----------------------------
CREATE TABLE sys_dict_school_year (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   year_name VARCHAR(128) NOT NULL COMMENT '年份名称 如2023-2024学年',
                                   semester_name VARCHAR(128) NOT NULL COMMENT '学期名称 如第一学期',
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   is_deleted TINYINT DEFAULT 0 COMMENT '0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '学年学期表';

-- ----------------------------
-- 4. 学院字典表
-- ----------------------------
CREATE TABLE sys_dict_college (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  college_name VARCHAR(128) NOT NULL COMMENT '学院名称',
                                  is_deleted TINYINT DEFAULT 0 COMMENT '0未删除 1已删除',
                                  UNIQUE KEY uk_college_name (college_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '学院字典表';

-- ----------------------------
-- 5. 专业字典表
-- ----------------------------
CREATE TABLE sys_dict_major (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                major_code VARCHAR(64) NOT NULL COMMENT '专业代码',
                                major_name VARCHAR(128) NOT NULL COMMENT '专业名称',
                                college_id BIGINT COMMENT '所属学院ID（关联学院表）',
                                is_deleted TINYINT DEFAULT 0 COMMENT '0未删除 1已删除',
                                UNIQUE KEY uk_major_code (major_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '专业字典表';

-- ----------------------------
-- 6. 课程信息表
-- ----------------------------
CREATE TABLE course (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        major_id BIGINT NOT NULL COMMENT '专业ID（关联sys_dict_major）',
                        course_code VARCHAR(64) NOT NULL COMMENT '课程代码',
                        course_name VARCHAR(255) NOT NULL COMMENT '课程名称',
                        course_nature VARCHAR(32) COMMENT '课程性质：必修/选修',
                        credit DECIMAL(3,1) COMMENT '学分',
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        is_deleted TINYINT DEFAULT 0,
                        UNIQUE KEY uk_major_course_code (major_id, course_code),
                        INDEX idx_major_id (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '课程信息表';

-- ----------------------------
-- 7. 教学班级表
-- ----------------------------
CREATE TABLE teaching_class (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                course_id BIGINT NOT NULL COMMENT '课程ID',
                                teacher_id BIGINT NOT NULL COMMENT '主讲教师ID（sys_user.id）',
                                term_id BIGINT NOT NULL COMMENT '学年学期ID（关联sys_academic_term）',
                                class_name VARCHAR(128) NOT NULL COMMENT '班级名称',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                is_deleted TINYINT DEFAULT 0,
                                UNIQUE KEY uk_course_teacher_term (course_id, teacher_id, term_id),
                                INDEX idx_course_id (course_id),
                                INDEX idx_teacher_id (teacher_id),
                                INDEX idx_term_id (term_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '教学班级表';

-- ----------------------------
-- 8. 学生信息表
-- ----------------------------
CREATE TABLE student (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         student_no VARCHAR(32) NOT NULL UNIQUE COMMENT '学号',
                         student_name VARCHAR(64) NOT NULL COMMENT '姓名',
                         major_id BIGINT NOT NULL COMMENT '专业ID（sys_dict_major）',
                         grade VARCHAR(32) COMMENT '年级',
                         class_name VARCHAR(64) COMMENT '班级',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         is_deleted TINYINT DEFAULT 0,
                         INDEX idx_major_id (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '学生信息表';

-- ----------------------------
-- 9. 班级学生关联表
-- ----------------------------
CREATE TABLE class_student (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
                               student_id BIGINT NOT NULL COMMENT '学生ID',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               is_deleted TINYINT DEFAULT 0,
                               UNIQUE KEY uk_class_student (teaching_class_id, student_id),
                               INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '教学班级-学生关联表';

-- ----------------------------
-- 10. 毕业要求表
-- ----------------------------
CREATE TABLE graduation_requirement (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        major_id BIGINT NOT NULL COMMENT '专业ID',
                                        requirement_code VARCHAR(32) NOT NULL COMMENT 'GR1/GR2...',
                                        requirement_name VARCHAR(255) NOT NULL COMMENT '毕业要求名称',
                                        description TEXT COMMENT '描述',
                                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                                        is_deleted TINYINT DEFAULT 0,
                                        UNIQUE KEY uk_major_req (major_id, requirement_code),
                                        INDEX idx_major_id (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '毕业要求主表';

-- ----------------------------
-- 11. 毕业要求指标点表   每个毕业要求细分的指标点
-- ----------------------------
CREATE TABLE indicator_point (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           requirement_id BIGINT NOT NULL COMMENT '毕业要求ID',
                           indicator_code VARCHAR(32) NOT NULL COMMENT '1.1/1.2/2.1',
                           indicator_name VARCHAR(255) NOT NULL COMMENT '指标点名称',
                           description TEXT COMMENT '描述',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                           update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                           is_deleted TINYINT DEFAULT 0,
                           UNIQUE KEY uk_req_code (requirement_id, indicator_code),
                           INDEX idx_req_id (requirement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '毕业要求二级指标点表';

-- ----------------------------
-- 12. 宏观支撑矩阵表（课程→指标点权重 Wc）
-- ----------------------------
CREATE TABLE matrix_course_indicator (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         major_id BIGINT NOT NULL COMMENT '专业ID',
                                         course_id BIGINT NOT NULL COMMENT '课程ID',
                                         indicator_id BIGINT NOT NULL COMMENT '指标点ID',
                                         total_weight DECIMAL(5,4) NOT NULL COMMENT '总支撑权重 Wc',
                                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                                         is_deleted TINYINT DEFAULT 0,
                                         UNIQUE KEY uk_course_indicator (course_id, indicator_id),
                                         INDEX idx_indicator_id (indicator_id),
                                         INDEX idx_major_id (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '宏观支撑矩阵表(课程-指标点)';

-- ----------------------------
-- 13. 课程目标表
-- ----------------------------
CREATE TABLE course_objective (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  course_id BIGINT NOT NULL COMMENT '课程ID',
                                  obj_code VARCHAR(32) NOT NULL COMMENT 'CO1/CO2',
                                  obj_name VARCHAR(255) NOT NULL COMMENT '目标名称',
                                  obj_desc TEXT COMMENT '目标描述',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                                  is_deleted TINYINT DEFAULT 0,
                                  UNIQUE KEY uk_course_obj (course_id, obj_code),
                                  INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '课程目标表';

-- ----------------------------
-- 14. 内部贡献权重表（课程目标→指标点权重 wjk）
-- ----------------------------
CREATE TABLE weight_objective_indicator (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            course_id BIGINT NOT NULL COMMENT '课程ID',
                                            objective_id BIGINT NOT NULL COMMENT '课程目标ID',
                                            indicator_id BIGINT NOT NULL COMMENT '指标点ID',
                                            inner_weight DECIMAL(5,4) NOT NULL COMMENT '内部贡献权重 wjk',
                                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                            update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                                            is_deleted TINYINT DEFAULT 0,
                                            UNIQUE KEY uk_obj_indicator (objective_id, indicator_id),
                                            INDEX idx_indicator_id (indicator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '课程目标-指标点内部权重表';

-- ----------------------------
-- 15. 课程考核点表
-- ----------------------------
CREATE TABLE assessment_point (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  course_id BIGINT NOT NULL COMMENT '课程ID',
                                  point_code VARCHAR(64) NOT NULL COMMENT '考核点编号',
                                  point_name VARCHAR(255) NOT NULL COMMENT '考核点名称',
                                  full_score DECIMAL(5,1) NOT NULL COMMENT '满分值',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                                  is_deleted TINYINT DEFAULT 0,
                                  INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '课程考核点表';
-- 考核点 ↔ 课程目标 关联表
CREATE TABLE rel_point_objective (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     point_id BIGINT NOT NULL COMMENT '考核点ID',
                                     objective_id BIGINT NOT NULL COMMENT '课程目标ID',
                                     weight DECIMAL(5,4) DEFAULT 1.0 COMMENT '支撑权重',
                                     create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     is_deleted TINYINT DEFAULT 0,
                                     UNIQUE KEY uk_point_objective (point_id, objective_id),
                                     INDEX idx_point_id (point_id),
                                     INDEX idx_objective_id (objective_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '考核点-课程目标关联表(多对多)';

-- ----------------------------
-- 16. 学生考核点成绩表
-- ----------------------------
CREATE TABLE student_score (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
                               student_id BIGINT NOT NULL COMMENT '学生ID',
                               point_id BIGINT NOT NULL COMMENT '考核点ID',
                               actual_score DECIMAL(5,1) COMMENT '实际得分',
                               full_score DECIMAL(5,1) COMMENT '满分冗余',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
                               is_deleted TINYINT DEFAULT 0,
                               UNIQUE KEY uk_student_point (student_id, point_id),
                               INDEX idx_class_id (teaching_class_id),
                               INDEX idx_point_id (point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '学生考核点原始成绩表';

-- ----------------------------
-- 17. 学生课程目标达成度表（一级达成度）
-- ----------------------------
CREATE TABLE student_objective_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    objective_id BIGINT NOT NULL COMMENT '课程目标ID',
    objective_code VARCHAR(50) COMMENT '课程目标编号',
    objective_name VARCHAR(255) COMMENT '课程目标名称',
    achievement DECIMAL(10,4) COMMENT '一级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_class_student_objective (teaching_class_id, student_id, objective_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '学生课程目标达成度表（一级达成度）';

-- ----------------------------
-- 18. 课程指标点达成度表（二级达成度）
-- ----------------------------
CREATE TABLE course_indicator_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    indicator_id BIGINT NOT NULL COMMENT '指标点ID',
    indicator_code VARCHAR(50) COMMENT '指标点编号',
    indicator_name VARCHAR(255) COMMENT '指标点名称',
    achievement DECIMAL(10,4) COMMENT '二级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_class_indicator (teaching_class_id, indicator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '课程指标点达成度表（二级达成度）';

-- ----------------------------
-- 19. 专业级指标点达成度表（三级达成度）
-- ----------------------------
CREATE TABLE major_indicator_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL COMMENT '专业ID',
    term_id BIGINT COMMENT '学年学期ID',
    grade VARCHAR(20) COMMENT '年级',
    indicator_id BIGINT NOT NULL COMMENT '指标点ID',
    indicator_code VARCHAR(50) COMMENT '指标点编号',
    indicator_name VARCHAR(255) COMMENT '指标点名称',
    requirement_id BIGINT COMMENT '毕业要求ID',
    requirement_code VARCHAR(50) COMMENT '毕业要求编号',
    requirement_name VARCHAR(255) COMMENT '毕业要求名称',
    achievement DECIMAL(10,4) COMMENT '三级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_major_term_grade (major_id, term_id, grade),
    INDEX idx_indicator_id (indicator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '专业级指标点达成度表（三级达成度）';