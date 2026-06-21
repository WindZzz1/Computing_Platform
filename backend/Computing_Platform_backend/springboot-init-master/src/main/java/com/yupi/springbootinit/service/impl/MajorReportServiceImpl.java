package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.MajorIndicatorAchievementMapper;
import com.yupi.springbootinit.mapper.SysDictMajorMapper;
import com.yupi.springbootinit.mapper.SysDictSchoolYearMapper;
import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.entity.MajorIndicatorAchievement;
import com.yupi.springbootinit.model.entity.SysDictMajor;
import com.yupi.springbootinit.model.entity.SysDictSchoolYear;
import com.yupi.springbootinit.model.vo.report.IndicatorPointAchievementVO;
import com.yupi.springbootinit.model.vo.report.MajorAchievementRadarVO;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;
import com.yupi.springbootinit.service.MajorReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 专业报告服务实现（模块 D-2）。
 * <p>
 * 雷达图（三级达成度可视化）已实现；穿透式台账与 Excel 导出在后续 PR 补全。
 *
 * @author YU
 */
@Service
@Slf4j
public class MajorReportServiceImpl implements MajorReportService {

    @Resource
    private MajorIndicatorAchievementMapper majorIndicatorAchievementMapper;

    @Resource
    private SysDictMajorMapper sysDictMajorMapper;

    @Resource
    private SysDictSchoolYearMapper sysDictSchoolYearMapper;

    @Override
    public MajorAchievementRadarVO getRadarChartData(MajorReportRequest request) {
        Long majorId = request.getMajorId();
        Long termId = request.getTermId();
        String grade = request.getGrade();

        // 前置：三级达成度必须已计算（雷达图直接消费三级结果）
        List<MajorIndicatorAchievement> achievements =
                majorIndicatorAchievementMapper.selectByMajorTermGrade(majorId, termId, grade);
        if (achievements == null || achievements.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "该专业本学期本年级尚未计算三级达成度，请先执行专业级计算");
        }

        SysDictMajor major = sysDictMajorMapper.selectById(majorId);
        SysDictSchoolYear term = sysDictSchoolYearMapper.selectById(termId);

        MajorAchievementRadarVO vo = new MajorAchievementRadarVO();
        vo.setMajorId(majorId);
        vo.setMajorName(major != null ? major.getMajorName() : null);
        vo.setMajorCode(major != null ? major.getMajorCode() : null);
        vo.setYearName(term != null ? term.getYearName() : null);
        vo.setSemesterName(term != null ? term.getSemesterName() : null);
        vo.setGrade(grade);
        vo.setGeneratedTime(new Date());

        List<IndicatorPointAchievementVO> points = achievements.stream().map(a -> {
            IndicatorPointAchievementVO p = new IndicatorPointAchievementVO();
            p.setIndicatorId(a.getIndicatorId());
            p.setIndicatorCode(a.getIndicatorCode());
            p.setIndicatorName(a.getIndicatorName());
            p.setAchievement(a.getAchievement());
            p.setRequirementId(a.getRequirementId());
            p.setRequirementCode(a.getRequirementCode());
            p.setRequirementName(a.getRequirementName());
            return p;
        }).collect(Collectors.toList());
        vo.setIndicatorPoints(points);
        return vo;
    }

    @Override
    public PenetrationAccountVO getPenetrationAccount(MajorReportRequest request) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "穿透式台账功能待完整实现");
    }

    @Override
    public byte[] exportPenetrationAccountExcel(MajorReportRequest request) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "Excel导出功能待完整实现");
    }

    @Override
    public boolean validateMajorPermission(Long majorId, Long userId, String userRole) {
        // 教务管理员可以查看所有专业
        if (SysUserConstant.ROLE_EDU.equals(userRole)) {
            return true;
        }

        // 专业负责人可以查看所有专业（只读权限）
        // 已知遗留：此处未按 user.major_id 做专业归属校验，属跨模块权限模型调整，后续单独收紧。
        if (SysUserConstant.ROLE_LEADER.equals(userRole)) {
            return true;
        }

        return false;
    }
}
