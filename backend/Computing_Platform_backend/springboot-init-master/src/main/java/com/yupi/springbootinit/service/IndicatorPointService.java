package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointAddRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointQueryRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointUpdateRequest;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 二级指标点服务
 *
 * @author YU
 */
public interface IndicatorPointService extends IService<IndicatorPoint> {

    /**
     * 创建二级指标点
     *
     * @param indicatorPointAddRequest 新增请求
     * @return 二级指标点ID
     */
    Long createIndicatorPoint(IndicatorPointAddRequest indicatorPointAddRequest);

    /**
     * 更新二级指标点
     *
     * @param indicatorPointUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateIndicatorPoint(IndicatorPointUpdateRequest indicatorPointUpdateRequest);

    /**
     * 删除二级指标点
     *
     * @param id 二级指标点ID
     * @return 是否成功
     */
    Boolean deleteIndicatorPoint(Long id);

    /**
     * 根据ID获取二级指标点
     *
     * @param id 二级指标点ID
     * @return 二级指标点VO
     */
    IndicatorPointVO getIndicatorPointById(Long id);

    /**
     * 分页查询二级指标点
     *
     * @param indicatorPointQueryRequest 查询请求
     * @return 分页结果
     */
    Page<IndicatorPointVO> pageIndicatorPoint(IndicatorPointQueryRequest indicatorPointQueryRequest);

    /**
     * 获取查询条件
     *
     * @param indicatorPointQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<IndicatorPoint> getQueryWrapper(IndicatorPointQueryRequest indicatorPointQueryRequest);

    /**
     * 获取二级指标点VO
     *
     * @param indicatorPoint 二级指标点实体
     * @return 二级指标点VO
     */
    IndicatorPointVO getIndicatorPointVO(IndicatorPoint indicatorPoint);

    /**
     * 通过Excel批量导入指标点
     *
     * @param file Excel文件
     * @return 导入结果 { total, successCount, failCount, failDetails }
     */
    Map<String, Object> importIndicatorPointsFromExcel(MultipartFile file);

    /**
     * 生成指标点导入模板
     *
     * @return Excel文件字节数组
     */
    byte[] generateIndicatorPointTemplate();
}