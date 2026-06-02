package com.yupi.springbootinit.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.yupi.springbootinit.model.dto.score.StudentScoreImportItem;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生成绩导入监听器
 * 用于解析包含动态列的Excel成绩文件
 *
 * @author YU
 */
@Slf4j
public class StudentScoreImportListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 存储所有解析出的数据
     */
    private final List<StudentScoreImportItem> dataList = new ArrayList<>();

    /**
     * 表头信息（列索引 -> 列名）
     */
    private Map<Integer, String> headers;

    /**
     * 考核点列索引映射（列索引 -> 考核点ID）
     * 这个信息需要从外部传入
     */
    private Map<Integer, String> pointColumnMapping;

    /**
     * 学号列索引
     */
    private Integer studentNoIndex = 0;

    /**
     * 姓名列索引
     */
    private Integer studentNameIndex = 1;

    /**
     * 当前行号
     */
    private int currentRowNum = 0;

    public void setPointColumnMapping(Map<Integer, String> pointColumnMapping) {
        this.pointColumnMapping = pointColumnMapping;
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        // 读取表头（第二行，第一行是合并的课程信息）
        // EasyExcel会把所有行都读取，我们需要跳过第一行
        ReadRowHolder readRowHolder = context.readRowHolder();
        int rowIndex = readRowHolder.getRowIndex();

        // 只处理第二行作为表头
        if (rowIndex == 1) {
            this.headers = headMap;

            // 识别学号和姓名列的索引
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                String columnName = entry.getValue();
                if ("学号".equals(columnName)) {
                    studentNoIndex = entry.getKey();
                } else if ("姓名".equals(columnName)) {
                    studentNameIndex = entry.getKey();
                }
            }

            log.info("解析表头成功，学号列索引：{}，姓名列索引：{}", studentNoIndex, studentNameIndex);
            log.info("考核点列映射：{}", pointColumnMapping);
        }
    }

    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        // 获取实际行号（从0开始）
        ReadRowHolder readRowHolder = context.readRowHolder();
        int rowIndex = readRowHolder.getRowIndex();

        // 跳过前两行（第一行是课程信息，第二行是表头）
        if (rowIndex <= 1) {
            return;
        }

        currentRowNum = rowIndex - 1; // 转换为数据行号

        // 创建导入项
        StudentScoreImportItem item = new StudentScoreImportItem();
        item.setRowNum(currentRowNum);

        try {
            // 提取学号和姓名
            String studentNo = rowData.get(studentNoIndex);
            String studentName = rowData.get(studentNameIndex);

            if (studentNo == null || studentNo.trim().isEmpty()) {
                item.setValid(false);
                item.setErrorMessage("学号为空");
                dataList.add(item);
                return;
            }

            item.setStudentNo(studentNo.trim());
            item.setStudentName(studentName != null ? studentName.trim() : "");

            // 提取考核点成绩
            Map<String, BigDecimal> scores = new HashMap<>();
            for (Map.Entry<Integer, String> entry : pointColumnMapping.entrySet()) {
                Integer columnIndex = entry.getKey();
                String pointId = entry.getValue();

                String scoreStr = rowData.get(columnIndex);
                if (scoreStr != null && !scoreStr.trim().isEmpty()) {
                    try {
                        BigDecimal score = new BigDecimal(scoreStr.trim());
                        scores.put(pointId, score);
                    } catch (NumberFormatException e) {
                        // 成绩格式错误
                        item.setValid(false);
                        item.setErrorMessage("第" + (columnIndex + 1) + "列成绩格式错误：" + scoreStr);
                        dataList.add(item);
                        return;
                    }
                }
            }

            item.setScores(scores);
            item.setValid(true);
            dataList.add(item);

        } catch (Exception e) {
            log.error("解析第{}行数据失败", currentRowNum, e);
            item.setValid(false);
            item.setErrorMessage("数据解析失败：" + e.getMessage());
            dataList.add(item);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel解析完成，共解析{}行数据", dataList.size());
    }

    /**
     * 获取解析后的数据列表
     */
    public List<StudentScoreImportItem> getDataList() {
        return dataList;
    }
}
