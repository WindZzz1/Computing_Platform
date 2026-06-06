package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.MatrixCourseIndicatorMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.matrix.MatrixCourseIndicatorSaveRequest;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import com.yupi.springbootinit.model.entity.MatrixCourseIndicator;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.vo.MatrixConfigVO;
import com.yupi.springbootinit.model.vo.MatrixCourseIndicatorVO;
import com.yupi.springbootinit.service.MatrixCourseIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 宏观支撑矩阵服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class MatrixCourseIndicatorServiceImpl extends ServiceImpl<MatrixCourseIndicatorMapper, MatrixCourseIndicator>
        implements MatrixCourseIndicatorService {

    @Resource
    private MatrixCourseIndicatorMapper matrixCourseIndicatorMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Override
    public MatrixConfigVO getMatrixConfigByMajorId(Long majorId) {
        if (majorId == null || majorId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "专业ID不能为空");
        }

        // 验证专业是否存在
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
        }

        MatrixConfigVO configVO = new MatrixConfigVO();
        configVO.setMajorId(majorId);
        configVO.setMajorName(major.getMajorName());

        // 获取专业下的所有课程
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("major_id", majorId);
        courseQueryWrapper.orderByAsc("course_code");
        List<Course> courses = courseMapper.selectList(courseQueryWrapper);

        List<MatrixConfigVO.CourseSimpleVO> courseSimpleVOList = courses.stream().map(course -> {
            MatrixConfigVO.CourseSimpleVO simpleVO = new MatrixConfigVO.CourseSimpleVO();
            simpleVO.setId(course.getId());
            simpleVO.setCourseCode(course.getCourseCode());
            simpleVO.setCourseName(course.getCourseName());
            simpleVO.setCredit(course.getCredit());
            return simpleVO;
        }).collect(Collectors.toList());
        configVO.setCourses(courseSimpleVOList);

        // 获取所有指标点
        QueryWrapper<IndicatorPoint> indicatorQueryWrapper = new QueryWrapper<>();
        indicatorQueryWrapper.orderByAsc("indicator_code");
        List<IndicatorPoint> indicators = indicatorPointMapper.selectList(indicatorQueryWrapper);

        List<MatrixConfigVO.IndicatorPointSimpleVO> indicatorSimpleVOList = indicators.stream().map(indicator -> {
            MatrixConfigVO.IndicatorPointSimpleVO simpleVO = new MatrixConfigVO.IndicatorPointSimpleVO();
            simpleVO.setId(indicator.getId());
            simpleVO.setIndicatorCode(indicator.getIndicatorCode());
            simpleVO.setIndicatorName(indicator.getIndicatorName());
            simpleVO.setRequirementId(indicator.getRequirementId());
            return simpleVO;
        }).collect(Collectors.toList());
        configVO.setIndicators(indicatorSimpleVOList);

        // 获取已有的矩阵数据
        QueryWrapper<MatrixCourseIndicator> matrixQueryWrapper = new QueryWrapper<>();
        matrixQueryWrapper.eq("major_id", majorId);
        List<MatrixCourseIndicator> matrixData = matrixCourseIndicatorMapper.selectList(matrixQueryWrapper);

        List<MatrixCourseIndicatorVO> matrixVOList = matrixData.stream().map(this::convertToVO).collect(Collectors.toList());
        configVO.setMatrixData(matrixVOList);

        // 计算每个指标点的列总和
        Map<Long, BigDecimal> columnSums = calculateColumnSums(matrixData);
        configVO.setColumnSums(columnSums);

        return configVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMatrixConfig(MatrixCourseIndicatorSaveRequest saveRequest) {
        if (saveRequest == null || saveRequest.getMajorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "保存请求不能为空");
        }

        Long majorId = saveRequest.getMajorId();

        // 验证专业是否存在
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
        }

        // 先进行权重校验
        WeightCheckResult checkResult = checkMatrixWeights(saveRequest);
        if (!checkResult.isValid()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, checkResult.getMessage());
        }

        // 删除该专业原有的所有矩阵数据
        matrixCourseIndicatorMapper.deleteByMajorIdPhysically(majorId);

        // 批量插入新的矩阵数据
        if (saveRequest.getMatrixItems() != null && !saveRequest.getMatrixItems().isEmpty()) {
            List<MatrixCourseIndicator> matrixList = new ArrayList<>();

            for (MatrixCourseIndicatorSaveRequest.MatrixItem item : saveRequest.getMatrixItems()) {
                // 只保存有权重的记录（权重不为null且大于0）
                if (item.getTotalWeight() != null && item.getTotalWeight().compareTo(BigDecimal.ZERO) > 0) {
                    // 验证课程是否存在
                    Course course = courseMapper.selectById(item.getCourseId());
                    if (course == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在: " + item.getCourseId());
                    }

                    // 验证课程是否属于该专业
                    if (!majorId.equals(course.getMajorId())) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,
                                "课程 " + course.getCourseName() + " 不属于该专业");
                    }

                    // 验证指标点是否存在
                    IndicatorPoint indicator = indicatorPointMapper.selectById(item.getIndicatorId());
                    if (indicator == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指标点不存在: " + item.getIndicatorId());
                    }

                    MatrixCourseIndicator matrix = new MatrixCourseIndicator();
                    matrix.setMajorId(majorId);
                    matrix.setCourseId(item.getCourseId());
                    matrix.setIndicatorId(item.getIndicatorId());
                    // 保留4位小数
                    matrix.setTotalWeight(item.getTotalWeight().setScale(4, RoundingMode.HALF_UP));
                    matrixList.add(matrix);
                }
            }

            if (!matrixList.isEmpty()) {
                boolean saveResult = this.saveBatch(matrixList);
                if (!saveResult) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存矩阵配置失败");
                }
            }
        }

        return true;
    }

    /**
     * 权重校验容差
     * 所有支撑同一指标点的课程，其总支撑权重之和必须为1.0（允许0.0001的浮点误差）
     */
    private static final BigDecimal WEIGHT_TOLERANCE = new BigDecimal("0.0001");

    @Override
    public WeightCheckResult checkMatrixWeights(MatrixCourseIndicatorSaveRequest saveRequest) {
        if (saveRequest == null || saveRequest.getMatrixItems() == null) {
            return new WeightCheckResult(false, "数据不能为空", new HashMap<>());
        }

        // 按指标点分组计算权重总和
        Map<Long, BigDecimal> columnSums = new HashMap<>();
        // 指标点ID -> 指标点名称的映射（用于错误提示）
        Map<Long, String> indicatorNames = new HashMap<>();

        for (MatrixCourseIndicatorSaveRequest.MatrixItem item : saveRequest.getMatrixItems()) {
            if (item.getTotalWeight() != null && item.getTotalWeight().compareTo(BigDecimal.ZERO) > 0) {
                columnSums.merge(item.getIndicatorId(), item.getTotalWeight(), BigDecimal::add);

                // 获取指标点名称
                if (!indicatorNames.containsKey(item.getIndicatorId())) {
                    IndicatorPoint indicator = indicatorPointMapper.selectById(item.getIndicatorId());
                    if (indicator != null) {
                        indicatorNames.put(item.getIndicatorId(),
                            indicator.getIndicatorCode() + " " + indicator.getIndicatorName());
                    }
                }
            }
        }

        // 检查每个指标点的权重总和是否为1.0
        List<String> errorMessages = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : columnSums.entrySet()) {
            Long indicatorId = entry.getKey();
            BigDecimal sum = entry.getValue().setScale(4, RoundingMode.HALF_UP);
            BigDecimal deviation = sum.subtract(BigDecimal.ONE).abs();

            if (deviation.compareTo(WEIGHT_TOLERANCE) > 0) {
                String indicatorName = indicatorNames.getOrDefault(indicatorId, "ID=" + indicatorId);
                errorMessages.add(String.format(
                    "指标点[%s]的支撑权重总和为%.4f，偏差%.4f，要求必须为1.0",
                    indicatorName, sum, deviation
                ));
            }
        }

        // 检查是否所有指标点都配置了权重
        if (saveRequest.getMajorId() != null) {
            QueryWrapper<IndicatorPoint> indicatorQueryWrapper = new QueryWrapper<>();
            indicatorQueryWrapper.orderByAsc("indicator_code");
            List<IndicatorPoint> allIndicators = indicatorPointMapper.selectList(indicatorQueryWrapper);

            for (IndicatorPoint indicator : allIndicators) {
                if (!columnSums.containsKey(indicator.getId())) {
                    errorMessages.add(String.format(
                        "指标点[%s %s]未配置任何支撑课程，权重总和为0，要求必须为1.0",
                        indicator.getIndicatorCode(), indicator.getIndicatorName()
                    ));
                }
            }
        }

        if (!errorMessages.isEmpty()) {
            return new WeightCheckResult(false, String.join("; ", errorMessages), columnSums);
        }

        return new WeightCheckResult(true, "校验通过：所有指标点的支撑权重总和均为1.0", columnSums);
    }

    /**
     * 计算每个指标点的列总和
     */
    private Map<Long, BigDecimal> calculateColumnSums(List<MatrixCourseIndicator> matrixData) {
        Map<Long, BigDecimal> columnSums = new HashMap<>();

        for (MatrixCourseIndicator item : matrixData) {
            if (item.getTotalWeight() != null) {
                columnSums.merge(item.getIndicatorId(), item.getTotalWeight(), BigDecimal::add);
            }
        }

        return columnSums;
    }

    /**
     * 转换为VO对象
     */
    private MatrixCourseIndicatorVO convertToVO(MatrixCourseIndicator matrix) {
        if (matrix == null) {
            return null;
        }

        MatrixCourseIndicatorVO vo = new MatrixCourseIndicatorVO();
        BeanUtils.copyProperties(matrix, vo);

        // 设置专业名称
        if (matrix.getMajorId() != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(matrix.getMajorId());
            if (major != null) {
                vo.setMajorName(major.getMajorName());
            }
        }

        // 设置课程信息
        if (matrix.getCourseId() != null) {
            Course course = courseMapper.selectById(matrix.getCourseId());
            if (course != null) {
                vo.setCourseCode(course.getCourseCode());
                vo.setCourseName(course.getCourseName());
            }
        }

        // 设置指标点信息
        if (matrix.getIndicatorId() != null) {
            IndicatorPoint indicator = indicatorPointMapper.selectById(matrix.getIndicatorId());
            if (indicator != null) {
                vo.setIndicatorCode(indicator.getIndicatorCode());
                vo.setIndicatorName(indicator.getIndicatorName());
            }
        }

        return vo;
    }
}
