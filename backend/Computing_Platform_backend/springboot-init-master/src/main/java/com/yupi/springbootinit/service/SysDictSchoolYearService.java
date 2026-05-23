package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearUpdateRequest;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.vo.SysDictSchoolYearVO;

/**
 * 学年学期字典服务
 *
 * @author YU
 */
public interface SysDictSchoolYearService extends IService<SysDictSchoolYear> {

    /**
     * 创建学年学期
     *
     * @param sysDictSchoolYearAddRequest 新增请求
     * @return 学年学期ID
     */
    Long createSchoolYear(SysDictSchoolYearAddRequest sysDictSchoolYearAddRequest);

    /**
     * 更新年学期
     *
     * @param sysDictSchoolYearUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateSchoolYear(SysDictSchoolYearUpdateRequest sysDictSchoolYearUpdateRequest);

    /**
     * 删除学年学期
     *
     * @param id 学年学期ID
     * @return 是否成功
     */
    Boolean deleteSchoolYear(Long id);

    /**
     * 根据ID获取学年学期
     *
     * @param id 学年学期ID
     * @return 学年学期VO
     */
    SysDictSchoolYearVO getSchoolYearById(Long id);

    /**
     * 分页查询学年学期
     *
     * @param sysDictSchoolYearQueryRequest 查询请求
     * @return 分页结果
     */
    Page<SysDictSchoolYearVO> pageSchoolYear(SysDictSchoolYearQueryRequest sysDictSchoolYearQueryRequest);

    /**
     * 获取查询条件
     *
     * @param sysDictSchoolYearQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<SysDictSchoolYear> getQueryWrapper(SysDictSchoolYearQueryRequest sysDictSchoolYearQueryRequest);

    /**
     * 获取学年学期VO
     *
     * @param sysDictSchoolYear 学年学期实体
     * @return 学年学期VO
     */
    SysDictSchoolYearVO getSchoolYearVO(SysDictSchoolYear sysDictSchoolYear);
}