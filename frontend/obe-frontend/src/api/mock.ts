import type { Assessment, Course, Indicator, Objective } from '@/types'

export const courses: Course[] = [
  { id: 1, code: '081203-01', name: '高等数学B', teacher: '李老师', credit: 5, term: '2023-2024 第一学期', studentCount: 180, status: '已锁定' },
  { id: 2, code: '081203-02', name: '大学物理B', teacher: '赵老师', credit: 4, term: '2023-2024 第一学期', studentCount: 160, status: '已锁定' },
  { id: 3, code: 'CS203', name: '数据结构', teacher: '张老师', credit: 4, term: '2023-2024 第一学期', studentCount: 150, status: '待计算' },
  { id: 4, code: 'CS301', name: '操作系统', teacher: '王老师', credit: 4, term: '2023-2024 第一学期', studentCount: 140, status: '已锁定' },
  { id: 5, code: 'CS304', name: '数据库系统', teacher: '刘老师', credit: 3, term: '2023-2024 第一学期', studentCount: 140, status: '已锁定' },
  { id: 6, code: 'CS401', name: '软件工程', teacher: '陈老师', credit: 3, term: '2023-2024 第一学期', studentCount: 120, status: '未提交' }
]

export const indicators: Indicator[] = [
  { id: 1, code: '1.1', name: '工程知识', requirement: '毕业要求1', achievement: 0.78 },
  { id: 2, code: '1.2', name: '问题分析', requirement: '毕业要求1', achievement: 0.61 },
  { id: 3, code: '2.1', name: '复杂工程问题识别', requirement: '毕业要求2', achievement: 0.74 },
  { id: 4, code: '2.2', name: '开放解决方案分析', requirement: '毕业要求2', achievement: 0.76 },
  { id: 5, code: '3.1', name: '研究', requirement: '毕业要求3', achievement: 0.72 },
  { id: 6, code: '3.2', name: '使用现代工具', requirement: '毕业要求3', achievement: 0.73 },
  { id: 7, code: '3.3', name: '工程与社会', requirement: '毕业要求3', achievement: 0.725 },
  { id: 8, code: '8.3', name: '职业规范', requirement: '毕业要求8', achievement: 0.80 }
]

export const objectives: Objective[] = [
  { id: 1, code: '目标1-1', content: '掌握数据结构基本概念，能够解释线性表、树、图等结构特征', achievement: 0.72 },
  { id: 2, code: '目标2-1', content: '能够分析复杂工程问题中的数据组织方式与算法策略', achievement: 0.68 },
  { id: 3, code: '目标2-2', content: '能够建立数据模型，完成算法设计与复杂度分析', achievement: 0.75 },
  { id: 4, code: '目标3-1', content: '能够在实验或项目中实现算法并验证结果', achievement: 0.80 }
]

export const assessments: Assessment[] = [
  { id: 1, name: '平时成绩（作业/测验）', score: 10, objectiveId: 1, method: '过程性' },
  { id: 2, name: '实验（代码、报告）', score: 20, objectiveId: 3, method: '过程性' },
  { id: 3, name: '期中测试', score: 20, objectiveId: 1, method: '闭卷' },
  { id: 4, name: '期末大题', score: 50, objectiveId: 2, method: '闭卷' }
]

export const supportMatrixRows = [
  { course: '高等数学B', weights: { 1: 0.20, 2: 0.40, 3: 0.20, 4: 0.20, 5: 0.20, 6: 0.30, 7: 0.10, 8: 0.00 } as Record<number, number> },
  { course: '大学物理B', weights: { 1: 0.30, 2: 0.40, 3: 0.30, 4: 0.20, 5: 0.10, 6: 0.20, 7: 0.10, 8: 0.30 } as Record<number, number> },
  { course: '数据结构', weights: { 1: 0.20, 2: 0.10, 3: 0.30, 4: 0.20, 5: 0.30, 6: 0.20, 7: 0.20, 8: 0.00 } as Record<number, number> },
  { course: '操作系统', weights: { 1: 0.10, 2: 0.00, 3: 0.10, 4: 0.20, 5: 0.20, 6: 0.10, 7: 0.20, 8: 0.20 } as Record<number, number> },
  { course: '数据库系统', weights: { 1: 0.20, 2: 0.10, 3: 0.10, 4: 0.20, 5: 0.10, 6: 0.20, 7: 0.20, 8: 0.20 } as Record<number, number> },
  { course: '软件工程', weights: { 1: 0.00, 2: 0.00, 3: 0.00, 4: 0.00, 5: 0.10, 6: 0.00, 7: 0.20, 8: 0.30 } as Record<number, number> }
]

export const scoreRows = [
  { sid: '202201001', name: '张三', homework: 8.5, experiment: 18, mid: 16, final: 40 },
  { sid: '202201002', name: '李四', homework: 9.0, experiment: 20, mid: 18, final: 45 },
  { sid: '202201003', name: '王五', homework: 7.5, experiment: 16, mid: 14, final: 35 },
  { sid: '202201004', name: '赵六', homework: 9.5, experiment: 19, mid: 17, final: 42 }
]
