package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.GraduationRequirementMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.MatrixCourseIndicatorMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.matrix.MatrixCourseIndicatorSaveRequest;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import com.yupi.springbootinit.model.entity.MatrixCourseIndicator;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.excel.MatrixCourseIndicatorExcel;
import com.yupi.springbootinit.model.vo.MatrixConfigVO;
import com.yupi.springbootinit.model.vo.MatrixCourseIndicatorVO;
import com.yupi.springbootinit.service.MatrixCourseIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private GraduationRequirementMapper graduationRequirementMapper;

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

        // 获取当前专业下的所有指标点
        List<IndicatorPoint> indicators = listIndicatorsByMajorId(majorId);

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

        Map<Long, IndicatorPoint> availableIndicatorMap = getIndicatorMapByMajorId(majorId);

        // 先进行权重校验
        WeightCheckResult checkResult = checkMatrixWeights(saveRequest, availableIndicatorMap);
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

                    // 验证指标点是否属于当前专业
                    IndicatorPoint indicator = availableIndicatorMap.get(item.getIndicatorId());
                    if (indicator == null) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,
                                "指标点不属于该专业: " + item.getIndicatorId());
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
        if (saveRequest == null || saveRequest.getMajorId() == null) {
            return new WeightCheckResult(false, "专业ID不能为空", new HashMap<>());
        }
        Map<Long, IndicatorPoint> availableIndicatorMap = getIndicatorMapByMajorId(saveRequest.getMajorId());
        return checkMatrixWeights(saveRequest, availableIndicatorMap);
    }

    private WeightCheckResult checkMatrixWeights(MatrixCourseIndicatorSaveRequest saveRequest,
                                                 Map<Long, IndicatorPoint> availableIndicatorMap) {
        if (saveRequest == null || saveRequest.getMatrixItems() == null) {
            return new WeightCheckResult(false, "数据不能为空", new HashMap<>());
        }

        // 按指标点分组计算权重总和
        Map<Long, BigDecimal> columnSums = new HashMap<>();
        // 指标点ID -> 指标点名称的映射（用于错误提示）
        Map<Long, String> indicatorNames = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();

        for (MatrixCourseIndicatorSaveRequest.MatrixItem item : saveRequest.getMatrixItems()) {
            if (item.getTotalWeight() != null && item.getTotalWeight().compareTo(BigDecimal.ZERO) > 0) {
                if (item.getIndicatorId() == null) {
                    errorMessages.add("指标点ID不能为空");
                    continue;
                }

                IndicatorPoint indicator = availableIndicatorMap.get(item.getIndicatorId());
                if (indicator == null) {
                    errorMessages.add("指标点不属于当前专业: " + item.getIndicatorId());
                    continue;
                }

                columnSums.merge(item.getIndicatorId(), item.getTotalWeight(), BigDecimal::add);
                indicatorNames.putIfAbsent(item.getIndicatorId(),
                        indicator.getIndicatorCode() + " " + indicator.getIndicatorName());
            }
        }

        // 检查每个指标点的权重总和是否为1.0
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
        for (IndicatorPoint indicator : availableIndicatorMap.values()) {
            if (!columnSums.containsKey(indicator.getId())) {
                errorMessages.add(String.format(
                    "指标点[%s %s]未配置任何支撑课程，权重总和为0，要求必须为1.0",
                    indicator.getIndicatorCode(), indicator.getIndicatorName()
                ));
            }
        }

        if (!errorMessages.isEmpty()) {
            return new WeightCheckResult(false, String.join("; ", errorMessages), columnSums);
        }

        return new WeightCheckResult(true, "校验通过：所有指标点的支撑权重总和均为1.0", columnSums);
    }

    private List<IndicatorPoint> listIndicatorsByMajorId(Long majorId) {
        QueryWrapper<GraduationRequirement> requirementQueryWrapper = new QueryWrapper<>();
        requirementQueryWrapper.select("id");
        requirementQueryWrapper.eq("major_id", majorId);
        requirementQueryWrapper.orderByAsc("requirement_code");
        List<GraduationRequirement> requirements = graduationRequirementMapper.selectList(requirementQueryWrapper);
        if (requirements.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requirementIds = requirements.stream()
                .map(GraduationRequirement::getId)
                .collect(Collectors.toList());

        QueryWrapper<IndicatorPoint> indicatorQueryWrapper = new QueryWrapper<>();
        indicatorQueryWrapper.in("requirement_id", requirementIds);
        indicatorQueryWrapper.orderByAsc("indicator_code");
        return indicatorPointMapper.selectList(indicatorQueryWrapper);
    }

    private Map<Long, IndicatorPoint> getIndicatorMapByMajorId(Long majorId) {
        return listIndicatorsByMajorId(majorId).stream()
                .collect(Collectors.toMap(
                        IndicatorPoint::getId,
                        indicator -> indicator,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importMatrixFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式不正确，请上传Excel文件");
        }

        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();

        try {
            List<MatrixCourseIndicatorExcel> matrixExcels = EasyExcel.read(file.getInputStream())
                    .head(MatrixCourseIndicatorExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (matrixExcels == null || matrixExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < matrixExcels.size(); i++) {
                MatrixCourseIndicatorExcel excel = matrixExcels.get(i);
                try {
                    if (StringUtils.isAnyBlank(excel.getMajorCode(), excel.getCourseCode(),
                            excel.getIndicatorCode()) || excel.getTotalWeight() == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                        detail.put("reason", "必填字段为空");
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找专业
                    QueryWrapper<SysDictMajor> majorWrapper = new QueryWrapper<>();
                    majorWrapper.eq("major_code", excel.getMajorCode());
                    SysDictMajor major = sysDictMajorMapper.selectOne(majorWrapper);
                    if (major == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "专业代码 " + excel.getMajorCode() + " 不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找课程
                    QueryWrapper<Course> courseWrapper = new QueryWrapper<>();
                    courseWrapper.eq("course_code", excel.getCourseCode());
                    Course course = courseMapper.selectOne(courseWrapper);
                    if (course == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "课程代码 " + excel.getCourseCode() + " 不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找指标点
                    QueryWrapper<IndicatorPoint> indicatorWrapper = new QueryWrapper<>();
                    indicatorWrapper.eq("indicator_code", excel.getIndicatorCode());
                    IndicatorPoint indicator = indicatorPointMapper.selectOne(indicatorWrapper);
                    if (indicator == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "指标点编号 " + excel.getIndicatorCode() + " 不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 验证指标点属于该专业（通过毕业要求关联）
                    if (indicator.getRequirementId() != null) {
                        GraduationRequirement req = graduationRequirementMapper.selectById(indicator.getRequirementId());
                        if (req == null || !major.getId().equals(req.getMajorId())) {
                            failCount++;
                            Map<String, String> detail = new HashMap<>();
                            detail.put("row", String.valueOf(i + 2));
                            detail.put("courseCode", excel.getCourseCode());
                            detail.put("reason", "指标点 " + excel.getIndicatorCode() + " 不属于专业 " + excel.getMajorCode());
                            failDetails.add(detail);
                            continue;
                        }
                    }

                    // 验证权重范围
                    if (excel.getTotalWeight().compareTo(BigDecimal.ZERO) <= 0
                            || excel.getTotalWeight().compareTo(BigDecimal.ONE) > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "权重必须在0~1之间");
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查重复（同一专业+课程+指标点）
                    QueryWrapper<MatrixCourseIndicator> dupWrapper = new QueryWrapper<>();
                    dupWrapper.eq("major_id", major.getId());
                    dupWrapper.eq("course_id", course.getId());
                    dupWrapper.eq("indicator_id", indicator.getId());
                    if (this.count(dupWrapper) > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "该专业下课程 " + excel.getCourseCode()
                                + " 与指标点 " + excel.getIndicatorCode() + " 的支撑关系已存在");
                        failDetails.add(detail);
                        continue;
                    }

                    MatrixCourseIndicator matrix = new MatrixCourseIndicator();
                    matrix.setMajorId(major.getId());
                    matrix.setCourseId(course.getId());
                    matrix.setIndicatorId(indicator.getId());
                    matrix.setTotalWeight(excel.getTotalWeight().setScale(4, RoundingMode.HALF_UP));

                    if (this.save(matrix)) {
                        successCount++;
                    } else {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "保存失败");
                        failDetails.add(detail);
                    }
                } catch (BusinessException e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                    detail.put("reason", e.getMessage());
                    failDetails.add(detail);
                }
            }

            if (failCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "宏观支撑矩阵导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", matrixExcels.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failDetails", failDetails);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件读取失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateMatrixTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/matrix_template.xlsx");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    return buffer.toByteArray();
                }
            }
        } catch (Exception e) {
            log.warn("使用ClassPathResource读取宏观支撑矩阵模板失败: {}", e.getMessage());
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/matrix_template.xlsx")) {
            if (inputStream != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                return buffer.toByteArray();
            }
        } catch (Exception e) {
            log.warn("使用ClassLoader读取宏观支撑矩阵模板失败: {}", e.getMessage());
        }

        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, MatrixCourseIndicatorExcel.class)
                    .sheet("宏观支撑矩阵导入模板")
                    .doWrite(new ArrayList<>());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("动态生成宏观支撑矩阵模板失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }
}
