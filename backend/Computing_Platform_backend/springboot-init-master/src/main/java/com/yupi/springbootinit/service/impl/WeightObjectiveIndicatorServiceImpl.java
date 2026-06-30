package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.OwnershipHelper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.GraduationRequirementMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.MatrixCourseIndicatorMapper;
import com.yupi.springbootinit.mapper.WeightObjectiveIndicatorMapper;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorCheckRequest;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorSaveRequest;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import com.yupi.springbootinit.model.entity.MatrixCourseIndicator;
import com.yupi.springbootinit.model.entity.WeightObjectiveIndicator;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.model.vo.WeightCheckVO;
import com.yupi.springbootinit.model.vo.WeightObjectiveIndicatorVO;
import com.yupi.springbootinit.service.WeightObjectiveIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// 课程目标-指标点内部权重服务实现

@Service
@Slf4j
public class WeightObjectiveIndicatorServiceImpl
        extends ServiceImpl<WeightObjectiveIndicatorMapper, WeightObjectiveIndicator>
        implements WeightObjectiveIndicatorService {

    /**
     * 权重校验目标值
     * 同一课程内，同一指标点下，所有课程目标的内部权重之和必须为1.0
     */
    private static final BigDecimal ONE = new BigDecimal("1.0000");

    /**
     * 权重校验容差
     * 允许0.0001的浮点误差，与宏观支撑矩阵保持一致
     */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");

    @Resource
    private MatrixCourseIndicatorMapper matrixCourseIndicatorMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private GraduationRequirementMapper graduationRequirementMapper;

    @Resource
    private CourseObjectiveMapper courseObjectiveMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Resource
    private OwnershipHelper ownershipHelper;

    @Override
    public List<IndicatorPointVO> listAvailableIndicators(Long courseId) {
        validateId(courseId, "课程ID");
        ownershipHelper.checkCourseOwnership(courseId);
        List<MatrixCourseIndicator> matrixList = listMatrixByCourseId(courseId);
        if (matrixList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> indicatorIds = matrixList.stream().map(MatrixCourseIndicator::getIndicatorId)
                .distinct().collect(Collectors.toList());
        List<IndicatorPoint> indicators = indicatorPointMapper.selectBatchIds(indicatorIds);
        return indicators.stream().map(this::getIndicatorPointVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveWeights(WeightObjectiveIndicatorSaveRequest request) {
        WeightCheckVO checkVO = checkWeights(request);
        if (!Boolean.TRUE.equals(checkVO.getValid())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "同一指标点下内部权重合计必须为1.0");
        }
        Long courseId = request.getCourseId();
        List<WeightObjectiveIndicatorCheckRequest.Item> items = request.getWeightList();
        weightObjectiveIndicatorMapper.deleteByCourseIdPhysically(courseId);
        if (items == null || items.isEmpty()) {
            return true;
        }
        List<WeightObjectiveIndicator> weightList = items.stream().map(item -> {
            WeightObjectiveIndicator weight = new WeightObjectiveIndicator();
            weight.setCourseId(courseId);
            weight.setObjectiveId(item.getObjectiveId());
            weight.setIndicatorId(item.getIndicatorId());
            weight.setInnerWeight(item.getInnerWeight());
            return weight;
        }).collect(Collectors.toList());
        return this.saveBatch(weightList);
    }

    @Override
    public List<WeightObjectiveIndicatorVO> listWeights(Long courseId) {
        validateId(courseId, "课程ID");
        ownershipHelper.checkCourseOwnership(courseId);
        QueryWrapper<WeightObjectiveIndicator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.orderByAsc("indicator_id", "objective_id");
        List<WeightObjectiveIndicator> weights = this.list(queryWrapper);
        return buildWeightVOList(weights);
    }

    @Override
    public WeightCheckVO checkWeights(WeightObjectiveIndicatorCheckRequest request) {
        validateWeightRequest(request);

        Map<Long, BigDecimal> sumMap = new LinkedHashMap<>();
        for (WeightObjectiveIndicatorCheckRequest.Item item : request.getWeightList()) {
            BigDecimal current = sumMap.getOrDefault(item.getIndicatorId(), BigDecimal.ZERO);
            sumMap.put(item.getIndicatorId(), current.add(item.getInnerWeight()));
        }

        // 校验每个指标点的权重总和是否为1.0
        List<String> errorMessages = new ArrayList<>();
        Map<Long, String> indicatorNames = new HashMap<>();

        // 获取指标点名称
        for (WeightObjectiveIndicatorCheckRequest.Item item : request.getWeightList()) {
            if (!indicatorNames.containsKey(item.getIndicatorId())) {
                IndicatorPoint indicator = indicatorPointMapper.selectById(item.getIndicatorId());
                if (indicator != null) {
                    indicatorNames.put(item.getIndicatorId(),
                        indicator.getIndicatorCode() + " " + indicator.getIndicatorName());
                }
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : sumMap.entrySet()) {
            Long indicatorId = entry.getKey();
            BigDecimal sum = entry.getValue().setScale(4, RoundingMode.HALF_UP);
            BigDecimal deviation = sum.subtract(ONE).abs();

            if (deviation.compareTo(TOLERANCE) > 0) {
                String indicatorName = indicatorNames.getOrDefault(indicatorId, "ID=" + indicatorId);
                errorMessages.add(String.format(
                    "指标点[%s]的内部权重总和为%.4f，偏差%.4f，要求必须为1.0",
                    indicatorName, sum, deviation
                ));
            }
        }

        boolean valid = errorMessages.isEmpty();
        WeightCheckVO vo = new WeightCheckVO();
        vo.setValid(valid);
        vo.setIndicatorWeightSumMap(sumMap);

        if (!valid) {
            vo.setMessage("内部权重校验失败：" + String.join("; ", errorMessages));
        } else {
            vo.setMessage("校验通过：所有指标点的内部权重总和均为1.0");
        }

        return vo;
    }

    /**
     * 校验内部权重请求的完整性和业务合法性
     *
     * @param request 权重校验请求
     */
    private void validateWeightRequest(WeightObjectiveIndicatorCheckRequest request) {
        if (request == null || request.getCourseId() == null || request.getCourseId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        }
        Long courseId = request.getCourseId();
        ownershipHelper.checkCourseOwnership(courseId);
        if (request.getWeightList() == null || request.getWeightList().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内部权重列表不能为空");
        }
        Set<Long> availableIndicatorIds = listMatrixByCourseId(courseId).stream()
                .map(MatrixCourseIndicator::getIndicatorId).collect(Collectors.toSet());
        if (availableIndicatorIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程尚未配置宏观支撑矩阵");
        }
        Map<Long, CourseObjective> objectiveMap = listObjectivesByCourseId(courseId).stream()
                .collect(Collectors.toMap(CourseObjective::getId, Function.identity()));
        if (objectiveMap.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程尚未配置课程目标");
        }
        Set<String> pairSet = new java.util.HashSet<>();
        for (WeightObjectiveIndicatorCheckRequest.Item item : request.getWeightList()) {
            if (item == null || item.getObjectiveId() == null || item.getIndicatorId() == null
                    || item.getInnerWeight() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "内部权重项不完整");
            }
            if (item.getInnerWeight().compareTo(BigDecimal.ZERO) < 0 || item.getInnerWeight().compareTo(BigDecimal.ONE) > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "内部权重必须在0到1之间");
            }
            if (!objectiveMap.containsKey(item.getObjectiveId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标不属于该课程");
            }
            if (!availableIndicatorIds.contains(item.getIndicatorId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该指标点不在本课程宏观支撑范围内");
            }
            String pairKey = item.getObjectiveId() + ":" + item.getIndicatorId();
            if (!pairSet.add(pairKey)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标与指标点的权重配置重复");
            }
        }
    }


    /**
     * 判断权重合计是否在容差范围内等于1
     *
     * @param value 权重合计
     * @return 是否等于1
     */
    private boolean isOneWithTolerance(BigDecimal value) {
        return value.subtract(ONE).abs().compareTo(TOLERANCE) <= 0;
    }

    /**
     * 查询指定课程的宏观支撑矩阵配置
     *
     * @param courseId 课程ID
     * @return 宏观支撑矩阵列表
     */
    private List<MatrixCourseIndicator> listMatrixByCourseId(Long courseId) {
        QueryWrapper<MatrixCourseIndicator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return matrixCourseIndicatorMapper.selectList(queryWrapper);
    }

    /**
     * 查询指定课程的课程目标
     *
     * @param courseId 课程ID
     * @return 课程目标列表
     */
    private List<CourseObjective> listObjectivesByCourseId(Long courseId) {
        QueryWrapper<CourseObjective> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return courseObjectiveMapper.selectList(queryWrapper);
    }

    /**
     * 构建内部权重VO列表
     *
     * @param weights 内部权重实体列表
     * @return 内部权重VO列表
     */
    private List<WeightObjectiveIndicatorVO> buildWeightVOList(List<WeightObjectiveIndicator> weights) {
        if (weights == null || weights.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> objectiveIds = weights.stream().map(WeightObjectiveIndicator::getObjectiveId).collect(Collectors.toSet());
        Set<Long> indicatorIds = weights.stream().map(WeightObjectiveIndicator::getIndicatorId).collect(Collectors.toSet());
        Map<Long, CourseObjective> objectiveMap = courseObjectiveMapper.selectBatchIds(objectiveIds).stream()
                .collect(Collectors.toMap(CourseObjective::getId, Function.identity()));
        Map<Long, IndicatorPoint> indicatorMap = indicatorPointMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(IndicatorPoint::getId, Function.identity()));
        List<WeightObjectiveIndicatorVO> result = new ArrayList<>();
        for (WeightObjectiveIndicator weight : weights) {
            WeightObjectiveIndicatorVO vo = new WeightObjectiveIndicatorVO();
            BeanUtils.copyProperties(weight, vo);
            CourseObjective objective = objectiveMap.get(weight.getObjectiveId());
            if (objective != null) {
                vo.setObjCode(objective.getObjCode());
                vo.setObjName(objective.getObjName());
            }
            IndicatorPoint indicator = indicatorMap.get(weight.getIndicatorId());
            if (indicator != null) {
                vo.setIndicatorCode(indicator.getIndicatorCode());
                vo.setIndicatorName(indicator.getIndicatorName());
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 获取指标点VO
     *
     * @param indicator 指标点实体
     * @return 指标点VO
     */
    private IndicatorPointVO getIndicatorPointVO(IndicatorPoint indicator) {
        if (indicator == null) {
            return null;
        }
        IndicatorPointVO vo = new IndicatorPointVO();
        BeanUtils.copyProperties(indicator, vo);
        // 查询并设置毕业要求信息
        if (indicator.getRequirementId() != null) {
            GraduationRequirement requirement = graduationRequirementMapper.selectById(indicator.getRequirementId());
            if (requirement != null) {
                vo.setRequirementCode(requirement.getRequirementCode());
                vo.setRequirementName(requirement.getRequirementName());
            }
        }
        return vo;
    }

    /**
     * 校验ID参数
     *
     * @param id   ID
     * @param name 字段名称
     */
    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + "不合法");
        }
    }
}
