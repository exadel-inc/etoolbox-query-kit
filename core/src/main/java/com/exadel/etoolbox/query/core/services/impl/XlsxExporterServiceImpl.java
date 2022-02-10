package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.XlsxExporterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component(service = XlsxExporterService.class)
public class XlsxExporterServiceImpl implements XlsxExporterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XlsxExporterServiceImpl.class);

    // ISO8601Long
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss X";

    private final SimpleDateFormat simpleDateFormat;
    private CellStyle hLinkStyle;
    private CellStyle hLinkStyleOdd;
    private CellStyle dateStyle;
    private CellStyle dateStyleOdd;
    private CellStyle stringStyle;
    private CellStyle stringStyleOdd;


    public XlsxExporterServiceImpl() {
        this(DEFAULT_DATE_FORMAT);
    }

    public XlsxExporterServiceImpl(String dateFormatPattern) {
        simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
    }

    /**
     * @param out     - any output stream
     * @param columns - table columns<br />
     *                <p>Convention:</p>
     *                <p>1. if the columns contains ", URL" - the URL style will be applied and the cell will have the "Address" type</p>
     *                <p>2. if the columns contains ", Date" - the Date style will be applied and the cell will have the "Date" type</p>
     * @param data    - table data, the list of Key(header)/Value - the Keys should be the equal the columns in the "columns" list parameter
     */
    @Override
    public void export(final OutputStream out, final Map<String, String> columns, final List<Map<String, String>> data) {
        if (MapUtils.isEmpty(columns) || CollectionUtils.isEmpty(data)) {
            return;
        }
        System.setProperty("java.awt.headless", "true");
        hLinkStyleOdd = null;
        hLinkStyle = null;
        dateStyleOdd = null;
        dateStyle = null;
        stringStyleOdd = null;
        stringStyle = null;

        try (Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet();
            CreationHelper createHelper = wb.getCreationHelper();

            int rowNumber = 0;
            int columnNumber = 0;

            Row row = sheet.createRow(rowNumber);

            XSSFCellStyle headerStyle = (XSSFCellStyle) wb.createCellStyle();
            headerStyle.setFillBackgroundColor(new XSSFColor(new java.awt.Color(68, 114, 196)));
            headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(68, 114, 196)));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = wb.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);

            for (String header : columns.keySet()) {
                Cell cell = row.createCell(columnNumber++);
                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }

            for (Map<String, String> dataRow : data) {
                row = sheet.createRow(++rowNumber);
                columnNumber = 0;

                for (Map.Entry<String, String> headerEntry : columns.entrySet()) {
                    Cell cell = row.createCell(columnNumber++);
                    String value = dataRow.get(headerEntry.getValue());

                    if (headerEntry.getKey().endsWith(", URL")) {
                        putURLCell(createHelper, wb, cell, value, rowNumber % 2 != 0);
                    } else if (headerEntry.getKey().endsWith(", Date")) {
                        putDateCell(createHelper, wb, cell, getParsedDate(value), rowNumber % 2 != 0);
                    } else {
                        putStringCell(wb, cell, value, rowNumber % 2 != 0);
                    }
                }
            }

            for (int i = 0; i < columns.keySet().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            sheet.setAutoFilter(new CellRangeAddress(0, rowNumber, 0, columnNumber - 1));
            wb.write(out);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    private Date getParsedDate(String value) throws ParseException {
        return StringUtils.isBlank(value) ? null : simpleDateFormat.parse(value);
    }

    private CellStyle getHLinkStyle(Workbook wb, boolean odd) {
        if (odd) {
            if (hLinkStyleOdd == null) {
                hLinkStyleOdd = wb.createCellStyle();
                Font hLinkFont = wb.createFont();
                hLinkFont.setUnderline(Font.U_SINGLE);
                hLinkFont.setColor(IndexedColors.BLUE.getIndex());
                hLinkStyleOdd.setFont(hLinkFont);
                hLinkStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                ((XSSFCellStyle) hLinkStyleOdd).setFillBackgroundColor(new XSSFColor(new java.awt.Color(217, 217, 217)));
                ((XSSFCellStyle) hLinkStyleOdd).setFillForegroundColor(new XSSFColor(new java.awt.Color(217, 217, 217)));
            }

            return hLinkStyleOdd;
        } else {
            if (hLinkStyle == null) {
                hLinkStyle = wb.createCellStyle();
                Font hLinkFont = wb.createFont();
                hLinkFont.setUnderline(Font.U_SINGLE);
                hLinkFont.setColor(IndexedColors.BLUE.getIndex());
                hLinkStyle.setFont(hLinkFont);
            }

            return hLinkStyle;
        }
    }

    private CellStyle getDateStyle(Workbook wb, CreationHelper createHelper, boolean odd) {
        if (odd) {
            if (dateStyleOdd == null) {
                dateStyleOdd = wb.createCellStyle();
                dateStyleOdd.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
                dateStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                ((XSSFCellStyle) dateStyleOdd).setFillBackgroundColor(new XSSFColor(new java.awt.Color(217, 217, 217)));
                ((XSSFCellStyle) dateStyleOdd).setFillForegroundColor(new XSSFColor(new java.awt.Color(217, 217, 217)));
            }

            return dateStyleOdd;
        } else {
            if (dateStyle == null) {
                dateStyle = wb.createCellStyle();
                dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
            }

            return dateStyle;
        }
    }

    private CellStyle getStyle(Workbook wb, boolean odd) {
        if (odd) {
            if (stringStyleOdd == null) {
                stringStyleOdd = wb.createCellStyle();
                stringStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                ((XSSFCellStyle) stringStyleOdd).setFillBackgroundColor(new XSSFColor(new java.awt.Color(217, 217, 217)));
                ((XSSFCellStyle) stringStyleOdd).setFillForegroundColor(new XSSFColor(new java.awt.Color(217, 217, 217)));
            }

            return stringStyleOdd;
        } else {
            if (stringStyle == null) {
                stringStyle = wb.createCellStyle();
            }

            return stringStyle;
        }
    }

    private void putURLCell(CreationHelper createHelper, Workbook wb, Cell cell, String value, boolean odd) {
        cell.setCellValue(value);
        try {
            Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(value);
            cell.setHyperlink(link);
            cell.setCellStyle(getHLinkStyle(wb, odd));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Link {} has Invalid URI format and will be styled as text", value, e);
            cell.setCellStyle(getStyle(wb, odd));
        }
    }

    private void putDateCell(CreationHelper createHelper, Workbook wb, Cell cell, Date date, boolean odd) {
        cell.setCellValue(date);
        cell.setCellStyle(getDateStyle(wb, createHelper, odd));
    }

    private void putStringCell(Workbook wb, Cell cell, String value, boolean odd) {
        cell.setCellValue(value);
        cell.setCellStyle(getStyle(wb, odd));
    }
}
