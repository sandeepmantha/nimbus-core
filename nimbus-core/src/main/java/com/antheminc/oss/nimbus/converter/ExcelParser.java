package com.antheminc.oss.nimbus.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.springframework.beans.factory.InitializingBean;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ExcelParser implements SourceReader, InitializingBean{

    private Workbook workbook;
    private DataFormatter formatter;
    private FormulaEvaluator evaluator;
    private ExcelParserSettings settings;
    
    public ExcelParser(ExcelParserSettings settings) {
    	this.settings = settings;
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void parse() {
		
		doOpen();
		doRead();
		doClose();
	}

	@Override
	public void doOpen() {
            try {
				this.workbook = WorkbookFactory.create(this.settings.getFile());
			} catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();
            this.formatter = new DataFormatter(true);	
	}

	@Override
	public void doClose() {
		try {
			this.workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doRead() {
		Sheet selSheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = selSheet.iterator();
		CsvWriterUnivocityImpl writer = new CsvWriterUnivocityImpl("test.csv");

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			ArrayList<String> rowCsv = convertRowToCSV(row, formatter, evaluator);
			writer.doWrite(rowCsv); 			
		}
		
		writer.doClose();

	}

	 private  ArrayList<String> convertRowToCSV(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
	        Cell cell = null;
	        int lastCellNum = 0;
	        ArrayList<String> csvLine = new ArrayList<String>();
	        if(row != null) {
	            lastCellNum = row.getLastCellNum();
	            for(int i = 0; i <= lastCellNum; i++) {
	                cell = row.getCell(i);
	                if(cell == null) {
	                    csvLine.add("");
	                }
	                else {
	                    if(cell.getCellTypeEnum() != CellType.FORMULA) {
	                        csvLine.add(formatter.formatCellValue(cell));
	                    }
	                    else {
	                        csvLine.add(formatter.formatCellValue(cell, evaluator));
	                    }
	                }
	            }
	           
	        }
	        return csvLine;
	  }
}
