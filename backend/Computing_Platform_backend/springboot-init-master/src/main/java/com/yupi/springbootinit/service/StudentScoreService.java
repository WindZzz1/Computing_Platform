package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import com.yupi.springbootinit.model.entity.StudentScore;
import com.yupi.springbootinit.model.vo.ScoreImportResultVO;
import com.yupi.springbootinit.model.vo.ScorePreviewVO;

import java.util.List;

/**
 * 学生考核点成绩服务
 *
 * @author YU
 */
public interface StudentScoreService extends IService<StudentScore> {

    /**
     * 生成成绩录入模板（动态表头）
     * 模板包含：
     * 1. 两行复合表头（第一行显示课程信息，第二行显示具体列名）
     * 2. 学号、姓名列
     * 3. 所有考核点列（格式：A1 期末考试(满分100)）
     * 4. 预填充该班级所有学生的学号和姓名
     *
     * @param classId 教学班级ID
     * @return Excel字节数组
     */
    byte[] generateScoreTemplate(Long classId);

    /**
     * 导入学生成绩
     * 解析Excel文件，校验数据并存入数据库
     *
     * @param classId 教学班级ID
     * @param file Excel文件
     * @return 导入结果
     */
    ScoreImportResultVO importScores(Long classId, MultipartFile file);

    /**
     * 获取教学班级的成绩预览数据
     * 包含所有学生的所有考核点成绩
     *
     * @param classId 教学班级ID
     * @return 成绩预览列表
     */
    List<ScorePreviewVO> getScoresByClass(Long classId);

    /**
     * 更新单条成绩记录
     *
     * @param id 成绩记录ID
     * @param actualScore 实际得分
     * @return 是否成功
     */
    Boolean updateScore(Long id, java.math.BigDecimal actualScore);

    /**
     * 删除教学班级的所有成绩
     *
     * @param classId 教学班级ID
     * @return 是否成功
     */
    Boolean deleteScoresByClassId(Long classId);
}
