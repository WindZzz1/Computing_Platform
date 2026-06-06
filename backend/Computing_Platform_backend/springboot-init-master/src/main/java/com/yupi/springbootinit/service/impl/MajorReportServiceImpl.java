package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.PenetrationAccountMapper;
import com.yupi.springbootinit.model.dto.report.MajorReportRequest;
import com.yupi.springbootinit.model.vo.report.MajorAchievementRadarVO;
import com.yupi.springbootinit.model.vo.report.PenetrationAccountVO;
import com.yupi.springbootinit.service.MajorReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专业报告服务实现（简化版本，用于快速启动项目）
 *
 * @author YU
 */
@Service
@Slf4j
public class MajorReportServiceImpl extends ServiceImpl<PenetrationAccountMapper, com.yupi.springbootinit.model.entity.Course>
        implements MajorReportService {

    @Resource
    private PenetrationAccountMapper penetrationAccountMapper;

    @Override
    public MajorAchievementRadarVO getRadarChartData(MajorReportRequest request) {
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "雷达图功能待完整实现");
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
        if (SysUserConstant.ROLE_LEADER.equals(userRole)) {
            return true;
        }

        return false;
    }
}