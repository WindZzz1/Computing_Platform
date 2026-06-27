package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.course.CourseAddRequest;
import com.yupi.springbootinit.model.dto.course.CourseImportRequest;
import com.yupi.springbootinit.model.dto.course.CourseQueryRequest;
import com.yupi.springbootinit.model.dto.course.CourseUpdateRequest;
import com.yupi.springbootinit.model.entity.Course;
import com.yupi.springbootinit.model.vo.CourseVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 课程服务
 *
 * @author YU
 */
public interface CourseService extends IService<Course> {

    /**
     * 创建课程
     *
     * @param courseAddRequest 新增请求
     * @return 课程ID
     */
    Long createCourse(CourseAddRequest courseAddRequest);

    /**
     * 更新课程
     *
     * @param courseUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateCourse(CourseUpdateRequest courseUpdateRequest);

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    Boolean deleteCourse(Long id);

    /**
     * 根据ID获取课程
     *
     * @param id 课程ID
     * @return 课程VO
     */
    CourseVO getCourseById(Long id);

    /**
     * 分页查询课程
     *
     * @param courseQueryRequest 查询请求
     * @return 分页结果
     */
    Page<CourseVO> pageCourse(CourseQueryRequest courseQueryRequest);

    /**
     * 获取查询条件
     *
     * @param courseQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<Course> getQueryWrapper(CourseQueryRequest courseQueryRequest);

    /**
     * 获取课程VO
     *
     * @param course 课程实体
     * @return 课程VO
     */
    CourseVO getCourseVO(Course course);

    /**
     * 批量导入课程
     *
     * @param courseImportRequest 导入请求
     * @return 导入结果（成功数量，失败详情）
     */
    Map<String, Object> importCourses(CourseImportRequest courseImportRequest);

    /**
     * 通过Excel批量导入课程
     *
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importCoursesFromExcel(MultipartFile file);

    /**
     * 生成课程导入模板
     *
     * @return Excel文件
     */
    byte[] generateCourseTemplate();

    /**
     * 获取所有课程简化列表
     *
     * @return 课程简化列表
     */
    java.util.List<com.yupi.springbootinit.model.vo.CourseSimpleVO> listCourseSimple();

    /**
     * 获取当前登录教师讲授的课程简化列表（按 teacher_id 过滤，数据归属隔离）
     *
     * @return 课程简化列表
     */
    java.util.List<com.yupi.springbootinit.model.vo.CourseSimpleVO> listMyCourses();
}