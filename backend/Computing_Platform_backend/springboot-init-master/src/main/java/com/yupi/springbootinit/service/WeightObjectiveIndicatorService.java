package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorCheckRequest;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorSaveRequest;
import com.yupi.springbootinit.model.entity.WeightObjectiveIndicator;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.model.vo.WeightCheckVO;
import com.yupi.springbootinit.model.vo.WeightObjectiveIndicatorVO;

import java.util.List;

//课程目标-指标点内部权重服务

public interface WeightObjectiveIndicatorService extends IService<WeightObjectiveIndicator> {

    /**
     * 获取指定课程可配置的指标点
     *
     * @param courseId 课程ID
     * @return 指标点列表
     */
    List<IndicatorPointVO> listAvailableIndicators(Long courseId);

    /**
     * 保存课程内部权重配置
     *
     * @param request 保存请求
     * @return 是否成功
     */
    Boolean saveWeights(WeightObjectiveIndicatorSaveRequest request);

    /**
     * 获取指定课程的内部权重配置
     *
     * @param courseId 课程ID
     * @return 内部权重列表
     */
    List<WeightObjectiveIndicatorVO> listWeights(Long courseId);

    /**
     * 校验内部权重合计是否为1
     *
     * @param request 校验请求
     * @return 校验结果
     */
    WeightCheckVO checkWeights(WeightObjectiveIndicatorCheckRequest request);

    /**
     * 通过 Excel 批量导入内部贡献权重
     *
     * @param file Excel 文件
     * @return 导入结果（total/successCount/failCount/failDetails）
     */
    java.util.Map<String, Object> importWeightsFromExcel(org.springframework.web.multipart.MultipartFile file);

    /**
     * 生成内部贡献权重导入模板
     *
     * @return 模板文件字节
     */
    byte[] generateWeightTemplate();
}
