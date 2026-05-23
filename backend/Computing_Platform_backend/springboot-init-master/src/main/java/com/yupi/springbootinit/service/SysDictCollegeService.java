package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeUpdateRequest;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.vo.SysDictCollegeVO;

/**
 * 学院字典服务
 *
 * @author YU
 */
public interface SysDictCollegeService extends IService<SysDictCollege> {

    /**
     * 创建学院
     *
     * @param sysDictCollegeAddRequest 新增请求
     * @return 学院ID
     */
    Long createCollege(SysDictCollegeAddRequest sysDictCollegeAddRequest);

    /**
     * 更新学院
     *
     * @param sysDictCollegeUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateCollege(SysDictCollegeUpdateRequest sysDictCollegeUpdateRequest);

    /**
     * 删除学院
     *
     * @param id 学院ID
     * @return 是否成功
     */
    Boolean deleteCollege(Long id);

    /**
     * 根据ID获取学院
     *
     * @param id 学院ID
     * @return 学院VO
     */
    SysDictCollegeVO getCollegeById(Long id);

    /**
     * 分页查询学院
     *
     * @param sysDictCollegeQueryRequest 查询请求
     * @return 分页结果
     */
    Page<SysDictCollegeVO> pageCollege(SysDictCollegeQueryRequest sysDictCollegeQueryRequest);

    /**
     * 获取查询条件
     *
     * @param sysDictCollegeQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<SysDictCollege> getQueryWrapper(SysDictCollegeQueryRequest sysDictCollegeQueryRequest);

    /**
     * 获取学院VO
     *
     * @param sysDictCollege 学院实体
     * @return 学院VO
     */
    SysDictCollegeVO getCollegeVO(SysDictCollege sysDictCollege);
}