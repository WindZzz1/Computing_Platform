package com.yupi.springbootinit.manager;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.util.List;

/**
 * 简易 PDF 表格渲染器：嵌入 CJK 字体、支持标题/段落/表格/自动分页。
 * <p>
 * 用于课程达成度报表 PDF 导出（PDFBox 2.0.x 内置字体不支持中文，必须嵌入 TTF）。
 *
 * @author YU
 */
public class PdfTableRenderer {

    private static final float MARGIN = 40f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float ROW_HEIGHT = 18f;
    private static final float CELL_TEXT_SIZE = 9f;
    // 表头背景浅蓝灰
    private static final float HEADER_BG_R = 0.85f;
    private static final float HEADER_BG_G = 0.90f;
    private static final float HEADER_BG_B = 0.97f;

    private final PDDocument doc;
    private final PDType0Font font;
    private PDPageContentStream cs;
    private float y;

    public PdfTableRenderer(PDDocument doc, PDType0Font font) throws IOException {
        this.doc = doc;
        this.font = font;
        newPage();
    }

    /** 新起一页 */
    public void newPage() throws IOException {
        if (cs != null) {
            cs.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        cs = new PDPageContentStream(doc, page);
        y = PAGE_HEIGHT - MARGIN;
    }

    /** 结束渲染（关闭当前 content stream） */
    public void close() throws IOException {
        if (cs != null) {
            cs.close();
            cs = null;
        }
    }

    /** 居中文字（标题/副标题） */
    public void drawCenteredText(String text, float size, float spaceAfter) throws IOException {
        ensureSpace(size + spaceAfter);
        String t = nullToEmpty(text);
        float w = textWidth(t, size);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset((PAGE_WIDTH - w) / 2, y - size);
        cs.showText(t);
        cs.endText();
        y -= (size + spaceAfter);
    }

    /** 左对齐段落文字（小节标题） */
    public void drawLeftText(String text, float size, float spaceAfter) throws IOException {
        ensureSpace(size + spaceAfter);
        String t = nullToEmpty(text);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, y - size);
        cs.showText(t);
        cs.endText();
        y -= (size + spaceAfter);
    }

    /**
     * 画一张表：小节标题 + 表头（带背景） + 数据行，行数过多自动分页并在新页重画表头。
     *
     * @param title   小节标题（可空）
     * @param headers 表头
     * @param rows    数据行（每行字符串数组）
     * @param ratios  各列宽度比例（按 CONTENT_WIDTH 分配）
     */
    public void drawTable(String title, String[] headers, List<String[]> rows, float[] ratios) throws IOException {
        if (title != null && !title.isEmpty()) {
            drawLeftText(title, 11f, 6f);
        }
        float[] widths = ratioToWidths(ratios);
        drawHeaderRow(headers, widths);
        if (rows != null) {
            for (String[] row : rows) {
                if (y - ROW_HEIGHT < MARGIN) {
                    newPage();
                    drawHeaderRow(headers, widths);
                }
                drawRow(row, widths);
            }
        }
        y -= 6f;
    }

    private void drawHeaderRow(String[] headers, float[] widths) throws IOException {
        ensureSpace(ROW_HEIGHT);
        float rowBottom = y - ROW_HEIGHT;
        // 表头背景
        cs.setNonStrokingColor(HEADER_BG_R, HEADER_BG_G, HEADER_BG_B);
        cs.addRect(MARGIN, rowBottom, CONTENT_WIDTH, ROW_HEIGHT);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);
        float x = MARGIN;
        for (int i = 0; i < headers.length; i++) {
            drawCellBox(x, rowBottom, widths[i], ROW_HEIGHT);
            drawCellText(headers[i], x, rowBottom, widths[i]);
            x += widths[i];
        }
        y = rowBottom;
    }

    private void drawRow(String[] row, float[] widths) throws IOException {
        float rowBottom = y - ROW_HEIGHT;
        float x = MARGIN;
        for (int i = 0; i < widths.length; i++) {
            drawCellBox(x, rowBottom, widths[i], ROW_HEIGHT);
            drawCellText(i < row.length ? row[i] : "", x, rowBottom, widths[i]);
            x += widths[i];
        }
        y = rowBottom;
    }

    private void drawCellBox(float x, float bottom, float w, float h) throws IOException {
        cs.addRect(x, bottom, w, h);
        cs.stroke();
    }

    private void drawCellText(String text, float x, float bottom, float colWidth) throws IOException {
        String t = nullToEmpty(text);
        String fit = fitText(t, colWidth - 8f, CELL_TEXT_SIZE);
        cs.beginText();
        cs.setFont(font, CELL_TEXT_SIZE);
        cs.newLineAtOffset(x + 4f, bottom + (ROW_HEIGHT - CELL_TEXT_SIZE) / 2);
        cs.showText(fit);
        cs.endText();
    }

    private void ensureSpace(float h) throws IOException {
        if (y - h < MARGIN) {
            newPage();
        }
    }

    /** 超宽文字截断 + 省略号 */
    private String fitText(String text, float maxW, float size) throws IOException {
        if (textWidth(text, size) <= maxW) {
            return text;
        }
        String ellipsis = "..";
        while (text.length() > 1 && textWidth(text + ellipsis, size) > maxW) {
            text = text.substring(0, text.length() - 1);
        }
        return text.isEmpty() ? "" : text + ellipsis;
    }

    private float textWidth(String text, float size) throws IOException {
        return font.getStringWidth(text) / 1000f * size;
    }

    private float[] ratioToWidths(float[] ratios) {
        float sum = 0f;
        for (float r : ratios) {
            sum += r;
        }
        float[] widths = new float[ratios.length];
        for (int i = 0; i < ratios.length; i++) {
            widths[i] = CONTENT_WIDTH * ratios[i] / sum;
        }
        return widths;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
