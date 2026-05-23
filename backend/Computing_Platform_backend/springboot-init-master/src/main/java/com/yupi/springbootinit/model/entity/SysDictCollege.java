package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 学院字典表
 * @TableName sys_dict_college
 */
@TableName(value = "sys_dict_college")
@Data
public class SysDictCollege {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学院名称
     */
    private String collegeName;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
}