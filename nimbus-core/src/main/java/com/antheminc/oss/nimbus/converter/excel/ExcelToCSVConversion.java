/**
 *  Copyright 2016-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.converter.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.joda.time.LocalDate;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * <p>Uses Univocity to convert excel files to their .csv equivalent and stores
 * that file in the local filesystem's temporary storage.
 * 
 * @author Tony Lopez
 * @author Sandeep Mantha
 */
public class ExcelToCSVConversion implements ExcelToCsvConverter {

	@Override
	public File convert(File file, ExcelParserSettings settings) throws IOException {
		try (Workbook workbook = WorkbookFactory.create(file)) {
			return convert(workbook, file.getName(), settings);
		} catch (EncryptedDocumentException | InvalidFormatException e) {
			throw createConversionEx(e);
		}
	}

	@Override
	public File convert(InputStream inputStream, ExcelParserSettings settings) throws IOException {
		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			String tmpFilename = "excel-conversion-" + LocalDate.now().toString("yyyyMMddHHmmss");
			return convert(workbook, tmpFilename, settings);
		} catch (EncryptedDocumentException | InvalidFormatException e) {
			throw createConversionEx(e);
		}
	}

	private File convert(Workbook workbook, String csvFilename, ExcelParserSettings excelParserSettings)
			throws IOException {
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		DataFormatter formatter = new DataFormatter(true);

		//TODO - loop over all sheets in the workbook
		Sheet selSheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = selSheet.iterator();

		CsvWriterSettings settings = new CsvWriterSettings();
		settings.setNullValue("?");
		settings.getFormat().setComment('-');
		settings.setEmptyValue("!");
		settings.setHeaderWritingEnabled(true);
		settings.setSkipEmptyLines(false);

		File csvFile = File.createTempFile(csvFilename, ".csv");

		CsvWriter writer = new CsvWriter(csvFile, settings);
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			List<String> rowCsv = convertRowToCSV(row, formatter, evaluator);
			writer.writeRow(rowCsv);
		}
		writer.close();

		return csvFile;
	}

	private List<String> convertRowToCSV(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
		Cell cell = null;
		int lastCellNum = 0;
		List<String> csvLine = new ArrayList<String>();
		if (row != null) {
			lastCellNum = row.getLastCellNum();
			for (int i = 0; i <= lastCellNum; i++) {
				cell = row.getCell(i);
				if (cell == null) {
					csvLine.add("");
				} else {
					if (cell.getCellTypeEnum() != CellType.FORMULA) {
						csvLine.add(formatter.formatCellValue(cell));
					} else {
						csvLine.add(formatter.formatCellValue(cell, evaluator));
					}
				}
			}
		}
		return csvLine;
	}

	private FrameworkRuntimeException createConversionEx(Exception e) {
		throw new FrameworkRuntimeException("An error occurred while conerting the Excel workbook object.", e);
	}
}
