package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorUpdateRequest;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.vo.SysDictMajorVO;

/**
 * 专业字典服务
 *
 * @author YU
 */
public interface SysDictMajorService extends IService<SysDictMajor> {

    /**
     * 创建专业
     *
     * @param sysDictMajorAddRequest 新增请求
     * @return 专业ID
     */
    Long createMajor(SysDictMajorAddRequest sysDictMajorAddRequest);

    /**
     * 更新专业
     *
     * @param sysDictMajorUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateMajor(SysDictMajorUpdateRequest sysDictMajorUpdateRequest);

    /**
     * 删除专业
     *
     * @param id 专业ID
     * @return 是否成功
     */
    Boolean deleteMajor(Long id);

    /**
     * 根据ID获取专业
     *
     * @param id 专业ID
     * @return 专业VO
     */
    SysDictMajorVO getMajorById(Long id);

    /**
     * 分页查询专业
     *
     * @param sysDictMajorQueryRequest 查询请求
     * @return 分页结果
     */
    Page<SysDictMajorVO> pageMajor(SysDictMajorQueryRequest sysDictMajorQueryRequest);

    /**
     * 获取查询条件
     *
     * @param sysDictMajorQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<SysDictMajor> getQueryWrapper(SysDictMajorQueryRequest sysDictMajorQueryRequest);

    /**
     * 获取专业VO
     *
     * @param sysDictMajor 专业实体
     * @return 专业VO
     */
    SysDictMajorVO getMajorVO(SysDictMajor sysDictMajor);
}