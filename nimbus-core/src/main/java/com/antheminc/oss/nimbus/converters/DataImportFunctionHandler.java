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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.cmd.exec.ExecutionContext;
import com.antheminc.oss.nimbus.domain.cmd.exec.FunctionHandler;
import com.antheminc.oss.nimbus.domain.model.config.ModelConfig;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Model;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;
import com.univocity.parsers.csv.CsvParserSettings;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepositoryFactory;

import lombok.Getter;

/**
 * @author Tony Lopez
 * @author Sandeep Mantha
 *
 */
@Getter
public class DataImportFunctionHandler<T> implements FunctionHandler<T, Void> {

	public static final String ARG_FILE = "file";
	
	private final ModelRepositoryFactory modelRepositoryFactory;
	
	private final CSVFileImporter csvFileImporter;
	private final ExcelFileImporter excelFileImporter;
	
	public DataImportFunctionHandler(ModelRepositoryFactory modelRepositoryFactory, BeanResolverStrategy beanResolver) {
		this.modelRepositoryFactory = modelRepositoryFactory;
		this.csvFileImporter = beanResolver.get(CSVFileImporter.class);
		this.excelFileImporter = beanResolver.get(ExcelFileImporter.class);
}

	// /p/domain/_process?fn=_dataImport&file=sample.xlsx
	@Override
	public Void execute(ExecutionContext eCtx, Param<T> actionParameter) {

		validate(eCtx, actionParameter);
		
		ModelRepository modelRepository = getModelRepositoryFactory().get(actionParameter.getRootDomain().getConfig().getRepo());
		
		if (shouldHandleAsFile(eCtx)) {
			final String filename = eCtx.getCommandMessage().getCommand().getFirstParameterValue(ARG_FILE);
			handleFile(filename, modelRepository, actionParameter.getRootDomain().findIfRoot().getConfig());
		}
		
		return null;
	}

	/**
	 * <p>Validate actionParameter is a root persistable param.
	 * @param eCtx
	 * @param actionParameter
	 */
	private void validate(ExecutionContext eCtx, Param<T> actionParameter) {
		Model<?> rootModel = actionParameter.getRootDomain();
		if(!actionParameter.equals(rootModel.getAssociatedParam())) {
			throw new InvalidConfigException("Import function handler is only allowed to execute over root domain entities.");
		}
		
		if(null == rootModel.getConfig().getRepo()) {
			throw new InvalidConfigException("Unable to import data without @Repo configuration.");
		}
	}

	private boolean shouldHandleAsFile(ExecutionContext eCtx) {
		return StringUtils.isNotEmpty(eCtx.getCommandMessage().getCommand().getFirstParameterValue(ARG_FILE));
	}

	private void handleFile(String filename, ModelRepository modelRepository, ModelConfig<?> rootModelConfig) {
		
		File file = FileUtils.getFile(filename);
		Resource resource = new FileSystemResource(file);
		
		String extension = FilenameUtils.getExtension(filename);
		switch (extension) {
		
			case "xlsx":
				ExcelParserSettings excelSettings = new ExcelParserSettings();
				this.excelFileImporter.doImport(resource, modelRepository, rootModelConfig, excelSettings);
				break;
				
			case "csv":
				CsvParserSettings csvParserSettings = new CsvParserSettings();
				this.csvFileImporter.doImport(resource, modelRepository, rootModelConfig, csvParserSettings	);

				break;
				
			default:
				throw new InvalidConfigException("File type \"." + extension + "\" is not supported.");
		}
	}
}
