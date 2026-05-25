package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果返回类
 *
 * @param <T> 数据类型
 * @author YU
 */
@Data
public class PageResultVO<T> implements Serializable {

    /**
     * 列表数据
     */
    private List<T> records;

    /**
     * 总数
     */
    private long total;

    /**
     * 每页条数
     */
    private long size;

    /**
     * 当前页
     */
    private long current;

    /**
     * 总页数
     */
    private long pages;

    public PageResultVO() {
    }

    public PageResultVO(List<T> records, long total, long size, long current, long pages) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = pages;
    }

    /**
     * 从MyBatis-Plus的Page对象构建PageResultVO
     *
     * @param page MyBatis-Plus的Page对象
     * @param <T>  数据类型
     * @return PageResultVO对象
     */
    public static <T> PageResultVO<T> from(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        result.setPages(page.getPages());
        return result;
    }
}
