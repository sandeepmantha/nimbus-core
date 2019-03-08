/**
 *  Copyright 2016-2018 the original author or authors.
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
package com.antheminc.oss.nimbus.converters;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.domain.cmd.exec.ExecutionContext;
import com.antheminc.oss.nimbus.domain.cmd.exec.FunctionHandler;
import com.antheminc.oss.nimbus.domain.model.config.ModelConfig;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;

import lombok.Getter;

/**
 * @author Tony Lopez
 *
 */
@Getter
public class DataImportFunctionHandler<T> implements FunctionHandler<T, Void> {

	private final ModelRepository modelRepository;
	
	private final CSVFileImporter csvFileImporter;
	private final ExcelFileImporter excelFileImporter;
	
	public DataImportFunctionHandler(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
		
		// TODO if we need to externalize these we can
		this.csvFileImporter = new CSVFileImporter();
		this.excelFileImporter = new ExcelFileImporter();
	}

	// /p/domain/_process?fn=_dataImport&file=sample.xlsx
	@Override
	public Void execute(ExecutionContext eCtx, Param<T> actionParameter) {
		// validate actionParameter is a root persistable param
		
		if (shouldHandleAsFile(eCtx)) {
			// TODO get file name from eCtx
			final String filename = "sample.xlsx";
			
			handleFile(filename, actionParameter.getRootDomain().findIfRoot().getConfig());
		}
		
		return null;
	}

	private boolean shouldHandleAsFile(ExecutionContext eCtx) {
		// TODO Determine if should be handled as a file
		return true;
	}

	private void handleFile(String filename, ModelConfig<?> modelConfig) {
		
		// TODO get the file appropriately
		File file = FileUtils.getFile(filename);
		Resource resource = new FileSystemResource(file);
		
		String extension = FilenameUtils.getExtension(filename);
		switch (extension) {
		
			case "xlsx":
				this.excelFileImporter.doImport(resource, modelRepository, modelConfig);
				break;
				
			case "csv":
				this.csvFileImporter.doImport(resource, modelRepository, modelConfig);
				break;
				
			default:
				throw new InvalidConfigException("File type \"." + extension + "\" is not supported.");
		}
	}
}
