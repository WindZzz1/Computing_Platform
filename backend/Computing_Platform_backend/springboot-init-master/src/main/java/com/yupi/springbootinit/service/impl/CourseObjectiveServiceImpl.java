package com.yupi.springbootinit.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.OwnershipHelper;
import com.yupi.springbootinit.mapper.CourseMapper;
import com.yupi.springbootinit.mapper.CourseObjectiveMapper;
import com.yupi.springbootinit.mapper.RelPointObjectiveMapper;
import com.yupi.springbootinit.mapper.WeightObjectiveIndicatorMapper;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseObjectiveUpdateRequest;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.entity.CourseObjective;
import com.yupi.springbootinit.model.entity.WeightObjectiveIndicator;
import com.yupi.springbootinit.model.excel.CourseObjectiveExcel;
import com.yupi.springbootinit.model.vo.CourseObjectiveVO;
import com.yupi.springbootinit.service.CourseObjectiveService;
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

//课程目标服务实现

@Service
@Slf4j
public class CourseObjectiveServiceImpl extends ServiceImpl<CourseObjectiveMapper, CourseObjective>
        implements CourseObjectiveService {

    @Resource
    private RelPointObjectiveMapper relPointObjectiveMapper;

    @Resource
    private WeightObjectiveIndicatorMapper weightObjectiveIndicatorMapper;

    @Resource
    private OwnershipHelper ownershipHelper;

    @Resource
    private CourseMapper courseMapper;

    @Override
    public Long createCourseObjective(CourseObjectiveAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long courseId = request.getCourseId();
        String objCode = request.getObjCode();
        String objName = request.getObjName();
        if (courseId == null || courseId <= 0 || StringUtils.isAnyBlank(objCode, objName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID、目标编号和目标名称不能为空");
        }
        ownershipHelper.checkCourseOwnership(courseId);
        validateDuplicateObjCode(courseId, objCode, null);
        baseMapper.deleteDeletedByCourseIdAndObjCode(courseId, objCode);

        CourseObjective courseObjective = new CourseObjective();
        BeanUtils.copyProperties(request, courseObjective);
        boolean saveResult = this.save(courseObjective);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建课程目标失败");
        }
        return courseObjective.getId();
    }

    @Override
    public Boolean updateCourseObjective(CourseObjectiveUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CourseObjective exist = this.getById(request.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        Long courseId = request.getCourseId() == null ? exist.getCourseId() : request.getCourseId();
        String objCode = StringUtils.isBlank(request.getObjCode()) ? exist.getObjCode() : request.getObjCode();
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
        ownershipHelper.checkCourseOwnership(exist.getCourseId());
        if (!courseId.equals(exist.getCourseId())) {
            ownershipHelper.checkCourseOwnership(courseId);
        }
        validateDuplicateObjCode(courseId, objCode, request.getId());

        CourseObjective courseObjective = new CourseObjective();
        BeanUtils.copyProperties(request, courseObjective);
        return this.updateById(courseObjective);
    }

    @Override
    public Boolean deleteCourseObjective(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标ID不合法");
        }
        CourseObjective exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        ownershipHelper.checkCourseOwnership(exist.getCourseId());
        validateObjectiveNotReferenced(id);
        return this.removeById(id);
    }

    @Override
    public CourseObjectiveVO getCourseObjectiveById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程目标ID不合法");
        }
        CourseObjective courseObjective = this.getById(id);
        if (courseObjective == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程目标不存在");
        }
        ownershipHelper.checkCourseOwnership(courseObjective.getCourseId());
        return getCourseObjectiveVO(courseObjective);
    }

    @Override
    public Page<CourseObjectiveVO> pageCourseObjective(CourseObjectiveQueryRequest request) {
        long current = request == null ? 1 : request.getCurrent();
        long size = request == null ? 10 : request.getPageSize();
        QueryWrapper<CourseObjective> queryWrapper = this.getQueryWrapper(request);
        Page<CourseObjective> page = this.page(new Page<>(current, size), queryWrapper);
        Page<CourseObjectiveVO> voPage = new Page<>(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::getCourseObjectiveVO)
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public QueryWrapper<CourseObjective> getQueryWrapper(CourseObjectiveQueryRequest request) {
        Long courseId = resolveRequiredCourseId(request);
        ownershipHelper.checkCourseOwnership(courseId);

        QueryWrapper<CourseObjective> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.like(StringUtils.isNotBlank(request.getObjCode()), "obj_code", request.getObjCode());
        queryWrapper.like(StringUtils.isNotBlank(request.getObjName()), "obj_name", request.getObjName());
        queryWrapper.orderByAsc("course_id", "obj_code");
        return queryWrapper;
    }

    @Override
    public CourseObjectiveVO getCourseObjectiveVO(CourseObjective courseObjective) {
        if (courseObjective == null) {
            return null;
        }
        CourseObjectiveVO vo = new CourseObjectiveVO();
        BeanUtils.copyProperties(courseObjective, vo);
        return vo;
    }

    private Long resolveRequiredCourseId(CourseObjectiveQueryRequest request) {
        if (request == null || request.getCourseId() == null || request.getCourseId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        return request.getCourseId();
    }



    /**
     * 校验同一课程下目标编号是否重复
     *
     * @param courseId  课程ID
     * @param objCode   目标编号
     * @param excludeId 更新时需要排除的课程目标ID
     */
    private void validateDuplicateObjCode(Long courseId, String objCode, Long excludeId) {
        QueryWrapper<CourseObjective> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("obj_code", objCode);
        queryWrapper.ne(excludeId != null, "id", excludeId);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该课程下目标编号已存在");
        }
    }

    /**
     * 校验课程目标是否已被下游数据引用
     *
     * @param objectiveId 课程目标ID
     */
    private void validateObjectiveNotReferenced(Long objectiveId) {
        Long assessmentCount = relPointObjectiveMapper.countByObjectiveId(objectiveId);
        if (assessmentCount != null && assessmentCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程目标已被考核点引用，不能删除");
        }

        QueryWrapper<WeightObjectiveIndicator> weightQueryWrapper = new QueryWrapper<>();
        weightQueryWrapper.eq("objective_id", objectiveId);
        Long weightCount = weightObjectiveIndicatorMapper.selectCount(weightQueryWrapper);
        if (weightCount != null && weightCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "课程目标已被内部权重配置引用，不能删除");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCourseObjectivesFromExcel(MultipartFile file) {
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
            List<CourseObjectiveExcel> objectiveExcels = EasyExcel.read(file.getInputStream())
                    .head(CourseObjectiveExcel.class)
                    .sheet(0)
                    .doReadSync();

            if (objectiveExcels == null || objectiveExcels.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Excel中没有数据");
            }

            for (int i = 0; i < objectiveExcels.size(); i++) {
                CourseObjectiveExcel excel = objectiveExcels.get(i);
                try {
                    if (StringUtils.isAnyBlank(excel.getCourseCode(), excel.getObjCode(), excel.getObjName())) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode() != null ? excel.getCourseCode() : "");
                        detail.put("reason", "必填字段为空（课程代码、目标编号、目标名称）");
                        failDetails.add(detail);
                        continue;
                    }

                    QueryWrapper<Course> courseWrapper = new QueryWrapper<>();
                    courseWrapper.eq("course_code", excel.getCourseCode());
                    Course course = courseMapper.selectOne(courseWrapper);
                    if (course == null) {
                        failCount++;
                        Map<String, String> detail = new HashMap<>();
                        detail.put("row", String.valueOf(i + 2));
                        detail.put("courseCode", excel.getCourseCode());
                        detail.put("reason", "课程代码不存在");
                        failDetails.add(detail);
                        continue;
                    }

                    ownershipHelper.checkCourseOwnership(course.getId());
                    validateDuplicateObjCode(course.getId(), excel.getObjCode(), null);
                    baseMapper.deleteDeletedByCourseIdAndObjCode(course.getId(), excel.getObjCode());

                    CourseObjective courseObjective = new CourseObjective();
                    courseObjective.setCourseId(course.getId());
                    courseObjective.setObjCode(excel.getObjCode());
                    courseObjective.setObjName(excel.getObjName());
                    courseObjective.setObjDesc(excel.getObjDesc());

                    if (this.save(courseObjective)) {
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
                        "课程目标导入存在 " + failCount + " 条失败，已整体回滚，请修正后重新导入");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", objectiveExcels.size());
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
    public byte[] generateCourseObjectiveTemplate() {
        String templatePath = "/templates/course_objective_template.xlsx";
        try {
            ClassPathResource resource = new ClassPathResource("templates/course_objective_template.xlsx");
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
            log.warn("使用ClassPathResource读取课程目标模板失败: {}", e.getMessage());
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/course_objective_template.xlsx")) {
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
            log.warn("使用ClassLoader读取课程目标模板失败: {}", e.getMessage());
        }

        log.info("静态模板不存在，使用动态生成");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, CourseObjectiveExcel.class)
                    .sheet("课程目标导入模板")
                    .doWrite(new ArrayList<>());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("动态生成课程目标模板失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成模板失败");
        }
    }
}
