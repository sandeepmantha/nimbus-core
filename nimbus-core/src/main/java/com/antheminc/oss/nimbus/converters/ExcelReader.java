package com.antheminc.oss.nimbus.converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

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

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ExcelReader<T> implements SourceReader, InitializingBean{

    private Workbook workbook;
    private DataFormatter formatter;
    private FormulaEvaluator evaluator;
    private ExcelReaderSettings settings;
    
    public ExcelReader(ExcelReaderSettings settings) {
    	this.settings = settings;
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doOpen() {
            try {
				this.workbook = WorkbookFactory.create(this.settings.getResource().getFile());
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
		
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			CsvWriterUnivocityImpl writer = new CsvWriterUnivocityImpl("/Users/ac97583/Downloads/test/test.csv");
			ArrayList<String> rowCsv = convertRowToCSV(row, formatter, evaluator);
			writer.doWrite(rowCsv); 			
		}
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
