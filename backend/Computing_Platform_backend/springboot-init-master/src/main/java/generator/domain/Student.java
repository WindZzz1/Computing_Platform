package generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 学生信息表
 * @TableName student
 */
@TableName(value ="student")
@Data
public class Student {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学号
     */
    private String student_no;

    /**
     * 姓名
     */
    private String student_name;

    /**
     * 专业ID（sys_dict_major）
     */
    private Long major_id;

    /**
     * 年级
     */
    private String grade;

    /**
     * 班级
     */
    private String class_name;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 
     */
    private Integer is_deleted;
}