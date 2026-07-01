package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.teachingClass.ClassStudentBindRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassAddRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassQueryRequest;
import com.yupi.springbootinit.model.dto.teachingClass.TeachingClassUpdateRequest;
import com.yupi.springbootinit.model.entity.TeachingClass;
import com.yupi.springbootinit.model.vo.TeachingClassVO;

/**
 * 教学班级服务
 *
 * @author YU
 */
public interface TeachingClassService extends IService<TeachingClass> {

    /**
     * 创建教学班级
     *
     * @param teachingClassAddRequest 新增请求
     * @return 教学班级ID
     */
    Long createTeachingClass(TeachingClassAddRequest teachingClassAddRequest);

    /**
     * 更新教学班级
     *
     * @param teachingClassUpdateRequest 更新请求
     * @return 是否成功
     */
    Boolean updateTeachingClass(TeachingClassUpdateRequest teachingClassUpdateRequest);

    /**
     * 删除教学班级
     *
     * @param id 教学班级ID
     * @return 是否成功
     */
    Boolean deleteTeachingClass(Long id);

    /**
     * 根据ID获取教学班级
     *
     * @param id 教学班级ID
     * @return 教学班级VO
     */
    TeachingClassVO getTeachingClassById(Long id);

    /**
     * 分页查询教学班级
     *
     * @param teachingClassQueryRequest 查询请求
     * @return 分页结果
     */
    Page<TeachingClassVO> pageTeachingClass(TeachingClassQueryRequest teachingClassQueryRequest);

    /**
     * 获取查询条件
     *
     * @param teachingClassQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<TeachingClass> getQueryWrapper(TeachingClassQueryRequest teachingClassQueryRequest);

    /**
     * 获取教学班级VO
     *
     * @param teachingClass 教学班级实体
     * @return 教学班级VO
     */
    TeachingClassVO getTeachingClassVO(TeachingClass teachingClass);

    /**
     * 绑定学生到教学班级
     *
     * @param classStudentBindRequest 绑定请求
     * @return 绑定的学生数量
     */
    Integer bindStudents(ClassStudentBindRequest classStudentBindRequest);

    /**
     * 解绑学生
     *
     * @param classId 教学班级ID
     * @param studentId 学生ID
     * @return 是否成功
     */
    Boolean unbindStudent(Long classId, Long studentId);

    /**
     * 获取教学班级的学生列表
     *
     * @param classId 教学班级ID
     * @return 学生列表
     */
    java.util.List<com.yupi.springbootinit.model.vo.StudentVO> getClassStudents(Long classId);

    /**
     * 批量导入学生到教学班级
     *
     * @param classStudentImportRequest 导入请求
     * @return 导入结果
     */
    java.util.Map<String, Object> importStudents(com.yupi.springbootinit.model.dto.teachingClass.ClassStudentImportRequest classStudentImportRequest);

    /**
     * 通过Excel批量导入学生到教学班级
     *
     * @param classId 教学班级ID
     * @param file Excel文件
     * @return 导入结果
     */
    java.util.Map<String, Object> importStudentsFromExcel(Long classId, org.springframework.web.multipart.MultipartFile file);

    /**
     * 获取当前登录教师主讲的的教学班列表（按 teacher_id 过滤，数据归属隔离）
     *
     * @return 教学班 VO 列表
     */
    java.util.List<com.yupi.springbootinit.model.vo.TeachingClassVO> listMyTeachingClasses();

    /**
     * 通过Excel批量导入教学班
     *
     * @param file Excel文件
     * @return 导入结果
     */
    java.util.Map<String, Object> importTeachingClassesFromExcel(org.springframework.web.multipart.MultipartFile file);
}