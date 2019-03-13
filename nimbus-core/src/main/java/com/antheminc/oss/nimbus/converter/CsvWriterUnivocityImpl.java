package com.antheminc.oss.nimbus.converter;

import java.io.File;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class CsvWriterUnivocityImpl implements GenericWriter {

	CsvWriterSettings settings;
	CsvWriter writer;
	
	public CsvWriterUnivocityImpl(String file) {
		this.settings = new CsvWriterSettings();
		settings.setNullValue("?");
		settings.getFormat().setComment('-');
		settings.setEmptyValue("!");
		settings.setHeaderWritingEnabled(true);
		settings.setSkipEmptyLines(false);
		this.writer = new CsvWriter(new File(file), settings);
	}
	
	@Override
	public void doWrite(Object rowCsv) {
		this.writer.writeRow(rowCsv);
	}

	@Override
	public void doClose() {
		this.writer.close();
	}

	
}
