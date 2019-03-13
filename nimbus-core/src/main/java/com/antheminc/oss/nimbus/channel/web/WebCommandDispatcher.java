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
/**
 *
 */
package com.antheminc.oss.nimbus.channel.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.converter.CsvFileImporter;
import com.antheminc.oss.nimbus.converter.ExcelFileImporter;
import com.antheminc.oss.nimbus.converter.ExcelParserSettings;
import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.MultiOutput;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.domain.model.state.ModelEvent;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepositoryFactory;
import com.antheminc.oss.nimbus.support.EnableLoggingInterceptor;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Getter;

/**
 * @author Soham Chakravarti
 *
 */
@Getter
@EnableLoggingInterceptor
public class WebCommandDispatcher {

	private final WebCommandBuilder builder;

	private final CommandExecutorGateway gateway;

	private final ModelRepository modelRepository;
	
	private final CsvFileImporter csvFileImporter;
	private final ExcelFileImporter excelFileImporter;	
	
	public WebCommandDispatcher(BeanResolverStrategy beanResolver) {
		this.builder = beanResolver.get(WebCommandBuilder.class);
		this.gateway = beanResolver.get(CommandExecutorGateway.class);
		this.modelRepository =beanResolver.get(ModelRepository.class);
		this.csvFileImporter = beanResolver.get(CsvFileImporter.class);
		this.excelFileImporter = beanResolver.get(ExcelFileImporter.class);
	}
	
	public Object handle(HttpServletRequest httpReq, ModelEvent<String> event) {
		Command cmd = getBuilder().build(httpReq, event);
		return handle(cmd, event.getPayload());
	}

	public Object handle(HttpServletRequest httpReq, String json) {
		Command cmd = getBuilder().build(httpReq);
		return handle(cmd, json);
	}

	public MultiOutput handle(Command cmd, String payload) {
		return getGateway().execute(cmd, payload);
	}

	public Object handle(HttpServletRequest httpReq, CommonsMultipartFile file) {
		String message = "";
		
		if (!file.isEmpty()) {
			FileItem fileItem = file.getFileItem();
			String name = fileItem.getName();
			try {
				String ext = FilenameUtils.getExtension(name);
				switch (ext) {
				
				case "xlsx":
					getExcelFileImporter().setExcelParserSettings(new ExcelParserSettings());
					getExcelFileImporter().doImport(fileItem.getInputStream(), getModelRepository(), "mypojo");
					break;
					
				case "csv":
					getCsvFileImporter().setParserSettings(new CsvParserSettings());
					getCsvFileImporter().doImport(fileItem.getInputStream(), getModelRepository(), "mypojo");

					break;
					
				default:
					throw new InvalidConfigException("File type \"." + ext + "\" is not supported.");
			}
				message = "You successfully uploaded file=" + name;
			} catch (Exception e) {
				message = "You failed to upload " + name + " => " + e.getMessage();
			}
		} else {
			message = "You failed to upload " 
					+ " because the file was empty.";
		}
		return message;
	}
}
