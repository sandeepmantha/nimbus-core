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

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.domain.model.config.ModelConfig;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;
import com.antheminc.oss.nimbus.support.JustLogit;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.BeanProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Getter;

/**
 * @author Tony Lopez
 *
 */
public class CSVFileImporter extends FileImporter {

	private final CsvParserSettings parserSettings;
	
	public CSVFileImporter() {
		this.parserSettings = new CsvParserSettings();
		parserSettings.getFormat().setLineSeparator("\n");
		parserSettings.setHeaderExtractionEnabled(true);
	}
	
	@Override
	public void doImport(Resource resource, ModelRepository modelRepository, ModelConfig<?> modelConfig) {
		BeanProcessor<?> rowProcessor = new PersistenceProcessor(modelRepository, modelConfig);
		this.parserSettings.setProcessor(rowProcessor);
		try {
			new CsvParser(parserSettings).parse(resource.getFile());
		} catch (IOException e) {
			throw new FrameworkRuntimeException(e);
		}
	}

	@Getter
	public class PersistenceProcessor<S> extends BeanProcessor<S> {
		
		private final ModelRepository modelRepository;
		private final ModelConfig<S> modelConfig;
		
		public PersistenceProcessor(ModelRepository modelRepository, ModelConfig<S> modelConfig) {
			super(modelConfig.getReferredClass());
			this.modelRepository = modelRepository;
			this.modelConfig = modelConfig;
		}

		@Override
		public void beanProcessed(S bean, ParsingContext context) {
			this.modelRepository._new(getModelConfig(), bean);
		}
	}
}
