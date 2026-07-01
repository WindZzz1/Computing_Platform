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
import com.alibaba.excel.EasyExcel;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.excel.WeightObjectiveIndicatorExcel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private CourseMapper courseMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importWeightsFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel文件");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> failDetails = new ArrayList<>();

        List<WeightObjectiveIndicatorExcel> rows;
        try {
            rows = EasyExcel.read(file.getInputStream())
                    .head(WeightObjectiveIndicatorExcel.class)
                    .sheet(0)
                    .doReadSync();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel解析失败：" + e.getMessage());
        }
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
        }

        // 缓存，避免重复查库
        Map<String, Course> courseCache = new HashMap<>();
        Map<String, CourseObjective> objectiveCache = new HashMap<>();
        Map<String, IndicatorPoint> indicatorCache = new HashMap<>();
        // courseId -> 权重项列表
        Map<Long, List<WeightObjectiveIndicatorCheckRequest.Item>> byCourse = new LinkedHashMap<>();

        for (int i = 0; i < rows.size(); i++) {
            WeightObjectiveIndicatorExcel row = rows.get(i);
            int rowNum = i + 2;
            String courseCode = row.getCourseCode();
            try {
                if (StringUtils.isAnyBlank(row.getCourseCode(), row.getObjCode(),
                        row.getIndicatorCode(), row.getInnerWeight())) {
                    failDetails.add(failDetail(rowNum, courseCode, "必填字段为空"));
                    continue;
                }
                // 课程
                Course course = courseCache.computeIfAbsent(row.getCourseCode().trim(), code -> {
                    QueryWrapper<Course> q = new QueryWrapper<>();
                    q.eq("course_code", code);
                    return courseMapper.selectOne(q);
                });
                if (course == null) {
                    failDetails.add(failDetail(rowNum, courseCode, "课程代码不存在"));
                    continue;
                }
                ownershipHelper.checkCourseOwnership(course.getId());
                // 课程目标
                final String objCode = row.getObjCode().trim();
                CourseObjective objective = objectiveCache.computeIfAbsent(course.getId() + ":" + objCode, k -> {
                    QueryWrapper<CourseObjective> q = new QueryWrapper<>();
                    q.eq("course_id", course.getId());
                    q.eq("obj_code", objCode);
                    return courseObjectiveMapper.selectOne(q);
                });
                if (objective == null) {
                    failDetails.add(failDetail(rowNum, courseCode, "课程目标编号 " + objCode + " 在该课程中不存在"));
                    continue;
                }
                // 指标点
                IndicatorPoint indicator = indicatorCache.computeIfAbsent(row.getIndicatorCode().trim(), code -> {
                    QueryWrapper<IndicatorPoint> q = new QueryWrapper<>();
                    q.eq("indicator_code", code);
                    return indicatorPointMapper.selectOne(q);
                });
                if (indicator == null) {
                    failDetails.add(failDetail(rowNum, courseCode, "指标点编号 " + row.getIndicatorCode() + " 不存在"));
                    continue;
                }
                // 权重值
                BigDecimal innerWeight;
                try {
                    innerWeight = new BigDecimal(row.getInnerWeight().trim());
                } catch (NumberFormatException e) {
                    failDetails.add(failDetail(rowNum, courseCode, "内部权重格式不正确: " + row.getInnerWeight()));
                    continue;
                }
                if (innerWeight.compareTo(BigDecimal.ZERO) < 0 || innerWeight.compareTo(BigDecimal.ONE) > 0) {
                    failDetails.add(failDetail(rowNum, courseCode, "内部权重必须在0到1之间: " + innerWeight));
                    continue;
                }
                WeightObjectiveIndicatorCheckRequest.Item item = new WeightObjectiveIndicatorCheckRequest.Item();
                item.setObjectiveId(objective.getId());
                item.setIndicatorId(indicator.getId());
                item.setInnerWeight(innerWeight);
                byCourse.computeIfAbsent(course.getId(), k -> new ArrayList<>()).add(item);
            } catch (BusinessException e) {
                failDetails.add(failDetail(rowNum, courseCode, e.getMessage()));
            }
        }

        // 逐课程校验（复用 checkWeights：同指标点和=1.0、指标点在宏观矩阵范围内、目标属于该课程等）
        Map<Long, WeightObjectiveIndicatorSaveRequest> reqByCourse = new LinkedHashMap<>();
        if (failDetails.isEmpty()) {
            for (Map.Entry<Long, List<WeightObjectiveIndicatorCheckRequest.Item>> e : byCourse.entrySet()) {
                WeightObjectiveIndicatorSaveRequest req = new WeightObjectiveIndicatorSaveRequest();
                req.setCourseId(e.getKey());
                req.setWeightList(e.getValue());
                String code = courseCodeOf(courseCache, e.getKey());
                try {
                    WeightCheckVO vo = checkWeights(req);
                    if (!Boolean.TRUE.equals(vo.getValid())) {
                        failDetails.add(failDetail(0, code, vo.getMessage()));
                    } else {
                        reqByCourse.put(e.getKey(), req);
                    }
                } catch (BusinessException ex) {
                    failDetails.add(failDetail(0, code, ex.getMessage()));
                }
            }
        }

        // 全有或全无：有任一错误就不入库（此时未做任何写操作）
        if (!failDetails.isEmpty()) {
            result.put("total", rows.size());
            result.put("successCount", 0);
            result.put("failCount", failDetails.size());
            result.put("failDetails", failDetails);
            return result;
        }

        // 全部校验通过，逐课程先删后插
        for (WeightObjectiveIndicatorSaveRequest req : reqByCourse.values()) {
            saveWeights(req);
        }

        result.put("total", rows.size());
        result.put("successCount", rows.size());
        result.put("failCount", 0);
        result.put("failDetails", failDetails);
        return result;
    }

    @Override
    public byte[] generateWeightTemplate() {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        List<WeightObjectiveIndicatorExcel> sample = new ArrayList<>();
        WeightObjectiveIndicatorExcel s = new WeightObjectiveIndicatorExcel();
        s.setCourseCode("SE101");
        s.setObjCode("CO1");
        s.setIndicatorCode("1.1");
        s.setInnerWeight("0.25");
        sample.add(s);
        EasyExcel.write(out, WeightObjectiveIndicatorExcel.class).sheet("内部贡献权重").doWrite(sample);
        return out.toByteArray();
    }

    /** 反查 courseId 对应的 courseCode（用于错误信息展示） */
    private String courseCodeOf(Map<String, Course> courseCache, Long courseId) {
        return courseCache.entrySet().stream()
                .filter(en -> en.getValue() != null && en.getValue().getId().equals(courseId))
                .map(Map.Entry::getKey).findFirst().orElse("");
    }

    private Map<String, String> failDetail(int row, String courseCode, String reason) {
        Map<String, String> d = new HashMap<>();
        d.put("row", String.valueOf(row));
        d.put("courseCode", courseCode != null ? courseCode : "");
        d.put("reason", reason);
        return d;
    }
}
