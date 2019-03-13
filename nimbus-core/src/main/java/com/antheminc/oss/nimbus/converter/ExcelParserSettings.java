package com.antheminc.oss.nimbus.converter;

import java.io.File;
import java.io.InputStream;

import org.springframework.core.io.Resource;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ExcelParserSettings {

	private File file;
	private InputStream inpStream;
}
