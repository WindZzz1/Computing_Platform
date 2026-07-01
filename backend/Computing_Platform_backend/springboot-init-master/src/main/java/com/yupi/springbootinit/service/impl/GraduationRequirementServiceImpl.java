package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.GraduationRequirementMapper;
import com.yupi.springbootinit.mapper.IndicatorPointMapper;
import com.yupi.springbootinit.mapper.SysDictCollegeMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementAddRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementQueryRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementUpdateRequest;
import com.yupi.springbootinit.model.entity.GraduationRequirement;
import com.yupi.springbootinit.model.entity.SysDictCollege;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.excel.GraduationRequirementExcel;
import com.yupi.springbootinit.model.vo.GraduationRequirementVO;
import com.yupi.springbootinit.service.GraduationRequirementService;
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
 * 毕业要求服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class GraduationRequirementServiceImpl extends ServiceImpl<GraduationRequirementMapper, GraduationRequirement> implements GraduationRequirementService {

    @Resource
    private GraduationRequirementMapper graduationRequirementMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictCollegeMapper sysDictCollegeMapper;

    @Resource
    private IndicatorPointMapper indicatorPointMapper;

    @Override
    public Long createRequirement(GraduationRequirementAddRequest graduationRequirementAddRequest) {
        if (graduationRequirementAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String requirementCode = graduationRequirementAddRequest.getRequirementCode();
        String requirementName = graduationRequirementAddRequest.getRequirementName();
        Long majorId = graduationRequirementAddRequest.getMajorId();
        if (StringUtils.isAnyBlank(requirementCode, requirementName) || majorId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "毕业要求编号、名称或专业ID不能为空");
        }
        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
        }
        synchronized (requirementCode.intern()) {
            QueryWrapper<GraduationRequirement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("requirement_code", requirementCode);
            queryWrapper.eq("major_id", majorId);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下毕业要求编号已存在");
            }
            GraduationRequirement graduationRequirement = new GraduationRequirement();
            BeanUtils.copyProperties(graduationRequirementAddRequest, graduationRequirement);
            boolean saveResult = this.save(graduationRequirement);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建毕业要求失败");
            }
            return graduationRequirement.getId();
        }
    }

    @Override
    public Boolean updateRequirement(GraduationRequirementUpdateRequest graduationRequirementUpdateRequest) {
        if (graduationRequirementUpdateRequest == null || graduationRequirementUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GraduationRequirement existRequirement = this.getById(graduationRequirementUpdateRequest.getId());
        if (existRequirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        Long majorId = graduationRequirementUpdateRequest.getMajorId();
        if (majorId != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "专业不存在");
            }
        }
        String requirementCode = graduationRequirementUpdateRequest.getRequirementCode();
        Long updateMajorId = graduationRequirementUpdateRequest.getMajorId() != null ? graduationRequirementUpdateRequest.getMajorId() : existRequirement.getMajorId();
        if (StringUtils.isNotBlank(requirementCode) && !requirementCode.equals(existRequirement.getRequirementCode())) {
            QueryWrapper<GraduationRequirement> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("requirement_code", requirementCode);
            queryWrapper.eq("major_id", updateMajorId);
            queryWrapper.ne("id", graduationRequirementUpdateRequest.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该专业下毕业要求编号已存在");
            }
        }
        GraduationRequirement graduationRequirement = new GraduationRequirement();
        BeanUtils.copyProperties(graduationRequirementUpdateRequest, graduationRequirement);
        return this.updateById(graduationRequirement);
    }

    @Override
    public Boolean deleteRequirement(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "毕业要求ID不能为空");
        }
        GraduationRequirement requirement = this.getById(id);
        if (requirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        validateRequirementNotReferenced(id);
        boolean result = this.removeById(id);
        return result;
    }

    private void validateRequirementNotReferenced(Long requirementId) {
        Long indicatorCount = indicatorPointMapper.countByRequirementId(requirementId);
        if (indicatorCount != null && indicatorCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "毕业要求已被指标点引用，不能删除");
        }
    }

    @Override
    public GraduationRequirementVO getRequirementById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "毕业要求ID不能为空");
        }
        GraduationRequirement graduationRequirement = this.getById(id);
        if (graduationRequirement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "毕业要求不存在");
        }
        return this.getRequirementVO(graduationRequirement);
    }

    @Override
    public Page<GraduationRequirementVO> pageRequirement(GraduationRequirementQueryRequest graduationRequirementQueryRequest) {
        long current = graduationRequirementQueryRequest.getCurrent();
        long size = graduationRequirementQueryRequest.getPageSize();
        QueryWrapper<GraduationRequirement> queryWrapper = this.getQueryWrapper(graduationRequirementQueryRequest);
        Page<GraduationRequirement> requirementPage = this.page(new Page<>(current, size), queryWrapper);
        Page<GraduationRequirementVO> requirementVOPage = new Page<>(current, size, requirementPage.getTotal());
        requirementVOPage.setRecords(requirementPage.getRecords().stream().map(this::getRequirementVO).collect(java.util.stream.Collectors.toList()));
        return requirementVOPage;
    }

    @Override
    public QueryWrapper<GraduationRequirement> getQueryWrapper(GraduationRequirementQueryRequest graduationRequirementQueryRequest) {
        QueryWrapper<GraduationRequirement> queryWrapper = new QueryWrapper<>();
        if (graduationRequirementQueryRequest == null) {
            return queryWrapper;
        }
        String requirementCode = graduationRequirementQueryRequest.getRequirementCode();
        String requirementName = graduationRequirementQueryRequest.getRequirementName();
        Long majorId = graduationRequirementQueryRequest.getMajorId();
        java.util.Date createTimeStart = graduationRequirementQueryRequest.getCreateTimeStart();
        java.util.Date createTimeEnd = graduationRequirementQueryRequest.getCreateTimeEnd();
        queryWrapper.like(StringUtils.isNotBlank(requirementCode), "requirement_code", requirementCode);
        queryWrapper.like(StringUtils.isNotBlank(requirementName), "requirement_name", requirementName);
        queryWrapper.eq(majorId != null, "major_id", majorId);
        queryWrapper.ge(createTimeStart != null, "create_time", createTimeStart);
        queryWrapper.le(createTimeEnd != null, "create_time", createTimeEnd);
        queryWrapper.orderByAsc("requirement_code");
        return queryWrapper;
    }

    @Override
    public GraduationRequirementVO getRequirementVO(GraduationRequirement graduationRequirement) {
        if (graduationRequirement == null) {
            return null;
        }
        GraduationRequirementVO graduationRequirementVO = new GraduationRequirementVO();
        BeanUtils.copyProperties(graduationRequirement, graduationRequirementVO);
        if (graduationRequirement.getMajorId() != null) {
            SysDictMajor major = sysDictMajorMapper.selectById(graduationRequirement.getMajorId());
            if (major != null) {
                graduationRequirementVO.setMajorName(major.getMajorName());
                graduationRequirementVO.setCollegeId(major.getCollegeId());
                if (major.getCollegeId() != null) {
                    SysDictCollege college = sysDictCollegeMapper.selectById(major.getCollegeId());
                    if (college != null) {
                        graduationRequirementVO.setCollegeName(college.getCollegeName());
                    }
                }
            }
        }
        return graduationRequirementVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importGraduationRequirementsFromExcel(MultipartFile file) {
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
            List<GraduationRequirementExcel> requirementExcels = EasyExcel.read(file.getInputStream())
                    .head(GraduationRequirementExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (requirementExcels == null || requirementExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < requirementExcels.size(); i++) {
                GraduationRequirementExcel excel = requirementExcels.get(i);
                try {
                    if (StringUtils.isAnyBlank(excel.getMajorCode(), excel.getRequirementCode(), excel.getRequirementName())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("requirementCode", excel.getRequirementCode() != null ? excel.getRequirementCode() : "");
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
                        detail.put("requirementCode", excel.getRequirementCode());
                        detail.put("reason", "专业代码 " + excel.getMajorCode() + " 不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    // 检查重复
                    QueryWrapper<GraduationRequirement> dupWrapper = new QueryWrapper<>();
                    dupWrapper.eq("requirement_code", excel.getRequirementCode());
                    dupWrapper.eq("major_id", major.getId());
                    if (this.count(dupWrapper) > 0) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("requirementCode", excel.getRequirementCode());
                        detail.put("reason", "该专业下毕业要求编号 " + excel.getRequirementCode() + " 已存在");
                        failDetails.add(detail);
                        continue;
                    }

                    GraduationRequirement requirement = new GraduationRequirement();
                    requirement.setMajorId(major.getId());
                    requirement.setRequirementCode(excel.getRequirementCode());
                    requirement.setRequirementName(excel.getRequirementName());
                    requirement.setDescription(excel.getDescription());

                    if (this.save(requirement)) {
                        successCount++;
                    } else {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("requirementCode", excel.getRequirementCode());
                        detail.put("reason", "保存失败");
                        failDetails.add(detail);
                    }
                } catch (BusinessException e) {
                    failCount++;
                    Map<String, String> detail = new HashMap<>();
                    detail.put("row", String.valueOf(i + 2));
                    detail.put("requirementCode", excel.getRequirementCode() != null ? excel.getRequirementCode() : "");
                    detail.put("reason", e.getMessage());
                    failDetails.add(detail);
                }
            }

            if (failCount > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "毕业要求导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", requirementExcels.size());
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
    public byte[] generateGraduationRequirementTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/graduation_requirement_template.xlsx");
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
            log.warn("读取毕业要求模板失败: {}", e.getMessage());
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/graduation_requirement_template.xlsx")) {
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
            log.warn("ClassLoader读取毕业要求模板失败: {}", e.getMessage());
        }

        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, GraduationRequirementExcel.class)
                    .sheet("毕业要求导入模板")
                    .doWrite(new ArrayList<>());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("动态生成毕业要求模板失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }
}
