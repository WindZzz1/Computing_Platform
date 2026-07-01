// 第十六周工作汇报 PPT 生成脚本
const pptxgen = require("pptxgenjs");
const React = require("react");
const ReactDOMServer = require("react-dom/server");
const sharp = require("sharp");
const FA = require("react-icons/fa");

// ---------- 调色板（深海军蓝 + 青绿强调，契合工程数据平台） ----------
const C = {
  NAVY:   "0F2A4A",
  NAVY2:  "12365A",
  TEALB:  "1C7293",
  TEAL:   "14B8A6",
  TEALD:  "0D9488",
  MINT:   "5EEAD4",
  LIGHT:  "F4F7FB",
  CARD:   "FFFFFF",
  INK:    "1E293B",
  SUB:    "475569",
  MUTED:  "64748B",
  LINE:   "E2E8F0",
  CORAL:  "EF4444",
  AMBER:  "F59E0B",
  GREEN:  "16A34A",
  PURPLE: "7C3AED",
  GOLD:   "B45309",
};
const FONT = "Microsoft YaHei";
const W = 13.333, H = 7.5;

const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE";
pres.author = "计算平台开发组";
pres.company = "开源软件开发课程";
pres.title = "第十六周工作汇报";

// ---------- 图标渲染 ----------
function svgOf(Comp, color, size = 256) {
  return ReactDOMServer.renderToStaticMarkup(
    React.createElement(Comp, { color, size: String(size) })
  );
}
async function icon(Comp, color = "#14B8A6", size = 256) {
  const png = await sharp(Buffer.from(svgOf(Comp, color, size))).png().toBuffer();
  return "image/png;base64," + png.toString("base64");
}
const ICONS = {};

// ---------- 通用图元工厂（每次新建对象，避免 pptxgenjs 原地修改踩坑） ----------
function rect(slide, x, y, w, h, fill, opts = {}) {
  slide.addShape(pres.shapes.RECTANGLE, {
    x, y, w, h, fill: { color: fill },
    line: opts.line ? { color: opts.line, width: opts.width || 1 } : undefined,
    shadow: opts.shadow ? { type: "outer", color: "0F2A4A", blur: 10, offset: 3, angle: 90, opacity: 0.10 } : undefined,
  });
}
function card(slide, x, y, w, h, accent) {
  rect(slide, x, y, w, h, C.CARD, { shadow: true, line: C.LINE, width: 1 });
  if (accent) rect(slide, x, y, 0.09, h, accent);
}
function oval(slide, x, y, d, fill, lineColor) {
  slide.addShape(pres.shapes.OVAL, {
    x, y, w: d, h: d,
    fill: { color: fill },
    line: lineColor ? { color: lineColor, width: 0 } : undefined,
  });
}
function numCircle(slide, x, y, n, fill = C.TEAL, txt = "FFFFFF", d = 0.5) {
  oval(slide, x, y, d, fill);
  slide.addText(String(n), { x, y, w: d, h: d, align: "center", valign: "middle", fontFace: FONT, fontSize: 18, bold: true, color: txt, margin: 0 });
}
function txt(slide, t, o) {
  slide.addText(t, { fontFace: FONT, color: C.INK, ...o });
}
function downArrow(slide, x, y, h = 0.34, color = C.TEAL) {
  slide.addShape(pres.shapes.DOWN_ARROW, { x, y, w: 0.34, h, fill: { color }, line: { color, width: 0 } });
}

// 顶栏 + 页脚
function chrome(slide, pageNo, dark = false) {
  if (!dark) {
    rect(slide, 0, 0, W, 0.16, C.TEAL);
    txt(slide, "面向专业认证的毕业要求达成度计算平台", { x: 0.6, y: H - 0.42, w: 7, h: 0.3, fontSize: 9.5, color: C.MUTED, margin: 0 });
  }
  txt(slide, "第十六周工作汇报 · 计算平台开发组", { x: W - 6.4, y: H - 0.42, w: 5.8, h: 0.3, fontSize: 9.5, color: dark ? "9DB4CC" : C.MUTED, align: "right", margin: 0 });
  txt(slide, String(pageNo).padStart(2, "0"), { x: W - 0.95, y: H - 0.42, w: 0.4, h: 0.3, fontSize: 10, color: dark ? "9DB4CC" : C.TEALD, bold: true, align: "right", margin: 0 });
}
function slideTitle(slide, kickerText, titleText, iconData) {
  txt(slide, kickerText, { x: 0.6, y: 0.42, w: 8, h: 0.3, fontSize: 12, bold: true, color: C.TEALD, charSpacing: 2, margin: 0 });
  if (iconData) slide.addImage({ data: iconData, x: 0.6, y: 0.82, w: 0.46, h: 0.46 });
  txt(slide, titleText, { x: iconData ? 1.2 : 0.6, y: 0.78, w: 11.4, h: 0.6, fontSize: 27, bold: true, color: C.NAVY, margin: 0 });
}

// 分节页（深色）
function divider(no, label, title, sub, iconData, pageNo) {
  const s = pres.addSlide();
  s.background = { color: C.NAVY };
  rect(s, 0, 0, W, H, C.NAVY);
  rect(s, 0, 0, 0.22, H, C.TEAL);
  // 大号编号水印
  txt(s, no, { x: 8.4, y: 0.4, w: 4.4, h: 4.6, fontSize: 220, bold: true, color: "1A3A5E", align: "right", valign: "middle", margin: 0 });
  txt(s, label, { x: 1.0, y: 2.35, w: 8, h: 0.4, fontSize: 15, bold: true, color: C.MINT, charSpacing: 4, margin: 0 });
  if (iconData) s.addImage({ data: iconData, x: 1.0, y: 2.8, w: 0.7, h: 0.7 });
  txt(s, title, { x: 1.85, y: 2.78, w: 9.5, h: 0.85, fontSize: 40, bold: true, color: "FFFFFF", margin: 0, valign: "middle" });
  if (sub) txt(s, sub, { x: 1.02, y: 3.85, w: 9, h: 0.8, fontSize: 15, color: "B9CDE3", margin: 0 });
  txt(s, "面向专业认证的毕业要求达成度计算平台", { x: 1.0, y: H - 0.55, w: 9, h: 0.3, fontSize: 10, color: "7E96B0", margin: 0 });
  txt(s, String(pageNo).padStart(2, "0"), { x: W - 1.0, y: H - 0.55, w: 0.4, h: 0.3, fontSize: 10, color: "7E96B0", align: "right", margin: 0 });
  return s;
}

(async () => {
  // 预渲染图标
  const pick = (n, col) => icon(FA[n], col);
  ICONS.chart     = await pick("FaChartBar", "#0D9488");
  ICONS.play      = await pick("FaPlayCircle", "#0D9488");
  ICONS.tools     = await pick("FaTools", "#0D9488");
  ICONS.robot     = await pick("FaRobot", "#0D9488");
  ICONS.comments  = await pick("FaComments", "#0D9488");
  ICONS.check     = await pick("FaCheckCircle", "#16A34A");
  ICONS.bug       = await pick("FaBug", "#EF4444");
  ICONS.shield    = await pick("FaShieldAlt", "#1C7293");
  ICONS.lock      = await pick("FaLock", "#B45309");
  ICONS.db        = await pick("FaDatabase", "#7C3AED");
  ICONS.branch    = await pick("FaCodeBranch", "#0D9488");
  ICONS.github    = await pick("FaGithub", "#FFFFFF");
  ICONS.excel     = await pick("FaFileExcel", "#16A34A");
  ICONS.pdf       = await pick("FaFilePdf", "#EF4444");
  ICONS.layer     = await pick("FaLayerGroup", "#0D9488");
  ICONS.export    = await pick("FaFileExport", "#1C7293");
  ICONS.usershield = await pick("FaUserShield", "#1C7293");
  ICONS.server    = await pick("FaServer", "#B45309");
  ICONS.code      = await pick("FaCode", "#7C3AED");
  ICONS.doc       = await pick("FaFileAlt", "#0D9488");
  ICONS.rocket    = await pick("FaRocket", "#FFFFFF");
  ICONS.warn      = await pick("FaExclamationTriangle", "#F59E0B");
  ICONS.light     = await pick("FaLightbulb", "#F59E0B");
  ICONS.git       = await pick("FaGitAlt", "#FFFFFF");
  ICONS.q         = await pick("FaQuestionCircle", "#FFFFFF");
  ICONS.users     = await pick("FaUsers", "#0D9488");
  ICONS.sync      = await pick("FaSyncAlt", "#1C7293");
  ICONS.clip      = await pick("FaClipboardCheck", "#16A34A");
  ICONS.cog       = await pick("FaCogs", "#0D9488");

  // ===================== 1. 封面 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.NAVY };
    rect(s, 0, 0, W, H, C.NAVY);
    rect(s, 0, 0, 0.28, H, C.TEAL);
    rect(s, 0.28, 0, 0.06, H, C.MINT);
    txt(s, "开源软件开发 · 小组周报", { x: 1.1, y: 1.55, w: 8, h: 0.4, fontSize: 16, bold: true, color: C.MINT, charSpacing: 4, margin: 0 });
    txt(s, "工程教育认证", { x: 1.06, y: 2.15, w: 11, h: 1.0, fontSize: 50, bold: true, color: "FFFFFF", margin: 0 });
    txt(s, "毕业要求达成度统一计算平台", { x: 1.08, y: 3.18, w: 11, h: 1.0, fontSize: 50, bold: true, color: C.MINT, margin: 0 });
    rect(s, 1.12, 4.42, 3.4, 0.05, C.TEAL);
    txt(s, "工作进展 · 功能演示 · 难点攻克 · AI 编程实践 · 组间交流", { x: 1.1, y: 4.62, w: 11, h: 0.4, fontSize: 16, color: "C7D6E8", margin: 0 });
    txt(s, "计算平台开发组", { x: 1.1, y: 5.5, w: 6, h: 0.4, fontSize: 18, bold: true, color: "FFFFFF", margin: 0 });
    txt(s, "汇报周期：第十六周（2026.06.15 – 06.21）   |   仓库：WindZzz1/Computing_Platform", { x: 1.1, y: 5.95, w: 11, h: 0.4, fontSize: 12.5, color: "9DB4CC", margin: 0 });
    txt(s, "Spring Boot · Vue3 · MySQL · EasyExcel · PDFBox", { x: 1.1, y: 6.95, w: 11, h: 0.3, fontSize: 10.5, color: "6E86A2", margin: 0 });
  }

  // ===================== 2. 目录 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 2);
    txt(s, "AGENDA", { x: 0.6, y: 0.55, w: 8, h: 0.35, fontSize: 13, bold: true, color: C.TEALD, charSpacing: 6, margin: 0 });
    txt(s, "本次汇报内容", { x: 0.6, y: 0.92, w: 11, h: 0.6, fontSize: 30, bold: true, color: C.NAVY, margin: 0 });
    const items = [
      ["01", "工作进展", "成员分工 · 项目本周进展", ICONS.chart],
      ["02", "功能演示", "模块 D 报表导出 · 响应拦截器", ICONS.play],
      ["03", "难点攻克", "六大技术难题及解决经验", ICONS.tools],
      ["04", "小组分享", "AI 编程可操作、可验证实践", ICONS.robot],
      ["05", "组间交流", "跨组提问与本组回答", ICONS.comments],
    ];
    items.forEach((it, i) => {
      const col = i < 3 ? i : i - 3;
      const row = i < 3 ? 0 : 1;
      const x = 0.6 + col * 4.18;
      const y = 2.0 + row * 2.35;
      card(s, x, y, 3.95, 2.1, C.TEAL);
      txt(s, it[0], { x: x + 0.28, y: y + 0.22, w: 2, h: 0.7, fontSize: 40, bold: true, color: C.TEALD, margin: 0 });
      s.addImage({ data: it[3], x: x + 3.25, y: y + 0.28, w: 0.42, h: 0.42 });
      txt(s, it[1], { x: x + 0.3, y: y + 0.98, w: 3.4, h: 0.45, fontSize: 19, bold: true, color: C.NAVY, margin: 0 });
      txt(s, it[2], { x: x + 0.3, y: y + 1.42, w: 3.5, h: 0.5, fontSize: 11.5, color: C.SUB, margin: 0 });
    });
  }

  // ===================== 3. 本周数据概览 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 3);
    slideTitle(s, "OVERVIEW · 本周一览", "用数字看本周产出", ICONS.chart);
    const stats = [
      ["57", "次提交 commit", C.NAVY],
      ["28+", "个 PR 合并", C.TEALD],
      ["88", "个文件变更", C.TEALB],
      ["+5283 / −431", "代码行增删", C.PURPLE],
      ["4", "名成员协作", C.GOLD],
      ["44", "个单元测试", C.GREEN],
      ["6", "大类难题攻克", C.CORAL],
      ["3", "报表模块落地", C.TEALB],
    ];
    stats.forEach((st, i) => {
      const col = i % 4, row = Math.floor(i / 4);
      const x = 0.6 + col * 3.07;
      const y = 2.05 + row * 2.0;
      card(s, x, y, 2.85, 1.75, st[2]);
      txt(s, st[0], { x: x + 0.25, y: y + 0.3, w: 2.5, h: 0.85, fontSize: 36, bold: true, color: st[2], margin: 0, valign: "middle" });
      txt(s, st[1], { x: x + 0.27, y: y + 1.18, w: 2.5, h: 0.4, fontSize: 12.5, color: C.SUB, margin: 0 });
    });
    txt(s, "数据来源：git log（2026-06-15 ~ 06-21，全分支）+ 开发进度交接.md", { x: 0.6, y: 6.35, w: 12, h: 0.3, fontSize: 10, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 4. 团队分工 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 4);
    slideTitle(s, "TEAM · 成员分工", "本周各成员工作内容", ICONS.users);
    const rows = [
      ["后端 & 集成", "于佳杰 / WindZzz1", "模块 D 报表（D-1/D-2/D-3）全量补全；T1 致命 + T2 严重问题修复；P1 优化；28 个 PR 流程编排与 E2E 验收", C.NAVY],
      ["前端开发", "lzzpp", "模块 D 前端联调；编辑对话框对接后端 detail 端点；教师集成入口与角色隔离优化", C.TEALD],
      ["前端集成", "lzzxinji", "前端 PR 合并协调、跨分支集成（detail-fetch / teacher-flow 等）", C.TEALB],
      ["矩阵模块", "IStarrrr", "矩阵指标点按专业过滤 + 归属校验；修正指标点删除引用校验的结果表名", C.PURPLE],
    ];
    const head = ["工作流", "成员", "本周主要工作"];
    const data = [head.map(h => ({ text: h, options: { fill: { color: C.NAVY }, color: "FFFFFF", bold: true, align: "center", valign: "middle" } }))];
    rows.forEach(r => data.push([
      { text: r[0], options: { bold: true, color: r[3], fill: { color: "FFFFFF" }, valign: "middle" } },
      { text: r[1], options: { bold: true, color: C.INK, fill: { color: "FFFFFF" }, valign: "middle" } },
      { text: r[2], options: { color: C.SUB, fill: { color: "FFFFFF" }, valign: "middle" } },
    ]));
    s.addTable(data, {
      x: 0.6, y: 1.95, w: 12.13, colW: [2.0, 2.7, 7.43],
      rowH: 1.02, fontSize: 12.5, fontFace: FONT, color: C.INK,
      border: { type: "solid", pt: 1, color: C.LINE },
      align: "left", valign: "middle",
    });
    txt(s, "协作方式：GitHub Fork → 特性分支（fix/…、feat/…）→ Issue 编号关联 → merge-commit 合并；PR 正文首行 Closes #Issue 自动关闭。", { x: 0.6, y: 6.35, w: 12.1, h: 0.5, fontSize: 11, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 5. 项目主线 & 计算传导链 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 5);
    slideTitle(s, "CONTEXT · 项目主线", "本周两条主线 + 三级达成度计算传导链", ICONS.layer);
    // 左：两条主线
    card(s, 0.6, 1.95, 4.35, 4.5, C.TEAL);
    txt(s, "本周两条主线", { x: 0.85, y: 2.12, w: 4, h: 0.4, fontSize: 16, bold: true, color: C.NAVY, margin: 0 });
    txt(s, [
      { text: "主线一 · 模块 D 报表补全", options: { bold: true, color: C.TEALD, breakLine: true, fontSize: 13 } },
      { text: "上周 D-1/D-2/D-3 全部 throw \"暂未实现\" 的空壳 stub，本周 6 个报表接口 + Excel×3 + PDF×2 全部落地可用。", options: { color: C.SUB, breakLine: true, fontSize: 11.5, paraSpaceAfter: 8 } },
      { text: "主线二 · 质量加固（T1/T2）", options: { bold: true, color: C.TEALD, breakLine: true, fontSize: 13 } },
      { text: "针对计算正确性、权限越权、事务原子性等 11 个致命/严重问题逐一修复，并补齐 P1 工程优化。", options: { color: C.SUB, fontSize: 11.5 } },
    ], { x: 0.85, y: 2.6, w: 3.9, h: 3.6, margin: 0, valign: "top" });
    // 右：三级传导链
    card(s, 5.2, 1.95, 7.55, 4.5, C.NAVY);
    txt(s, "三级达成度计算传导链（自下而上）", { x: 5.45, y: 2.12, w: 7, h: 0.4, fontSize: 15, bold: true, color: C.NAVY, margin: 0 });
    const steps = [
      ["考核点得分", "student_score（学生 × 指标点得分）", C.MUTED],
      ["一级 · 课程目标", "C_ij = Σ实际得分 / Σ目标满分   [教师触发]", C.TEALB],
      ["二级 · 课程指标点", "E_k = Σ(C_j × w_jk)，Σw = 1.0   [教师触发]", C.TEALD],
      ["三级 · 专业指标点", "G_k = Σ(E_c × W_c)，ΣW = 1.0   [专业负责人 / 教务]", C.NAVY],
    ];
    steps.forEach((st, i) => {
      const y = 2.62 + i * 0.92;
      rect(s, 5.45, y, 7.05, 0.74, st[2]);
      txt(s, st[0], { x: 5.62, y, w: 2.7, h: 0.74, fontSize: 12.5, bold: true, color: "FFFFFF", valign: "middle", margin: 0 });
      txt(s, st[1], { x: 8.2, y, w: 4.2, h: 0.74, fontSize: 11, color: "EAF2F8", valign: "middle", margin: 0 });
      if (i < 3) downArrow(s, 8.86, y + 0.76, 0.14, C.TEAL);
    });
    txt(s, "D-1 = 一/二级课程报表  ·  D-2 = 三级专业（雷达图 + 穿透台账）  ·  D-3 = 专业指标点达成度", { x: 5.45, y: 6.28, w: 7.2, h: 0.3, fontSize: 10.5, color: C.TEALD, italic: true, bold: true, margin: 0 });
  }

  // -------- 分节：工作进展 --------
  divider("01", "PART 01 · WORK PROGRESS", "工作进展", "成员分工 · 项目本周进展", ICONS.chart, 6);

  // ===================== 6. 模块 D 报表全面补全（总览） =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 7);
    slideTitle(s, "PROGRESS · 模块 D", "报表模块从「空壳」到「全量可用」", ICONS.export);
    // before / after
    card(s, 0.6, 1.95, 4.3, 1.35, C.CORAL);
    s.addImage({ data: ICONS.warn, x: 0.85, y: 2.18, w: 0.4, h: 0.4 });
    txt(s, "上周状态", { x: 1.32, y: 2.12, w: 3, h: 0.4, fontSize: 14, bold: true, color: C.CORAL, margin: 0 });
    txt(s, "D-1/D-2/D-3 全部 throw \"暂未实现\"，报表接口未纳入 JWT 白名单（一直返回「未登录」）。", { x: 0.85, y: 2.55, w: 3.85, h: 0.7, fontSize: 11, color: C.SUB, margin: 0 });
    card(s, 5.05, 1.95, 7.7, 1.35, C.GREEN);
    s.addImage({ data: ICONS.check, x: 5.3, y: 2.18, w: 0.4, h: 0.4 });
    txt(s, "本周成果", { x: 5.77, y: 2.12, w: 4, h: 0.4, fontSize: 14, bold: true, color: C.GREEN, margin: 0 });
    txt(s, "6 个报表接口落地 + Excel 导出 ×3 + PDF 导出 ×2；JWT 白名单修复；雷达图 / 穿透台账 / 专业指标点全部可用。", { x: 5.3, y: 2.55, w: 7.25, h: 0.7, fontSize: 11, color: C.SUB, margin: 0 });
    // 三个模块卡
    const m = [
      ["D-1", "课程目标达成度评价表", "数据接口 + Excel + PDF（嵌入 CJK 字体）", C.TEALB, "#114 D-1"],
      ["D-2", "专业雷达图 + 穿透式台账", "MajorScopeHelper 复用 · 五层组装 · Excel 5 sheet", C.TEALD, "#126/#128/#130 D-2"],
      ["D-3", "专业指标点达成度", "Excel + PDF 导出（专业级权限）", C.NAVY, "#144 D-3"],
    ];
    m.forEach((it, i) => {
      const x = 0.6 + i * 4.18;
      card(s, x, 3.55, 3.95, 3.0, it[3]);
      txt(s, it[0], { x: x + 0.28, y: 3.72, w: 2, h: 0.6, fontSize: 30, bold: true, color: it[3], margin: 0 });
      txt(s, it[1], { x: x + 0.3, y: 4.4, w: 3.5, h: 0.7, fontSize: 15, bold: true, color: C.NAVY, margin: 0 });
      txt(s, it[2], { x: x + 0.3, y: 5.05, w: 3.5, h: 0.9, fontSize: 11.5, color: C.SUB, margin: 0 });
      rect(s, x + 0.3, 6.05, 3.35, 0.34, C.LIGHT);
      txt(s, it[4], { x: x + 0.3, y: 6.05, w: 3.35, h: 0.34, fontSize: 10.5, bold: true, color: it[3], align: "center", valign: "middle", margin: 0 });
    });
  }

  // ===================== 7. D-1 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 8);
    slideTitle(s, "PROGRESS · D-1", "课程目标达成情况评价表", ICONS.doc);
    const b = [
      ["数据接口", "CourseAchievementReportServiceImpl.generateReportData() 返回完整 VO，PDF/Excel 直接渲染。"],
      ["Excel 导出", "POST /course-report/export/excel —— 课程目标达成度明细，列：目标、满分、平均得分、达成度。"],
      ["PDF 导出", "POST /course-report/export/pdf —— 嵌入 LXGW WenKai 中文字体，PdfTableRenderer 表格化渲染。"],
      ["顺带修复", "报表接口纳入 JWT 拦截器白名单（此前报表接口一直返回「未登录」）。"],
    ];
    b.forEach((it, i) => {
      const y = 2.0 + i * 1.02;
      card(s, 0.6, y, 12.13, 0.88, C.TEALB);
      numCircle(s, 0.85, y + 0.19, i + 1, C.TEALD);
      txt(s, it[0], { x: 1.55, y, w: 2.6, h: 0.88, fontSize: 14, bold: true, color: C.NAVY, valign: "middle", margin: 0 });
      txt(s, it[1], { x: 4.2, y, w: 8.3, h: 0.88, fontSize: 12, color: C.SUB, valign: "middle", margin: 0 });
    });
    txt(s, "关键文件：service/impl/CourseAchievementReportServiceImpl.java · controller/CourseAchievementReportController.java", { x: 0.6, y: 6.3, w: 12.1, h: 0.3, fontSize: 10.5, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 8. D-2 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 9);
    slideTitle(s, "PROGRESS · D-2", "专业雷达图 + 穿透式台账", ICONS.layer);
    const b = [
      ["雷达图", "POST /major-report/radar-data —— 抽取 MajorScopeHelper 复用专业/学年/年级 scope，前端直接画图。"],
      ["穿透台账·组装", "五层穿透：major_indicator → course_indicator → student_objective → student_score，废弃坏 PenetrationAccountMapper。"],
      ["穿透台账·导出", "POST /major-report/export/account-excel —— 5 个 sheet 汇总，AssessmentPointAccount VO 对齐前端。"],
      ["权限", "@AuthCheck(anyRole = ROLE_LEADER, ROLE_EDU) —— 专业负责人 / 教务可访问（#110 修复后真正生效）。"],
    ];
    b.forEach((it, i) => {
      const y = 2.0 + i * 1.02;
      card(s, 0.6, y, 12.13, 0.88, C.TEALD);
      numCircle(s, 0.85, y + 0.19, i + 1, C.TEALD);
      txt(s, it[0], { x: 1.55, y, w: 2.9, h: 0.88, fontSize: 14, bold: true, color: C.NAVY, valign: "middle", margin: 0 });
      txt(s, it[1], { x: 4.5, y, w: 8.0, h: 0.88, fontSize: 12, color: C.SUB, valign: "middle", margin: 0 });
    });
    txt(s, "关键文件：service/impl/MajorReportServiceImpl.java · manager/MajorScopeHelper.java", { x: 0.6, y: 6.3, w: 12.1, h: 0.3, fontSize: 10.5, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 9. D-3 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 10);
    slideTitle(s, "PROGRESS · D-3", "专业指标点达成度导出", ICONS.export);
    const b = [
      ["Excel 导出", "POST /major-report/export/indicator-excel —— 各毕业要求指标点 G_k 达成度汇总表。"],
      ["PDF 导出", "POST /major-report/export/indicator-pdf —— 复用嵌入 CJK 字体方案，专业级报表。"],
      ["数据来源", "major_indicator_achievement 表（三级计算结果 G_k = Σ(E_c × W_c)）。"],
      ["验收", "E2E：admin 临时放行后真实数据导出，PDF 头部 %PDF-1.4、中文正常、体积约 30KB。"],
    ];
    b.forEach((it, i) => {
      const y = 2.0 + i * 1.02;
      card(s, 0.6, y, 12.13, 0.88, C.NAVY);
      numCircle(s, 0.85, y + 0.19, i + 1, C.NAVY, "FFFFFF");
      txt(s, it[0], { x: 1.55, y, w: 2.6, h: 0.88, fontSize: 14, bold: true, color: C.NAVY, valign: "middle", margin: 0 });
      txt(s, it[1], { x: 4.2, y, w: 8.3, h: 0.88, fontSize: 12, color: C.SUB, valign: "middle", margin: 0 });
    });
    txt(s, "至此模块 D 三个子报表（D-1/D-2/D-3）全部从空壳补全为可用功能。", { x: 0.6, y: 6.3, w: 12.1, h: 0.3, fontSize: 10.5, color: C.TEALD, italic: true, bold: true, margin: 0 });
  }

  // ===================== 10. T1 致命问题 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 11);
    slideTitle(s, "QUALITY · T1 致命问题", "影响计算正确性 / 安全的 5 个致命问题", ICONS.bug);
    const head = ["编号", "问题类别", "PR", "解决方案"];
    const rows = [
      ["T1-3", "鉴权越权", "#110", "AuthInterceptor 真正校验 anyRole（原先只读 mustRole，多角色接口对任意登录用户开放）"],
      ["T1-1", "计算丢数据", "#111", "calculateAchievement 失败时 setRollbackOnly() 显式回滚，避免「先删后插」中途失败丢数据"],
      ["T1-2", "跨专业串数据", "#112", "MajorCalculationServiceImpl 按 course.major_id + grade 过滤教学班（原空实现→跨专业串数据）"],
      ["T1-4", "重复计算", "#113", "新增 GradesheetStatusHelper 推断三态（未提交/已提交/已锁定）+ forceRecalculate 防重算 + 看板状态"],
      ["D-1", "报表 401", "#114", "报表接口纳入 JWT 白名单（原一直返回「未登录」）+ D-1 数据接口与 Excel"],
    ];
    const data = [head.map(h => ({ text: h, options: { fill: { color: C.CORAL }, color: "FFFFFF", bold: true, align: "center", valign: "middle" } }))];
    rows.forEach(r => data.push(r.map((c, i) => ({ text: c, options: { bold: i < 2, color: i < 2 ? C.NAVY : C.SUB, fill: { color: "FFFFFF" }, valign: "middle" } }))));
    s.addTable(data, {
      x: 0.6, y: 1.95, w: 12.13, colW: [1.0, 1.85, 1.0, 8.28],
      rowH: 0.82, fontSize: 11.5, fontFace: FONT, color: C.INK,
      border: { type: "solid", pt: 1, color: C.LINE }, align: "left", valign: "middle",
    });
    txt(s, "每个 PR 含编译通过 + 单测（#110=9 / #112=5 / #113=4）或 E2E 验证 + 详细正文。", { x: 0.6, y: 6.35, w: 12.1, h: 0.4, fontSize: 10.5, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 11. T2 严重问题 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 12);
    slideTitle(s, "QUALITY · T2 严重问题", "6 个严重问题 + 1 个 hotfix 合并修复", ICONS.shield);
    const head = ["编号", "问题类别", "PR", "解决方案"];
    const rows = [
      ["hotfix", "编译失败", "#115", "补回 #112/#113 合并冲突丢失的 StudentMapper 注入（曾导致 main 编译失败）"],
      ["T2-6", "公式偏差", "#116", "一级达成度公式改回规约 Σ得分/Σ满分（原为加权平均，结果不同）"],
      ["T2-3", "横向越权", "#117", "新增 OwnershipHelper，11 处写操作加数据级归属校验（班级 teacher_id / 课程归属）"],
      ["T2-1", "角色错误", "#119", "宏观矩阵配置接口 ROLE_EDU → ROLE_LEADER（规约 A-4 要求专业负责人）"],
      ["T2-5", "部分落库", "#121", "三个 Excel 批量导入改原子，任一行失败整批回滚（原先部分成功落库）"],
      ["T2-4", "跨班冲突", "#123", "student_score 唯一键加 class_id：uk_student_point → uk_class_student_point"],
    ];
    const data = [head.map(h => ({ text: h, options: { fill: { color: C.AMBER }, color: "FFFFFF", bold: true, align: "center", valign: "middle" } }))];
    rows.forEach(r => data.push(r.map((c, i) => ({ text: c, options: { bold: i < 2, color: i < 2 ? C.NAVY : C.SUB, fill: { color: "FFFFFF" }, valign: "middle" } }))));
    s.addTable(data, {
      x: 0.6, y: 1.95, w: 12.13, colW: [1.1, 2.0, 1.1, 7.93],
      rowH: 0.7, fontSize: 11, fontFace: FONT, color: C.INK,
      border: { type: "solid", pt: 1, color: C.LINE }, align: "left", valign: "middle",
    });
    txt(s, "GitHub Issues #118/#120/#122 已通过 PR 正文 Closes #N 自动关闭；远程库 schema 已 SHOW INDEX 验证。", { x: 0.6, y: 6.45, w: 12.1, h: 0.4, fontSize: 10.5, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 12. P1 + 矩阵 + 前端 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 13);
    slideTitle(s, "QUALITY · P1 + 其他", "P1 工程优化、矩阵模块、前端联调", ICONS.cog);
    const cards = [
      ["前端响应拦截器", "#134", "40100（未登录）→ 清登录态 + 跳 /login，原先只弹 error。", C.TEALD, ICONS.sync],
      ["导入原子化", "#136", "CourseServiceImpl.importCourses 去逐行 catch，任一行失败整批回滚。", C.TEALB, ICONS.clip],
      ["HikariCP keepalive", "#138", "max-lifetime/keepalive-time/connection-test-query，解决远程 MySQL 空闲连接 stale。", C.GOLD, ICONS.server],
      ["死代码清理", "#140", "删除 mock.ts / dict.ts、旧 types、GradeEntryServiceImpl.getCurrentUserId。", C.MUTED, ICONS.code],
      ["矩阵模块", "—", "按专业过滤矩阵指标点 + 归属校验；修正删除引用校验结果表名（IStarrrr）。", C.PURPLE, ICONS.layer],
      ["前端联调", "—", "D 模块编辑对话框对接后端 detail 端点；教师集成入口优化（lzzpp）。", C.GREEN, ICONS.users],
    ];
    cards.forEach((it, i) => {
      const col = i % 3, row = Math.floor(i / 3);
      const x = 0.6 + col * 4.18;
      const y = 1.95 + row * 2.18;
      card(s, x, y, 3.95, 2.0, it[3]);
      s.addImage({ data: it[4], x: x + 0.28, y: y + 0.24, w: 0.4, h: 0.4 });
      txt(s, it[0], { x: x + 0.8, y: y + 0.22, w: 3, h: 0.45, fontSize: 14.5, bold: true, color: C.NAVY, margin: 0, valign: "middle" });
      txt(s, it[1], { x: x + 0.3, y: y + 0.72, w: 3.4, h: 0.3, fontSize: 11, bold: true, color: it[3], margin: 0 });
      txt(s, it[2], { x: x + 0.3, y: y + 1.05, w: 3.45, h: 0.85, fontSize: 11, color: C.SUB, margin: 0 });
    });
  }

  // -------- 分节：功能演示 --------
  divider("02", "PART 02 · DEMO", "功能演示", "本周新功能现场演示", ICONS.play, 14);

  // ===================== 13. 演示总览 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 15);
    slideTitle(s, "DEMO · 演示总览", "四个演示项 · 现场可复现", ICONS.play);
    txt(s, "环境：admin / 12345678 登录  ·  后端 http://localhost:8101/api  ·  前端 http://localhost:5173", { x: 0.6, y: 1.7, w: 12, h: 0.4, fontSize: 13, color: C.TEALD, bold: true, margin: 0 });
    const demos = [
      ["①", "D-1 课程报表", "导出 Excel + PDF（验证中文不乱码）", ICONS.excel, C.GREEN],
      ["②", "D-2 雷达图 + 台账", "雷达图渲染 + 穿透台账 Excel（5 sheet）", ICONS.layer, C.TEALD],
      ["③", "D-3 专业指标点", "专业级 Excel + PDF 导出", ICONS.pdf, C.CORAL],
      ["④", "前端响应拦截器", "token 失效 → 自动登出跳登录", ICONS.lock, C.GOLD],
    ];
    demos.forEach((d, i) => {
      const x = 0.6 + (i % 2) * 6.18;
      const y = 2.25 + Math.floor(i / 2) * 2.0;
      card(s, x, y, 5.95, 1.8, d[4]);
      txt(s, d[0], { x: x + 0.3, y: y + 0.25, w: 0.9, h: 0.7, fontSize: 34, bold: true, color: d[4], margin: 0, valign: "middle" });
      s.addImage({ data: d[3], x: x + 1.15, y: y + 0.3, w: 0.45, h: 0.45 });
      txt(s, d[1], { x: x + 1.75, y: y + 0.28, w: 4, h: 0.5, fontSize: 16, bold: true, color: C.NAVY, margin: 0, valign: "middle" });
      txt(s, d[2], { x: x + 1.75, y: y + 0.82, w: 4, h: 0.7, fontSize: 12, color: C.SUB, margin: 0 });
    });
    txt(s, "演示后每项给出「验证点」（文件大小、%PDF 头、sheet 数、跳转行为），做到可验证。", { x: 0.6, y: 6.4, w: 12, h: 0.4, fontSize: 11, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 14. 演示 D-1 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 16);
    slideTitle(s, "DEMO · ① D-1", "课程目标达成度评价表 · Excel + PDF", ICONS.excel);
    const steps = [
      ["教师登录", "用主讲教师账号进入某教学班「课程报表」页，确认成绩已提交。"],
      ["导出 Excel", "点击「导出 Excel」→ 下载课程目标达成度明细表。"],
      ["导出 PDF", "点击「导出 PDF」→ 下载嵌入中文字体的 PDF。"],
      ["验证", "Excel 列完整；PDF 头部 %PDF-1.4、中文不乱码、体积约 30KB。"],
    ];
    steps.forEach((st, i) => {
      const y = 2.0 + i * 0.95;
      numCircle(s, 0.6, y + 0.05, i + 1, C.GREEN);
      txt(s, st[0], { x: 1.25, y, w: 2.4, h: 0.7, fontSize: 14, bold: true, color: C.NAVY, valign: "middle", margin: 0 });
      txt(s, st[1], { x: 3.7, y, w: 4.5, h: 0.7, fontSize: 12, color: C.SUB, valign: "middle", margin: 0 });
    });
    // 右侧验证卡
    card(s, 8.5, 1.95, 4.25, 4.3, C.GREEN);
    s.addImage({ data: ICONS.clip, x: 8.75, y: 2.18, w: 0.42, h: 0.42 });
    txt(s, "验证点（可复现）", { x: 9.25, y: 2.18, w: 3.3, h: 0.45, fontSize: 15, bold: true, color: C.GREEN, margin: 0, valign: "middle" });
    [
      "PDF 头部为 %PDF-1.4",
      "中文字符正常显示",
      "PDF 体积约 30KB（字体 subset）",
      "Excel 列：目标 / 满分 / 得分 / 达成度",
      "接口已纳入 JWT 白名单",
    ].forEach((v, i) => {
      txt(s, [
        { text: "✓  ", options: { bold: true, color: C.GREEN } },
        { text: v, options: { color: C.SUB } },
      ], { x: 8.78, y: 2.75 + i * 0.62, w: 3.8, h: 0.6, fontSize: 12, margin: 0, valign: "middle" });
    });
  }

  // ===================== 15. 演示 D-2 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 17);
    slideTitle(s, "DEMO · ② D-2", "专业雷达图 + 穿透式台账", ICONS.layer);
    const steps = [
      ["专业负责人登录", "进入「专业报表」→ 选择专业 / 学年 / 年级。"],
      ["雷达图", "查看专业各指标点 G_k 达成度雷达图（前端 ECharts 渲染）。"],
      ["穿透台账", "点击「导出台账」→ Excel 5 sheet（指标点逐层穿透到学生得分）。"],
      ["验证", "雷达图数据非空；Excel 5 sheet 标签齐全；数据与三级计算结果一致。"],
    ];
    steps.forEach((st, i) => {
      const y = 2.0 + i * 0.95;
      numCircle(s, 0.6, y + 0.05, i + 1, C.TEALD);
      txt(s, st[0], { x: 1.25, y, w: 2.6, h: 0.7, fontSize: 14, bold: true, color: C.NAVY, valign: "middle", margin: 0 });
      txt(s, st[1], { x: 3.9, y, w: 4.3, h: 0.7, fontSize: 12, color: C.SUB, valign: "middle", margin: 0 });
    });
    card(s, 8.5, 1.95, 4.25, 4.3, C.TEALD);
    s.addImage({ data: ICONS.clip, x: 8.75, y: 2.18, w: 0.42, h: 0.42 });
    txt(s, "验证点（可复现）", { x: 9.25, y: 2.18, w: 3.3, h: 0.45, fontSize: 15, bold: true, color: C.TEALD, margin: 0, valign: "middle" });
    [
      "雷达图按 majorId+grade 正确过滤",
      "台账 5 个 sheet 标签齐全",
      "穿透链：指标点→课程目标→学生",
      "权限：ROLE_LEADER / ROLE_EDU",
      "管理员被 403（符合数据隔离）",
    ].forEach((v, i) => {
      txt(s, [
        { text: "✓  ", options: { bold: true, color: C.TEALD } },
        { text: v, options: { color: C.SUB } },
      ], { x: 8.78, y: 2.75 + i * 0.62, w: 3.8, h: 0.6, fontSize: 12, margin: 0, valign: "middle" });
    });
  }

  // ===================== 16. 演示 D-3 + 响应拦截器 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 18);
    slideTitle(s, "DEMO · ③④ D-3 + 响应拦截器", "专业指标点导出 · 40100 自动登出", ICONS.export);
    // 左 D-3
    card(s, 0.6, 1.95, 6.0, 4.4, C.NAVY);
    txt(s, "③ D-3 专业指标点达成度", { x: 0.85, y: 2.12, w: 5.5, h: 0.4, fontSize: 15, bold: true, color: C.NAVY, margin: 0 });
    [
      "专业负责人进入「专业指标点报表」",
      "导出 Excel：各指标点 G_k 达成度",
      "导出 PDF：嵌入 CJK 字体的专业报表",
      "验证：%PDF-1.4、中文正常、约 30KB",
      "数据源：major_indicator_achievement",
    ].forEach((v, i) => {
      const y = 2.62 + i * 0.66;
      numCircle(s, 0.85, y, i + 1, C.NAVY, "FFFFFF", 0.34);
      txt(s, v, { x: 1.32, y, w: 5.0, h: 0.5, fontSize: 11.5, color: C.SUB, valign: "middle", margin: 0 });
    });
    // 右 响应拦截器
    card(s, 6.8, 1.95, 5.95, 4.4, C.GOLD);
    s.addImage({ data: ICONS.lock, x: 7.05, y: 2.15, w: 0.4, h: 0.4 });
    txt(s, "④ 前端响应拦截器（#134）", { x: 7.55, y: 2.15, w: 5, h: 0.4, fontSize: 15, bold: true, color: C.GOLD, margin: 0, valign: "middle" });
    txt(s, [
      { text: "问题：", options: { bold: true, color: C.CORAL } },
      { text: "后端返回 code===40100（未登录）时只弹 error，不登出。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 6 } },
      { text: "修复：", options: { bold: true, color: C.GREEN } },
      { text: "在 axios 响应拦截器统一处理 → 清登录态 + 跳 /login。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 6 } },
      { text: "演示：", options: { bold: true, color: C.NAVY } },
      { text: "手动让后端 token 失效 → 任意接口请求 → 自动跳回登录页。", options: { color: C.SUB } },
    ], { x: 7.05, y: 2.7, w: 5.45, h: 3.0, fontSize: 12, margin: 0, valign: "top" });
  }

  // -------- 分节：难点攻克 --------
  divider("03", "PART 03 · CHALLENGES", "难点攻克", "本周碰到的主要问题及解决经验", ICONS.tools, 19);

  // ===================== 17. 难点① PDF 中文 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 20);
    slideTitle(s, "CHALLENGE · ① PDF 中文渲染", "PDFBox 不支持中文 —— 字体嵌入方案", ICONS.pdf);
    card(s, 0.6, 1.95, 5.9, 4.4, C.CORAL);
    s.addImage({ data: ICONS.warn, x: 0.85, y: 2.15, w: 0.4, h: 0.4 });
    txt(s, "问题", { x: 1.35, y: 2.15, w: 3, h: 0.4, fontSize: 15, bold: true, color: C.CORAL, margin: 0, valign: "middle" });
    txt(s, [
      { text: "• PDFBox 2.0.x 内置字体不支持中文，导出 PDF 中文全乱码。", options: { breakLine: true, paraSpaceAfter: 6, color: C.SUB } },
      { text: "• 不支持 CFF-OTF 与变量 TTF 字体格式。", options: { breakLine: true, paraSpaceAfter: 6, color: C.SUB } },
      { text: "• 部署环境（Linux）可能无中文字体。", options: { color: C.SUB } },
    ], { x: 0.85, y: 2.7, w: 5.4, h: 3.4, fontSize: 12.5, margin: 0, valign: "top" });
    card(s, 6.7, 1.95, 6.05, 4.4, C.GREEN);
    s.addImage({ data: ICONS.light, x: 6.95, y: 2.15, w: 0.4, h: 0.4 });
    txt(s, "解决方案", { x: 7.45, y: 2.15, w: 4, h: 0.4, fontSize: 15, bold: true, color: C.GREEN, margin: 0, valign: "middle" });
    txt(s, [
      { text: "方案对比：", options: { bold: true, color: C.NAVY, breakLine: true, paraSpaceAfter: 4 } },
      { text: "① 嵌入开源 CJK 字体子集（最稳） ✓", options: { color: C.GREEN, bold: true, breakLine: true, paraSpaceAfter: 3 } },
      { text: "② 用运行环境系统字体（Linux 需自装）", options: { color: C.MUTED, breakLine: true, paraSpaceAfter: 8 } },
      { text: "落地：", options: { bold: true, color: C.NAVY, breakLine: true, paraSpaceAfter: 4 } },
      { text: "LXGW WenKai 静态 TTF（OFL 协议，~24MB 入库，subset 后 PDF 不膨胀）。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 4 } },
      { text: "PdfTableRenderer 表格化渲染 + pom resource filtering 修复字体加载。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 4 } },
      { text: "验证：PDF 头部 %PDF-1.4、中文正常、体积约 30KB。", options: { color: C.TEALD, bold: true } },
    ], { x: 6.95, y: 2.66, w: 5.6, h: 3.6, fontSize: 12, margin: 0, valign: "top" });
  }

  // ===================== 18. 难点② 穿透台账 SQL =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 21);
    slideTitle(s, "CHALLENGE · ② 穿透台账 SQL", "旧 Mapper 四处致命错误 —— 五层穿透重写", ICONS.db);
    const errs = [
      ["tc.major_id 列不存在", "teaching_class 表无 major_id，错误关联"],
      ["student_score.class_id 列名错", "正确列名应为 teaching_class_id"],
      ["4 条 SQL 无过滤", "未按 termId / grade 过滤，数据串档"],
      ["平铺 4 表无真 JOIN", "穿透语义不成立，无层级关联"],
    ];
    errs.forEach((e, i) => {
      const col = i % 2, row = Math.floor(i / 2);
      const x = 0.6 + col * 6.18;
      const y = 1.95 + row * 1.0;
      card(s, x, y, 5.95, 0.88, C.CORAL);
      numCircle(s, x + 0.2, y + 0.19, i + 1, C.CORAL);
      txt(s, e[0], { x: x + 0.85, y, w: 4.9, h: 0.45, fontSize: 12.5, bold: true, color: C.NAVY, valign: "middle", margin: 0 });
      txt(s, e[1], { x: x + 0.85, y: y + 0.42, w: 4.9, h: 0.4, fontSize: 10.5, color: C.SUB, valign: "middle", margin: 0 });
    });
    // 重写方案
    card(s, 0.6, 4.15, 12.13, 2.2, C.TEALD);
    s.addImage({ data: ICONS.light, x: 0.85, y: 4.35, w: 0.4, h: 0.4 });
    txt(s, "重写方案：走 course.major_id 关联，五层组装", { x: 1.35, y: 4.35, w: 10, h: 0.4, fontSize: 15, bold: true, color: C.TEALD, margin: 0, valign: "middle" });
    txt(s, "major_indicator  →  course_indicator  →  student_objective  →  student_score", { x: 0.85, y: 4.9, w: 11.6, h: 0.5, fontSize: 16, bold: true, color: C.NAVY, align: "center", margin: 0 });
    txt(s, "废弃坏 PenetrationAccountMapper；新增 AssessmentPointAccount VO 对齐前端字段；按 termId / grade 正确过滤；输出 Excel 5 sheet。E2E 已过。", { x: 0.85, y: 5.5, w: 11.6, h: 0.7, fontSize: 12, color: C.SUB, align: "center", margin: 0 });
  }

  // ===================== 19. 难点③ 工程化四连 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 22);
    slideTitle(s, "CHALLENGE · ③ 工程化四连", "越权 · 原子性 · 合并吞字段 · 连接 stale", ICONS.cog);
    const items = [
      ["多角色越权", ICONS.shield, C.TEALB, "#110", "AuthInterceptor 原只读 mustRole、不校验 anyRole → 真正校验 anyRole。"],
      ["导入非原子", ICONS.clip, C.GREEN, "#121/#136", "@Transactional 内逐行 catch 吞异常 → 去逐行 catch，任一行失败回滚。"],
      ["合并吞字段", ICONS.branch, C.PURPLE, "#115", "#112/#113 合并冲突丢失 studentMapper 注入致编译失败 → 同文件注入错位 + 合并前 mvn compile。"],
      ["连接 stale", ICONS.server, C.GOLD, "#138", "远程 MySQL 关闭空闲连接、HikariCP 不恢复 → keepalive-time / max-lifetime / connection-test-query。"],
    ];
    items.forEach((it, i) => {
      const col = i % 2, row = Math.floor(i / 2);
      const x = 0.6 + col * 6.18;
      const y = 1.95 + row * 2.18;
      card(s, x, y, 5.95, 2.0, it[2]);
      s.addImage({ data: it[1], x: x + 0.3, y: y + 0.26, w: 0.42, h: 0.42 });
      txt(s, it[0], { x: x + 0.85, y: y + 0.24, w: 4, h: 0.45, fontSize: 15.5, bold: true, color: C.NAVY, margin: 0, valign: "middle" });
      txt(s, it[3], { x: x + 4.9, y: y + 0.26, w: 0.95, h: 0.4, fontSize: 11, bold: true, color: it[2], align: "right", margin: 0 });
      txt(s, it[4], { x: x + 0.32, y: y + 0.85, w: 5.4, h: 1.0, fontSize: 11.5, color: C.SUB, margin: 0 });
    });
  }

  // -------- 分节：小组分享 --------
  divider("04", "PART 04 · AI PRACTICE", "小组分享", "AI 编程经验 · 可操作、可验证", ICONS.robot, 23);

  // ===================== 20. AI 实践总览 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 24);
    slideTitle(s, "SHARING · AI 编程实践", "我们把 AI 当「配对工程师」用 —— 四个落地实践", ICONS.robot);
    txt(s, "原则：AI 负责生成（代码/文档/诊断），人负责验证（编译 + 单测 + E2E + 对照规约）。", { x: 0.6, y: 1.72, w: 12, h: 0.4, fontSize: 13, color: C.TEALD, bold: true, margin: 0 });
    const items = [
      ["①", "Issue → PR → 自动闭环", "gh CLI 编排流程，Closes #N 自动关 Issue", ICONS.git, C.NAVY],
      ["②", "批量修复 + 单测 + 代码审查", "一类问题一个 PR，AI 按规约生成代码与单测", ICONS.clip, C.TEALD],
      ["③", "交接文档 + SQL 诊断", "AI 读 git log 生成结构化文档；诊断坏 SQL", ICONS.doc, C.PURPLE],
      ["④", "避坑：幻觉 / 吞字段 / 必须验证", "对照 schema、合并前编译、三重验证文化", ICONS.warn, C.AMBER],
    ];
    items.forEach((it, i) => {
      const col = i % 2, row = Math.floor(i / 2);
      const x = 0.6 + col * 6.18;
      const y = 2.2 + row * 2.18;
      card(s, x, y, 5.95, 2.0, it[4]);
      txt(s, it[0], { x: x + 0.28, y: y + 0.22, w: 1, h: 0.7, fontSize: 34, bold: true, color: it[4], margin: 0, valign: "middle" });
      s.addImage({ data: it[3], x: x + 1.2, y: y + 0.3, w: 0.4, h: 0.4 });
      txt(s, it[1], { x: x + 1.75, y: y + 0.28, w: 4, h: 0.5, fontSize: 15.5, bold: true, color: C.NAVY, margin: 0, valign: "middle" });
      txt(s, it[2], { x: x + 1.77, y: y + 0.82, w: 4, h: 0.8, fontSize: 11.5, color: C.SUB, margin: 0 });
    });
  }

  // ===================== 21. 实例① Issue→PR 闭环 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 25);
    slideTitle(s, "PRACTICE · ① Issue → PR 闭环", "可操作：用 gh CLI 把 AI 产出钉进标准流程", ICONS.git);
    const flow = [
      ["建 Issue", "gh issue create，写清问题 + 方案，assignees + labels（bug/feat）"],
      ["AI 修复", "从 main 建独立分支（fix/…），让 AI 按规约生成修复代码"],
      ["提 PR", "PR 正文首行写 Closes #<Issue号>，assignees 标注"],
      ["合并即闭环", "merge-commit 合并 → Issue 自动关闭；每个 PR 一类问题"],
    ];
    flow.forEach((f, i) => {
      const x = 0.6 + i * 3.11;
      card(s, x, 1.95, 2.88, 2.5, C.NAVY);
      numCircle(s, x + 1.19, 2.12, i + 1, C.NAVY, "FFFFFF", 0.5);
      txt(s, f[0], { x: x + 0.2, y: 2.72, w: 2.5, h: 0.5, fontSize: 14, bold: true, color: C.NAVY, align: "center", margin: 0 });
      txt(s, f[1], { x: x + 0.2, y: 3.22, w: 2.5, h: 1.1, fontSize: 10.5, color: C.SUB, align: "center", margin: 0 });
      if (i < 3) downArrow(s, x + 2.88, 3.0, 0.34, C.TEAL);
    });
    // 验证条
    card(s, 0.6, 4.75, 12.13, 1.6, C.TEALD);
    s.addImage({ data: ICONS.clip, x: 0.85, y: 4.98, w: 0.42, h: 0.42 });
    txt(s, "可验证（本周真实数据）", { x: 1.4, y: 4.98, w: 6, h: 0.45, fontSize: 15, bold: true, color: C.TEALD, margin: 0, valign: "middle" });
    txt(s, [
      { text: "• 本周 28 个 PR 全部走此流程；Issues #118/#120/#122 经 Closes #N 自动关闭。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 4 } },
      { text: "• gh 认证踩坑：fine-grained PAT 不能 assign issue/PR → 改用 classic PAT（repo scope）+ GH_TOKEN 前缀 assign，可复现。", options: { color: C.SUB } },
    ], { x: 0.85, y: 5.45, w: 11.6, h: 0.9, fontSize: 12, margin: 0 });
  }

  // ===================== 22. 实例② 批量修复+单测+审查 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 26);
    slideTitle(s, "PRACTICE · ② 批量修复 + 单测 + 审查", "可操作：AI 按规约生成代码、单测，并辅助发现隐患", ICONS.clip);
    const blocks = [
      ["按规约生成 + 单测", C.TEALD, "一类问题一个 PR；AI 生成代码同时产出 Mockito/JUnit5 单测。\n例：OwnershipHelper 11 处越权校验；#110 单测 9 个 / #112 单测 5 个 / #117 单测 8 个。"],
      ["AI 辅助发现隐患", C.PURPLE, "让 AI 对照规约文档审查，发现：anyRole 越权（#110）、一级公式 Σ得分/Σ满分 偏差（#116）、角色应为 ROLE_LEADER（规约 A-4）。"],
      ["可验证：三重验证", C.GREEN, "每个 PR = 编译通过 + 单测全绿（共 44 个）+ 关键项 E2E（admin 账号 + curl）。mvn test 可一键复现。"],
    ];
    blocks.forEach((b, i) => {
      const y = 1.95 + i * 1.45;
      card(s, 0.6, y, 12.13, 1.3, b[1]);
      s.addImage({ data: [ICONS.clip, ICONS.shield, ICONS.check][i], x: 0.85, y: y + 0.26, w: 0.42, h: 0.42 });
      txt(s, b[0], { x: 1.45, y: y + 0.18, w: 11, h: 0.4, fontSize: 14.5, bold: true, color: b[1], margin: 0 });
      txt(s, b[2], { x: 1.45, y: y + 0.58, w: 11, h: 0.7, fontSize: 11.5, color: C.SUB, margin: 0 });
    });
  }

  // ===================== 23. 实例③ 交接文档 + SQL 诊断 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 27);
    slideTitle(s, "PRACTICE · ③ 交接文档 + SQL 诊断", "可操作：AI 读代码生成文档、诊断坏 SQL", ICONS.doc);
    card(s, 0.6, 1.95, 5.95, 4.4, C.TEALB);
    s.addImage({ data: ICONS.doc, x: 0.85, y: 2.15, w: 0.42, h: 0.42 });
    txt(s, "AI 生成结构化交接文档", { x: 1.4, y: 2.15, w: 5, h: 0.45, fontSize: 15, bold: true, color: C.TEALB, margin: 0, valign: "middle" });
    txt(s, [
      { text: "操作：", options: { bold: true, color: C.NAVY, breakLine: true, paraSpaceAfter: 3 } },
      { text: "让 AI 读 git log + 关键源码，生成《开发进度交接.md》，含：今日完成 / 系统状态 / 明日待办 / 关键代码位置 / 计算传导链 / 决策记录。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 6 } },
      { text: "可验证：", options: { bold: true, color: C.GREEN, breakLine: true, paraSpaceAfter: 3 } },
      { text: "次日按文档「明日建议起点」直接 git pull + mvn run 上手，无需口头交接。", options: { color: C.SUB } },
    ], { x: 0.85, y: 2.7, w: 5.45, h: 3.5, fontSize: 12, margin: 0, valign: "top" });
    card(s, 6.75, 1.95, 6.0, 4.4, C.PURPLE);
    s.addImage({ data: ICONS.db, x: 7.0, y: 2.15, w: 0.42, h: 0.42 });
    txt(s, "AI 诊断坏 SQL", { x: 7.55, y: 2.15, w: 5, h: 0.45, fontSize: 15, bold: true, color: C.PURPLE, margin: 0, valign: "middle" });
    txt(s, [
      { text: "操作：", options: { bold: true, color: C.NAVY, breakLine: true, paraSpaceAfter: 3 } },
      { text: "把穿透台账的坏 SQL 贴给 AI，要求指出错误并给出修正方案。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 6 } },
      { text: "AI 输出（可验证）：", options: { bold: true, color: C.GREEN, breakLine: true, paraSpaceAfter: 3 } },
      { text: "① tc.major_id 列不存在 ② class_id 列名错 ③ 缺 termId/grade 过滤 ④ 无真 JOIN → 给出五层穿透重写方案。", options: { color: C.SUB, breakLine: true, paraSpaceAfter: 6 } },
      { text: "结果：重写后 E2E 通过，Excel 5 sheet 正确。", options: { color: C.TEALD, bold: true } },
    ], { x: 7.0, y: 2.7, w: 5.5, h: 3.5, fontSize: 12, margin: 0, valign: "top" });
  }

  // ===================== 24. 避坑经验 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 28);
    slideTitle(s, "PRACTICE · ④ 避坑经验", "AI 很强，但必须「信任 + 验证」", ICONS.warn);
    const items = [
      ["幻觉：编造不存在的列/方法", "AI 会写出 tc.major_id 这类不存在的字段。→ 必须查 schema / 编译验证后再采用。", C.CORAL],
      ["多 PR 合并吞字段", "AI 让多个 PR 同改注入块 → 冲突丢字段致编译失败。→ 同文件注入错位 + 合并前 mvn compile。", C.AMBER],
      ["建议违反规约", "AI 可能给「加权平均」而非规约要求的「Σ得分/Σ满分」。→ 对照规约文档复核每个公式。", C.PURPLE],
      ["验证文化是底线", "AI 生成、人验证：编译 + 单测（44 个）+ E2E 三重把关，关键 SQL 必查 schema。", C.GREEN],
    ];
    items.forEach((it, i) => {
      const col = i % 2, row = Math.floor(i / 2);
      const x = 0.6 + col * 6.18;
      const y = 1.95 + row * 2.18;
      card(s, x, y, 5.95, 2.0, it[2]);
      numCircle(s, x + 0.28, y + 0.26, i + 1, it[2]);
      txt(s, it[0], { x: x + 0.95, y: y + 0.24, w: 4.8, h: 0.7, fontSize: 14.5, bold: true, color: C.NAVY, margin: 0, valign: "middle" });
      txt(s, it[1], { x: x + 0.3, y: y + 0.95, w: 5.45, h: 0.95, fontSize: 11.5, color: C.SUB, margin: 0 });
    });
  }

  // -------- 分节：组间交流 --------
  divider("05", "PART 05 · Q&A", "组间交流", "欢迎其他小组提问，本组作答", ICONS.q, 29);

  // ===================== 25. 组间交流 Q&A =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.LIGHT };
    chrome(s, 30);
    slideTitle(s, "Q&A · 组间交流", "三个小组的提问与本组回答（节选）", ICONS.comments);
    const qa = [
      [C.NAVY, "计算模型组", "Q：三级达成度如何保证 ΣW = 1.0？", "A：后端 MatrixCourseIndicatorServiceImpl.checkMatrixWeights 强制校验，容差 0.0001；课程内部权重 ∑w = 1.0 由 checkWeights 同理校验，不满足直接拒绝落库。"],
      [C.TEALD, "安全组", "Q：如何防止教师横向越权查看 / 改他人数据？", "A：① OwnershipHelper 数据级归属校验（班级 teacher_id、课程「是否讲授」），admin 放行；② AuthInterceptor 真正校验 anyRole；③ 报表接口纳入 JWT 白名单。"],
      [C.PURPLE, "AI 组", "Q：AI 生成的代码如何保证不引入 bug？", "A：三重验证（编译 + 44 个单测 + E2E）+ 对照规约复核 + 关键 SQL 查 schema；AI 负责生成，人负责验证，幻觉字段会被编译立刻拦住。"],
    ];
    qa.forEach((q, i) => {
      const y = 1.95 + i * 1.5;
      card(s, 0.6, y, 12.13, 1.38, q[0]);
      // 组标签
      rect(s, 0.6, y, 1.7, 1.38, q[0]);
      txt(s, q[1], { x: 0.6, y, w: 1.7, h: 1.38, fontSize: 13, bold: true, color: "FFFFFF", align: "center", valign: "middle", margin: 0 });
      txt(s, q[2], { x: 2.5, y: y + 0.1, w: 10.0, h: 0.45, fontSize: 13, bold: true, color: C.NAVY, margin: 0 });
      txt(s, q[3], { x: 2.5, y: y + 0.58, w: 10.0, h: 0.75, fontSize: 11.5, color: C.SUB, margin: 0 });
    });
    txt(s, "备选问题：④ 部署组 — PDF 中文如何解决？A：嵌入 LXGW WenKai 静态 TTF（OFL）+ PdfTableRenderer。   ⑤ 测试组 — 测试数据哪来？A：新增软件工程专业完整数据（id 段 9000+）。", { x: 0.6, y: 6.5, w: 12.1, h: 0.55, fontSize: 10.5, color: C.MUTED, italic: true, margin: 0 });
  }

  // ===================== 26. 结尾 =====================
  {
    const s = pres.addSlide();
    s.background = { color: C.NAVY };
    rect(s, 0, 0, W, H, C.NAVY);
    rect(s, 0, 0, 0.22, H, C.TEAL);
    txt(s, "THANKS", { x: 1.0, y: 2.5, w: 11, h: 1.2, fontSize: 80, bold: true, color: "FFFFFF", charSpacing: 6, margin: 0 });
    rect(s, 1.05, 3.95, 3.2, 0.05, C.TEAL);
    txt(s, "感谢聆听 · 欢迎提问与交流", { x: 1.02, y: 4.15, w: 11, h: 0.5, fontSize: 20, color: C.MINT, margin: 0 });
    txt(s, "仓库：WindZzz1/Computing_Platform   |   计算平台开发组   |   2026.06.15 – 06.21", { x: 1.02, y: 5.6, w: 11, h: 0.4, fontSize: 13, color: "C7D6E8", margin: 0 });
  }

  await pres.writeFile({ fileName: "../第十六周工作汇报.pptx" });
  console.log("WROTE 第十六周工作汇报.pptx  slides:", pres._slides ? pres._slides.length : "?");
})();
