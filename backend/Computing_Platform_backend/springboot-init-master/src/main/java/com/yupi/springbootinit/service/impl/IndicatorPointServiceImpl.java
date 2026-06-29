package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.GraduationRequirementMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointAddRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointQueryRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointUpdateRequest;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.entity.IndicatorPoint;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.excel.IndicatorPointExcel;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.service.IndicatorPointService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 二级指标点服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class IndicatorPointServiceImpl extends ServiceImpl<IndicatorPointMapper, IndicatorPoint> implements IndicatorPointService {

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Resource
    private GraduationRequirementMapper graduationRequirementMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Override
    public Long createIndicatorPoint(IndicatorPointAddRequest indicatorPointAddRequest) {
        if (indicatorPointAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String indicatorCode = indicatorPointAddRequest.getIndicatorCode();
        String indicatorName = indicatorPointAddRequest.getIndicatorName();
        Long requirementId = indicatorPointAddRequest.getRequirementId();
        if (StringUtils.isAnyBlank(indicatorCode, indicatorName) || requirementId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指标点编号、名称或毕业要求ID不能为空");
        }
        GraduationRequirement requirement = graduationRequirementMapper.selectById(requirementId);
        if (requirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        synchronized (indicatorCode.intern()) {
            QueryWrapper<IndicatorPoint> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("indicator_code", indicatorCode);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下指标点编号已存在");
            }
            IndicatorPoint indicatorPoint = new IndicatorPoint();
            BeanUtils.copyProperties(indicatorPointAddRequest, indicatorPoint);
            boolean saveResult = this.save(indicatorPoint);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建指标点失败");
            }
            return indicatorPoint.getId();
        }
    }

    @Override
    public Boolean updateIndicatorPoint(IndicatorPointUpdateRequest indicatorPointUpdateRequest) {
        if (indicatorPointUpdateRequest == null || indicatorPointUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        IndicatorPoint existIndicatorPoint = this.getById(indicatorPointUpdateRequest.getId());
        if (existIndicatorPoint == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指标点不存在");
        }
        Long requirementId = indicatorPointUpdateRequest.getRequirementId();
        if (requirementId != null) {
            GraduationRequirement requirement = graduationRequirementMapper.selectById(requirementId);
            if (requirement == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
            }
        }
        String indicatorCode = indicatorPointUpdateRequest.getIndicatorCode();
        if (StringUtils.isNotBlank(indicatorCode) && !indicatorCode.equals(existIndicatorPoint.getIndicatorCode())) {
            QueryWrapper<IndicatorPoint> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("indicator_code", indicatorCode);
            queryWrapper.ne("id", indicatorPointUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下指标点编号已存在");
            }
        }
        IndicatorPoint indicatorPoint = new IndicatorPoint();
        BeanUtils.copyProperties(indicatorPointUpdateRequest, indicatorPoint);
        return this.updateById(indicatorPoint);
    }

    @Override
    public Boolean deleteIndicatorPoint(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指标点ID不能为空");
        }
        IndicatorPoint indicatorPoint = this.getById(id);
        if (indicatorPoint == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指标点不存在");
        }
        validateIndicatorPointNotReferenced(id);
        boolean result = this.removeById(id);
        return result;
    }

    private void validateIndicatorPointNotReferenced(Long indicatorId) {
        Long matrixCount = indicatorPointMapper.countMatrixByIndicatorId(indicatorId);
        if (matrixCount != null && matrixCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "指标点已被宏观支撑矩阵引用，不能删除");
        }
        Long weightCount = indicatorPointMapper.countWeightByIndicatorId(indicatorId);
        if (weightCount != null && weightCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "指标点已被内部权重配置引用，不能删除");
        }
        Long courseResultCount = indicatorPointMapper.countCourseResultByIndicatorId(indicatorId);
        if (courseResultCount != null && courseResultCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "指标点已被课程级达成度结果引用，不能删除");
        }
        Long majorResultCount = indicatorPointMapper.countMajorResultByIndicatorId(indicatorId);
        if (majorResultCount != null && majorResultCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "指标点已被专业级达成度结果引用，不能删除");
        }
    }

    @Override
    public IndicatorPointVO getIndicatorPointById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "指标点ID不能为空");
        }
        IndicatorPoint indicatorPoint = this.getById(id);
        if (indicatorPoint == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指标点不存在");
        }
        return this.getIndicatorPointVO(indicatorPoint);
    }

    @Override
    public Page<IndicatorPointVO> pageIndicatorPoint(IndicatorPointQueryRequest indicatorPointQueryRequest) {
        long current = indicatorPointQueryRequest.getCurrent();
        long size = indicatorPointQueryRequest.getPageSize();
        QueryWrapper<IndicatorPoint> queryWrapper = this.getQueryWrapper(indicatorPointQueryRequest);
        Page<IndicatorPoint> indicatorPointPage = this.page(new Page<>(current, size), queryWrapper);
        Page<IndicatorPointVO> indicatorPointVOPage = new Page<>(current, size, indicatorPointPage.getTotal());
        indicatorPointVOPage.setRecords(indicatorPointPage.getRecords().stream().map(this::getIndicatorPointVO).collect(java.util.stream.Collectors.toList()));
        return indicatorPointVOPage;
    }

    @Override
    public QueryWrapper<IndicatorPoint> getQueryWrapper(IndicatorPointQueryRequest indicatorPointQueryRequest) {
        QueryWrapper<IndicatorPoint> queryWrapper = new QueryWrapper<>();
        if (indicatorPointQueryRequest == null) {
            return queryWrapper;
        }
        String indicatorCode = indicatorPointQueryRequest.getIndicatorCode();
        String indicatorName = indicatorPointQueryRequest.getIndicatorName();
        Long requirementId = indicatorPointQueryRequest.getRequirementId();
        java.util.Date createTimeStart = indicatorPointQueryRequest.getCreateTimeStart();
        java.util.Date createTimeEnd = indicatorPointQueryRequest.getCreateTimeEnd();
        queryWrapper.like(StringUtils.isNotBlank(indicatorCode), "indicator_code", indicatorCode);
        queryWrapper.like(StringUtils.isNotBlank(indicatorName), "indicator_name", indicatorName);
        queryWrapper.eq(requirementId != null, "requirement_id", requirementId);
        queryWrapper.ge(createTimeStart != null, "create_time", createTimeStart);
        queryWrapper.le(createTimeEnd != null, "create_time", createTimeEnd);
        queryWrapper.orderByAsc("indicator_code");
        return queryWrapper;
    }

    @Override
    public IndicatorPointVO getIndicatorPointVO(IndicatorPoint indicatorPoint) {
        if (indicatorPoint == null) {
            return null;
        }
        IndicatorPointVO indicatorPointVO = new IndicatorPointVO();
        BeanUtils.copyProperties(indicatorPoint, indicatorPointVO);
        if (indicatorPoint.getRequirementId() != null) {
            GraduationRequirement requirement = graduationRequirementMapper.selectById(indicatorPoint.getRequirementId());
            if (requirement != null) {
                indicatorPointVO.setRequirementCode(requirement.getRequirementCode());
                indicatorPointVO.setRequirementName(requirement.getRequirementName());
            }
        }
        return indicatorPointVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importIndicatorPointsFromExcel(MultipartFile file) {
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
            List<IndicatorPointExcel> indicatorExcels = EasyExcel.read(file.getInputStream())
                    .head(IndicatorPointExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (indicatorExcels == null || indicatorExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < indicatorExcels.size(); i++) {
                IndicatorPointExcel excel = indicatorExcels.get(i);
                try {
                    if (StringUtils.isAnyBlank(excel.getRequirementCode(), excel.getIndicatorCode(), excel.getIndicatorName())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("indicatorCode", excel.getIndicatorCode() != null ? excel.getIndicatorCode() : "");
                        detail.put("reason", "必填字段为空（毕业要求编码、指标点编号、指标点名称）");
                        failDetails.add(detail);
                        continue;
                    }

                    // 查找毕业要求
                    QueryWrapper<GraduationRequirement> reqWrapper = new QueryWrapper<>();
                    reqWrapper.eq("requirement_code", excel.getRequirementCode());
                    GraduationRequirement requirement = graduationRequirementMapper.selectOne(reqWrapper);
                    if (requirement == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("indicatorCode", excel.getIndicatorCode());
                        detail.put("reason", "毕业要求编码 " + excel.getRequirementCode() + " 不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查重复
                    QueryWrapper<IndicatorPoint> dupWrapper = new QueryWrapper<>();
                    dupWrapper.eq("indicator_code", excel.getIndicatorCode());
                    if (this.count(dupWrapper) > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("indicatorCode", excel.getIndicatorCode());
                        detail.put("reason", "指标点编号 " + excel.getIndicatorCode() + " 已存在");
                        failDetails.add(detail);
                        continue;
                    }

                    IndicatorPoint indicatorPoint = new IndicatorPoint();
                    indicatorPoint.setRequirementId(requirement.getId());
                    indicatorPoint.setIndicatorCode(excel.getIndicatorCode());
                    indicatorPoint.setIndicatorName(excel.getIndicatorName());
                    indicatorPoint.setDescription(excel.getDescription());

                    if (this.save(indicatorPoint)) {
                        successCount++;
                    } else {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("indicatorCode", excel.getIndicatorCode());
                        detail.put("reason", "保存失败");
                        failDetails.add(detail);
                    }
                } catch (BusinessException e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("indicatorCode", excel.getIndicatorCode() != null ? excel.getIndicatorCode() : "");
                    detail.put("reason", e.getMessage());
                    failDetails.add(detail);
                }
            }

            if (failCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "指标点导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", indicatorExcels.size());
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
    public byte[] generateIndicatorPointTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/indicator_point_template.xlsx");
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
            log.warn("使用ClassPathResource读取指标点模板失败: {}", e.getMessage());
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/indicator_point_template.xlsx")) {
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
            log.warn("使用ClassLoader读取指标点模板失败: {}", e.getMessage());
        }

        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, IndicatorPointExcel.class)
                    .sheet("指标点导入模板")
                    .doWrite(new ArrayList<>());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("动态生成指标点模板失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }
}
