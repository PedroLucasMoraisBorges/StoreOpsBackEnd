package com.store_ops_backend.services.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class PdfReportBuilder {
    private static final float MARGIN = 40f;
    private static final float LINE_HEIGHT = 16f;
    private static final float TABLE_ROW_HEIGHT = 20f;
    private static final float SECTION_SPACING = 10f;
    private static final float TABLE_SPACING = 12f;

    private final PDDocument document;
    private final PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final String title;
    private final String companyName;
    private final String periodLabel;

    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private float cursorY;

    public PdfReportBuilder(String title, String companyName, String periodLabel) {
        this.document = new PDDocument();
        this.title = title;
        this.companyName = companyName;
        this.periodLabel = periodLabel;
        startNewPage();
    }

    public void addSectionTitle(String text) {
        ensureSpace(LINE_HEIGHT * 2 + SECTION_SPACING);
        writeLine(text, fontBold, 12f);
        cursorY -= SECTION_SPACING;
    }

    public void addParagraph(String text) {
        ensureSpace(LINE_HEIGHT);
        writeLine(text, fontRegular, 10f);
    }

    public void addTable(List<String> headers, List<List<String>> rows, List<Float> colWidths) {
        float tableWidth = colWidths.stream().reduce(0f, Float::sum);
        ensureSpace(TABLE_ROW_HEIGHT * 2 + TABLE_SPACING);
        drawTableRow(headers, colWidths, true, tableWidth);

        for (List<String> row : rows) {
            ensureSpace(TABLE_ROW_HEIGHT);
            drawTableRow(row, colWidths, false, tableWidth);
        }
        cursorY -= TABLE_SPACING;
    }

    public byte[] build() {
        try {
            if (contentStream != null) {
                contentStream.close();
            }
            addFooters();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void startNewPage() {
        try {
            if (contentStream != null) {
                contentStream.close();
            }
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            cursorY = currentPage.getMediaBox().getHeight() - MARGIN;
            drawHeader();
        } catch (IOException e) {
            throw new RuntimeException("Error creating PDF page", e);
        }
    }

    private void drawHeader() {
        writeLine(title, fontBold, 14f);
        writeLine("Empresa: " + companyName, fontRegular, 10f);
        writeLine("Período: " + periodLabel, fontRegular, 10f);
        writeLine("Emissão: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontRegular, 9f);
        cursorY -= SECTION_SPACING;
        drawLine();
    }

    private void drawLine() {
        try {
            float startX = MARGIN;
            float endX = currentPage.getMediaBox().getWidth() - MARGIN;
            contentStream.moveTo(startX, cursorY);
            contentStream.lineTo(endX, cursorY);
            contentStream.stroke();
            cursorY -= SECTION_SPACING;
        } catch (IOException e) {
            throw new RuntimeException("Error drawing line", e);
        }
    }

    private void writeLine(String text, PDFont font, float size) {
        try {
            contentStream.beginText();
            contentStream.setFont(font, size);
            contentStream.newLineAtOffset(MARGIN, cursorY);
            contentStream.showText(text);
            contentStream.endText();
            cursorY -= LINE_HEIGHT;
        } catch (IOException e) {
            throw new RuntimeException("Error writing text", e);
        }
    }

    private void drawTableRow(List<String> values, List<Float> colWidths, boolean isHeader, float tableWidth) {
        try {
            float x = MARGIN;
            float y = cursorY;
            float height = TABLE_ROW_HEIGHT;

            contentStream.setLineWidth(0.5f);
            contentStream.addRect(x, y - height + 4f, tableWidth, height);
            contentStream.stroke();

            for (int i = 0; i < values.size(); i++) {
                float width = colWidths.get(i);
                contentStream.addRect(x, y - height + 4f, width, height);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(isHeader ? fontBold : fontRegular, 9f);
            contentStream.newLineAtOffset(x + 4f, y - height + 12f);
            contentStream.showText(values.get(i) == null ? "-" : values.get(i));
            contentStream.endText();

                x += width;
            }

            cursorY -= height;
        } catch (IOException e) {
            throw new RuntimeException("Error drawing table row", e);
        }
    }

    private void ensureSpace(float height) {
        if (cursorY - height < MARGIN) {
            startNewPage();
        }
    }

    private void addFooters() throws IOException {
        int totalPages = document.getNumberOfPages();
        for (int i = 0; i < totalPages; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream footerStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                String text = "Página " + (i + 1) + " de " + totalPages;
                float y = MARGIN - 10f;
                footerStream.beginText();
                footerStream.setFont(fontRegular, 9f);
                footerStream.newLineAtOffset(MARGIN, y);
                footerStream.showText(text);
                footerStream.endText();
            }
        }
    }
}
