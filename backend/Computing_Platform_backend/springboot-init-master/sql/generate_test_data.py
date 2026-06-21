# -*- coding: utf-8 -*-
"""
生成《面向专业认证的毕业要求达成度统一计算平台》完整测试数据。
- 独立专业"软件工程"(id=9000) + 2023 级，id 段 9000+，不干扰现有数据。
- 覆盖完整流程：字典→毕业要求/指标点→课程/教学班/学生→宏观矩阵→课程目标/内部权重→考核点→成绩→三级达成度。
- 三级达成度严格按需求公式计算（一级 Σ得分/Σ满分、二级 Σ(C·w)、三级 Σ(E·W)），与后端一致，报表可直接展示。
- 清理：DELETE FROM <表> WHERE id>=9000;（教学班/学生等无业务 id 冲突，按 id>=9000 清；class_student/student_score 按关联 id 段清）
运行：python generate_test_data.py  →  输出 test_data.sql
"""
import hashlib
from decimal import Decimal, ROUND_HALF_UP

SALT = "yupi"
def pwd(p):
    return hashlib.md5((SALT + p).encode()).hexdigest()

def q(s):
    """SQL 字符串转义"""
    return "'" + str(s).replace("\\", "\\\\").replace("'", "''") + "'"

def dec(v, places=4):
    return Decimal(str(v)).quantize(Decimal("1." + "0"*places), rounding=ROUND_HALF_UP)

lines = []
def ins(table, cols, rows):
    cs = ",".join(cols)
    vals = []
    for r in rows:
        vals.append("(" + ",".join(str(x) for x in r) + ")")
    # 分批，每批 200 行，避免单条过长
    BATCH = 200
    for i in range(0, len(vals), BATCH):
        lines.append(f"INSERT INTO {table} ({cs}) VALUES " + ",\n".join(vals[i:i+BATCH]) + ";")

# ============ 1. 用户（测试账号，密码均 12345678）============
PW = pwd("12345678")
ins("sys_user", ["id","username","password","role_code","college_id","status","is_deleted"], [
    [q(9000), q("leader_se"), q(PW), q("leader"), q(9000), 1, 0],
    [q(9001), q("edu_se"),    q(PW), q("edu"),    "NULL",   1, 0],
    [q(9002), q("teacher_se"),q(PW), q("teacher"),q(9000),  1, 0],
])

# ============ 2. 学院 / 专业 / 学年学期 ============
ins("sys_dict_college", ["id","college_name","is_deleted"], [
    [q(9000), q("软件学院"), 0],
])
ins("sys_dict_major", ["id","major_code","major_name","college_id","is_deleted"], [
    [q(9000), q("080902SE"), q("软件工程"), q(9000), 0],
])
ins("sys_dict_school_year", ["id","year_name","semester_name","is_deleted"], [
    [q(9000), q("2024-2025学年"), q("第二学期"), 0],
])

# ============ 3. 毕业要求 + 指标点 ============
reqs = [
    (9000, "GR1", "工程知识", "能够将数学、自然科学、工程基础和专业知识用于解决复杂工程问题"),
    (9001, "GR2", "问题分析", "能够应用数学、自然科学和工程科学的基本原理，识别、表达、并通过文献研究分析复杂工程问题"),
    (9002, "GR3", "设计/开发解决方案", "能够设计针对复杂工程问题的解决方案，设计满足特定需求的系统、单元或工艺流程"),
    (9003, "GR4", "研究", "能够基于科学原理并采用科学方法对复杂工程问题进行研究"),
]
ins("graduation_requirement", ["id","major_id","requirement_code","requirement_name","description","is_deleted"],
    [[q(i), q(9000), q(c), q(n), q(d), 0] for i,c,n,d in reqs])

inds = []  # (id, req_id, code, name)
ind_data = [
    ("1.1", "数学与自然科学知识"), ("1.2", "工程基础知识"), ("1.3", "专业知识"),
    ("2.1", "问题识别"), ("2.2", "问题表达"), ("2.3", "文献研究分析"),
    ("3.1", "需求分析"), ("3.2", "系统设计"), ("3.3", "单元/工艺设计"),
    ("4.1", "实验设计"), ("4.2", "数据分析与解释"), ("4.3", "科学结论"),
]
iid = 9000
for ridx, (rid, rcode, _, _) in enumerate(reqs):
    for k in range(3):
        code, name = ind_data[ridx*3 + k]
        inds.append((iid, rid, code, name))
        iid += 1
ins("indicator_point", ["id","requirement_id","indicator_code","indicator_name","is_deleted"],
    [[q(i), q(r), q(c), q(n), 0] for i,r,c,n in inds])
ind_id = {code: i for i,r,code,n in inds}  # code -> id

# ============ 4. 课程 / 教学班 ============
courses = [
    (9000, "SE101", "软件工程导论", "必修", "2.5"),
    (9001, "SE102", "数据结构与算法", "必修", "4.0"),
    (9002, "SE103", "操作系统", "必修", "4.0"),
    (9003, "SE104", "计算机网络", "必修", "3.5"),
    (9004, "SE105", "数据库原理", "必修", "3.5"),
    (9005, "SE106", "软件测试", "必修", "3.0"),
]
ins("course", ["id","major_id","course_code","course_name","course_nature","credit","is_deleted"],
    [[q(i), q(9000), q(c), q(n), q(nat), q(cr), 0] for i,c,n,nat,cr in courses])
course_id = {code: i for i,code,n,nat,cr in courses}

ins("teaching_class", ["id","course_id","teacher_id","term_id","class_name","is_deleted"], [
    [q(9000+i), q(ci), q(9002), q(9000), q(f"软工2023-{n}班"), 0]
    for i,(ci,c,n,nat,cr) in enumerate(courses)
])
class_id = {ci: 9000+i for i,(ci,c,n,nat,cr) in enumerate(courses)}  # course_id -> class_id

# ============ 5. 学生 + 班级学生（30 人，每班 5 人）============
stu_rows, cs_rows = [], []
for k in range(30):
    sid = 9000 + k
    cls = class_id[courses[k // 5][0]]   # 每班 5 人
    sno = f"2023010{k+1:02d}"
    sname = f"测试学生{k+1:02d}"
    stu_rows.append([q(sid), q(sno), q(sname), q(9000), q("2023"), q(f"软工2023-{courses[k//5][2]}班"), 0])
    cs_rows.append([q(9000+k), q(cls), q(sid), 0])
ins("student", ["id","student_no","student_name","major_id","grade","class_name","is_deleted"], stu_rows)
ins("class_student", ["id","teaching_class_id","student_id","is_deleted"], cs_rows)

# ============ 6. 宏观支撑矩阵（每指标点 2 课支撑，∑W=1）============
# (indicator_code, [(course_code, W), ...])
matrix = [
    ("1.1", [("SE101",0.4),("SE102",0.6)]),
    ("1.2", [("SE102",0.5),("SE103",0.5)]),
    ("1.3", [("SE103",0.6),("SE104",0.4)]),
    ("2.1", [("SE101",0.5),("SE105",0.5)]),
    ("2.2", [("SE102",0.4),("SE105",0.6)]),
    ("2.3", [("SE104",0.5),("SE106",0.5)]),
    ("3.1", [("SE101",0.3),("SE106",0.7)]),
    ("3.2", [("SE102",0.5),("SE106",0.5)]),
    ("3.3", [("SE103",0.4),("SE104",0.6)]),
    ("4.1", [("SE105",0.5),("SE106",0.5)]),
    ("4.2", [("SE101",0.4),("SE103",0.6)]),
    ("4.3", [("SE104",0.5),("SE105",0.5)]),
]
mid = 9000
matrix_rows = []
course_indicators = {}  # course_id -> set(indicator_id)
indicator_courses = {}  # indicator_id -> [(course_id, W)]
for icode, lst in matrix:
    iid = ind_id[icode]
    indicator_courses[iid] = []
    for ccode, W in lst:
        cid = course_id[ccode]
        matrix_rows.append([q(mid), q(9000), q(cid), q(iid), q(dec(W,4)), 0])
        course_indicators.setdefault(cid, set()).add(iid)
        indicator_courses[iid].append((cid, Decimal(str(W))))
        mid += 1
ins("matrix_course_indicator", ["id","major_id","course_id","indicator_id","total_weight","is_deleted"], matrix_rows)

# ============ 7. 课程目标（每课 3：CO1/CO2/CO3）============
obj_id = 9000
objectives = {}  # course_id -> [(obj_id, code, name)]
obj_meta = [("CO1","知识目标"),("CO2","能力目标"),("CO3","素质目标")]
obj_rows = []
for cid, ccode, cname, nat, cr in courses:
    objectives[cid] = []
    for j,(oc,on) in enumerate(obj_meta):
        objectives[cid].append((obj_id, oc, f"{cname}-{on}"))
        obj_rows.append([q(obj_id), q(cid), q(oc), q(f"{cname}-{on}"), q("课程目标描述（纯文本）"), 0])
        obj_id += 1
ins("course_objective", ["id","course_id","obj_code","obj_name","obj_desc","is_deleted"], obj_rows)

# ============ 8. 内部权重（每课支撑的指标点分配给该课目标，∑w=1 per (course,indicator)）============
# 每课 3 目标轮流承接该课支撑的指标点；每 (course,indicator) 由 1 个目标 w=1.0 承接
wid = 9000
weight_rows = []
course_obj_ind = {}  # (course_id, obj_id) -> [indicator_id]  用于二级计算
obj_of_course_ind = {}  # (course_id, indicator_id) -> obj_id
for cid in objectives:
    inds_of_course = sorted(course_indicators.get(cid, set()))
    objs = [o[0] for o in objectives[cid]]
    for k, iid in enumerate(inds_of_course):
        oid = objs[k % len(objs)]   # 轮流分配
        weight_rows.append([q(wid), q(cid), q(oid), q(iid), q(dec(1.0,4)), 0])
        wid += 1
        course_obj_ind.setdefault((cid, oid), []).append(iid)
        obj_of_course_ind[(cid, iid)] = oid
ins("weight_objective_indicator", ["id","course_id","objective_id","indicator_id","inner_weight","is_deleted"], weight_rows)

# ============ 9. 考核点（每课 4：平时/实验/期中/期末）+ 绑定目标（多对多，weight=1.0）============
apid = 9000
ap_rows, rel_rows = [], []
assessment_points = {}  # course_id -> [(ap_id, code, name, full_score)]
point_config = [("AP1","平时作业",20),("AP2","实验报告",20),("AP3","期中考试",20),("AP4","期末考试",40)]
relid = 9000
for cid, ccode, cname, nat, cr in courses:
    assessment_points[cid] = []
    objs = [o[0] for o in objectives[cid]]
    for j,(pc,pn,full) in enumerate(point_config):
        oid = objs[j % len(objs)]   # 考核点轮流绑目标
        assessment_points[cid].append((apid, pc, pn, Decimal(full)))
        ap_rows.append([q(apid), q(cid), q(pc), q(f"{cname}-{pn}"), q(dec(full,1)), 0])
        rel_rows.append([q(relid), q(apid), q(oid), q(dec(1.0,4)), 0])
        relid += 1
        apid += 1
ins("assessment_point", ["id","course_id","point_code","point_name","full_score","is_deleted"], ap_rows)
ins("rel_point_objective", ["id","point_id","objective_id","weight","is_deleted"], rel_rows)

# 考核点 → 目标 反查
point_to_obj = {}
for cid in assessment_points:
    for j,(apid,pc,pn,full) in enumerate(assessment_points[cid]):
        objs = [o[0] for o in objectives[cid]]
        point_to_obj[apid] = objs[j % len(objs)]
# 目标 → 考核点 反查（按课）
obj_to_points = {}
for cid in assessment_points:
    objs = [o[0] for o in objectives[cid]]
    for j,(apid,pc,pn,full) in enumerate(assessment_points[cid]):
        oid = objs[j % len(objs)]
        obj_to_points.setdefault((cid, oid), []).append(apid)

# ============ 10. 学生成绩（确定性公式：满分的 0.65~0.95）============
scid = 9000
score_rows = []
# scores[(class_id, student_id, point_id)] = actual_score
scores = {}
for k in range(30):
    sid = 9000 + k
    cid = courses[k // 5][0]
    cls = class_id[cid]
    for j,(apid,pc,pn,full) in enumerate(assessment_points[cid]):
        ratio = 0.65 + 0.30 * ((k + j) % 10) / 10.0
        actual = (full * Decimal(str(ratio))).quantize(Decimal("0.1"), rounding=ROUND_HALF_UP)
        scores[(cls, sid, apid)] = actual
        score_rows.append([q(scid), q(cls), q(sid), q(apid), q(actual), q(dec(full,1)), 0])
        scid += 1
ins("student_score", ["id","teaching_class_id","student_id","point_id","actual_score","full_score","is_deleted"], score_rows)

# ============ 11. 三级达成度（按公式计算，与后端一致）============
CALC_TIME = "2026-06-21 10:00:00"

# 一级：学生×目标 C_ij = Σ(目标j考核点得分)/Σ(满分)
# objective_achv[(class_id, student_id, obj_id)] = Decimal
objective_achv = {}
for cid, ccode, cname, nat, cr in courses:
    cls = class_id[cid]
    students = [9000 + (k) for k in range(30) if courses[k//5][0] == cid]
    for oid, oc, on in objectives[cid]:
        pts = obj_to_points.get((cid, oid), [])
        if not pts:
            continue
        sum_full = sum(Decimal(str(ap_full)) for ap in pts for apid,pc,pn,ap_full in [next(p for p in assessment_points[cid] if p[0]==ap)])
        for sid in students:
            sum_actual = sum(scores.get((cls, sid, apid), Decimal("0")) for apid in pts)
            if sum_full and sum_full > 0:
                objective_achv[(cls, sid, oid)] = (sum_actual / sum_full).quantize(Decimal("0.0001"), rounding=ROUND_HALF_UP)

# 一级结果持久化
soa_rows = []
for (cls, sid, oid), val in objective_achv.items():
    cid = next(c for c,cl in class_id.items() if cl == cls)
    oc = next(o[1] for o in objectives[cid] if o[0]==oid)
    on = next(o[2] for o in objectives[cid] if o[0]==oid)
    soa_rows.append([q(9000+len(soa_rows)), q(cls), q(sid), q(oid), q(oc), q(on), q(dec(val,4)), q(CALC_TIME), 0])
ins("student_objective_achievement", ["id","teaching_class_id","student_id","objective_id","objective_code","objective_name","achievement","calculate_time","is_deleted"], soa_rows)

# 班级目标平均 C_j（目标在班内的学生平均）
class_obj_avg = {}  # (class_id, obj_id) -> avg
for (cls, sid, oid), val in objective_achv.items():
    class_obj_avg.setdefault((cls, oid), []).append(val)
for k in class_obj_avg:
    vals = class_obj_avg[k]
    class_obj_avg[k] = (sum(vals)/Decimal(len(vals))).quantize(Decimal("0.0001"), rounding=ROUND_HALF_UP)

# 二级：E_k(班,指标点) = Σ_j(C_j × w_jk)，w 为内部权重（同课同指标点单目标 w=1）
cia_rows = []
course_ind_achv = {}  # (class_id, indicator_id) -> E
for cid, ccode, cname, nat, cr in courses:
    cls = class_id[cid]
    for iid in sorted(course_indicators.get(cid, set())):
        oid = obj_of_course_ind[(cid, iid)]
        cj = class_obj_avg.get((cls, oid), Decimal("0"))
        # w=1.0（同课同指标点单目标承接）
        ek = (cj * Decimal("1.0")).quantize(Decimal("0.0001"), rounding=ROUND_HALF_UP)
        course_ind_achv[(cls, iid)] = ek
        icode = next(c for i,r,c,n in inds if i==iid)
        iname = next(n for i,r,c,n in inds if i==iid)
        cia_rows.append([q(9000+len(cia_rows)), q(cls), q(cid), q(iid), q(icode), q(iname), q(dec(ek,4)), q(CALC_TIME), 0])
ins("course_indicator_achievement", ["id","teaching_class_id","course_id","indicator_id","indicator_code","indicator_name","achievement","calculate_time","is_deleted"], cia_rows)

# 三级：G_k(专业,指标点) = Σ_c(E_ck × W_ck)，c 为课程（教学班），W 为宏观权重
mia_rows = []
for iid in [i for i,r,c,n in inds]:
    g = Decimal("0")
    icode = next(c for i,r,c,n in inds if i==iid)
    iname = next(n for i,r,c,n in inds if i==iid)
    rid = next(r for i,r,c,n in inds if i==iid)
    rcode = next(rc for ri,rc,rn,rd in reqs if ri==rid)
    rname = next(rn for ri,rc,rn,rd in reqs if ri==rid)
    for cid, W in indicator_courses[iid]:
        cls = class_id[cid]
        ek = course_ind_achv.get((cls, iid), Decimal("0"))
        g += ek * W
    g = g.quantize(Decimal("0.0001"), rounding=ROUND_HALF_UP)
    mia_rows.append([q(9000+len(mia_rows)), q(9000), q(9000), q("2023"), q(iid), q(icode), q(iname), q(rid), q(rcode), q(rname), q(dec(g,4)), q(CALC_TIME), 0])
ins("major_indicator_achievement", ["id","major_id","term_id","grade","indicator_id","indicator_code","indicator_name","requirement_id","requirement_code","requirement_name","achievement","calculate_time","is_deleted"], mia_rows)

# ============ 输出 ============
header = """-- ============================================================
-- 测试数据：软件工程专业(id=9000) 2023级 完整流程
-- 生成自 generate_test_data.py，id 段 9000+，不干扰现有数据
-- 测试账号：leader_se / edu_se / teacher_se，密码均 12345678
-- 清理：见文末 cleanup 段
-- ============================================================
USE graduation_achievement;
SET @OLD_FK_CHECKS=@@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS=0;

"""
footer = """

SET FOREIGN_KEY_CHECKS=@OLD_FK_CHECKS;

-- ============================================================
-- 清理本批测试数据（需要时执行）
-- ============================================================
-- DELETE FROM major_indicator_achievement WHERE id>=9000;
-- DELETE FROM course_indicator_achievement WHERE id>=9000;
-- DELETE FROM student_objective_achievement WHERE id>=9000;
-- DELETE FROM student_score WHERE id>=9000;
-- DELETE FROM rel_point_objective WHERE id>=9000;
-- DELETE FROM assessment_point WHERE id>=9000;
-- DELETE FROM weight_objective_indicator WHERE id>=9000;
-- DELETE FROM course_objective WHERE id>=9000;
-- DELETE FROM matrix_course_indicator WHERE id>=9000;
-- DELETE FROM class_student WHERE id>=9000;
-- DELETE FROM student WHERE id>=9000;
-- DELETE FROM teaching_class WHERE id>=9000;
-- DELETE FROM course WHERE id>=9000;
-- DELETE FROM indicator_point WHERE id>=9000;
-- DELETE FROM graduation_requirement WHERE id>=9000;
-- DELETE FROM sys_dict_school_year WHERE id>=9000;
-- DELETE FROM sys_dict_major WHERE id>=9000;
-- DELETE FROM sys_dict_college WHERE id>=9000;
-- DELETE FROM sys_user WHERE id>=9000;
"""
with open("test_data.sql", "w", encoding="utf-8") as f:
    f.write(header)
    f.write("\n".join(lines))
    f.write(footer)

print(f"已生成 test_data.sql，共 {len(lines)} 条 INSERT")
print(f"用户3 / 学院1 / 专业1 / 学年1 / 要求{len(reqs)} / 指标点{len(inds)} / 课程{len(courses)} / 班{len(courses)} / 学生30 / 成绩{len(score_rows)}")
print(f"一级达成度{len(soa_rows)} / 二级{len(cia_rows)} / 三级{len(mia_rows)}")
