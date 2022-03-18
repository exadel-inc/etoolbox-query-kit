/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.servlets;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Works in pair with {@link QueryServlet} to provide downloading query results in {@code XLSX} format
 */
@UtilityClass
class XlsxOutputHelper {

    private static final String MIME_TYPE = "application/vnd.ms-excel";
    private static final String WORKBOOK_NAME = "Query Results";
    private static final String WORKBOOK_FILE_NAME = "query-results.xlsx";

    /**
     * Outputs the given query results in {@code XLSX} format
     * @param response {@code SlingHttpServletResponse} to output the result into
     * @param value    {@link SearchResult} instance containing the results of query execution
     */
    public static void output(SlingHttpServletResponse response, SearchResult value) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MIME_TYPE);
        response.setHeader("Content-Disposition", "attachment; filename=" + WORKBOOK_FILE_NAME);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(WORKBOOK_NAME);

        SheetBuilder sheetBuilder = new SheetBuilder(workbook, sheet, value.getColumns());
        for (SearchItem item : value.getItems()) {
            sheetBuilder.populateDataRow(item);
        }
        sheetBuilder.adjustColumnWidths();

        try (
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                OutputStream serverOutput = response.getOutputStream()) {

            workbook.write(output);
            workbook.close();
            output.flush();

            response.setContentLength(output.size());
            serverOutput.write(output.toByteArray());
            serverOutput.flush();
        }
    }

    private static class SheetBuilder {

        private static final int CALENDAR_CELL_WIDTH = 30;
        private static final int NUMERIC_CELL_WIDTH = 10;
        private static final int BOOL_CELL_WIDTH = 5;

        private static final int MAX_STATS_THRESHOLD = 10000;

        private static final int COLUMN_WIDTH_COEFFICIENT = 256;
        private static final int MAX_COLUMN_WIDTH = 25000;

        private final XSSFSheet sheet;
        private final ColumnCollection columns;
        private final CellStyle cellStyle;

        private final double[] columnWidths;
        private final int[] columnHits;

        private int rowNumber = 0;

        public SheetBuilder(XSSFWorkbook workbook, XSSFSheet sheet, ColumnCollection columns) {
            this.sheet = sheet;
            this.columns = columns;
            this.cellStyle = createCellStyle(workbook);
            this.columnWidths = new double[columns.getPropertyNames().size() + 1];
            this.columnHits = new int[columns.getPropertyNames().size() + 1];

            populateHeadingRow();
        }

        private void populateHeadingRow() {
            Row row = sheet.createRow(rowNumber++);
            Cell pathCell = row.createCell(0);
            pathCell.setCellValue(Constants.TITLE_PATH);
            pathCell.setCellStyle(cellStyle);

            int column = 1;
            for (String prop : columns.getPropertyNames()) {
                Cell cell = row.createCell(column++);
                cell.setCellValue(prop);
                cell.setCellStyle(cellStyle);
            }
        }

        public void populateDataRow(SearchItem item) {
            Row row = sheet.createRow(rowNumber++);
            Cell pathCell = row.createCell(0);
            pathCell.setCellValue(item.getPath());
            pathCell.setCellStyle(cellStyle);
            updateColumnStats(0, item.getPath().length());

            int column = 1;
            int width;
            for (String prop : columns.getPropertyNames()) {
                Cell cell = row.createCell(column++);
                cell.setCellStyle(cellStyle);
                Object value = item.getProperty(prop);
                if (value == null) {
                    continue;
                }
                int valueType = ValueUtil.detectType(value);
                switch (valueType) {
                    case PropertyType.LONG:
                    case PropertyType.DOUBLE:
                        cell.setCellValue((double) value);
                        width = NUMERIC_CELL_WIDTH;
                        break;
                    case PropertyType.BOOLEAN:
                        cell.setCellValue((boolean) value);
                        width = BOOL_CELL_WIDTH;
                        break;
                    case PropertyType.DATE:
                        String stringifiedDate = Constants.DATE_FORMATTER.format(((Calendar) value).getTime());
                        cell.setCellValue(stringifiedDate);
                        width = CALENDAR_CELL_WIDTH;
                        break;
                    default:
                        String stringValue = String.valueOf(value);
                        cell.setCellValue(stringValue);
                        width = stringValue.length();
                }
                updateColumnStats(column - 1, width);
            }
        }

        private void updateColumnStats(int position, int width) {
            if (width == 0 || rowNumber > MAX_STATS_THRESHOLD) {
                return;
            }
            if (columnWidths[position] == 0) {
                columnWidths[position] = width;
            }
            columnWidths[position] = (columnWidths[position] * columnHits[position] + width) / (columnHits[position] + 1);
            columnHits[position]++;
        }

        public void adjustColumnWidths() {
            for (int i = 0; i <= columns.getPropertyNames().size(); i++) {
                int width = ((int) Math.ceil(columnWidths[i])) * COLUMN_WIDTH_COEFFICIENT;
                sheet.setColumnWidth(i, Math.min(width, MAX_COLUMN_WIDTH));
            }
        }

        private static CellStyle createCellStyle(XSSFWorkbook workbook) {
            XSSFCellStyle result = workbook.createCellStyle();
            result.setWrapText(true);
            result.setVerticalAlignment(VerticalAlignment.TOP);
            return result;
        }
    }
}
