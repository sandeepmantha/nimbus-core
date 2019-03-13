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
package com.antheminc.oss.nimbus.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.core.io.Resource;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.domain.config.builder.DomainConfigBuilder;
import com.antheminc.oss.nimbus.domain.model.config.ModelConfig;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepositoryFactory;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.BeanProcessor;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p>An csv file importer interface that imports data from an .csv file into
 * the provided {@link ModelRepository}.
 * 
 * <p>This implementation uses Univocity to take advantage of the parsing
 * features available.
 * 
 * @author Tony Lopez
 * @author Sandeep Mantha
 *
 */
@RequiredArgsConstructor
@Getter
@Setter
public class CsvFileImporter extends FileImporter {


	public final static String[] SUPPORTED_EXTENSIONS = new String[] { "csv" };

	private final DomainConfigBuilder domainConfigBuilder;
	private final ModelRepositoryFactory modelRepositoryFactory;

	@Getter
	public class PersistenceProcessor<S> extends BeanProcessor<S> {

		private final ModelConfig<S> modelConfig;
		private final ModelRepository modelRepository;

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

	private CsvParserSettings parserSettings = new CsvParserSettings();

	@Override

	public <T> void doImport(File file, String domainAlias) {
		ModelConfig<?> rootModelConfig = getDomainConfigBuilder().getModel(domainAlias);
		ModelRepository modelRepository = getModelRepositoryFactory().get(rootModelConfig.getRepo());
		BeanProcessor<?> rowProcessor = new PersistenceProcessor<>(modelRepository, rootModelConfig);
		getParserSettings().setProcessor(rowProcessor);
		new com.univocity.parsers.csv.CsvParser(getParserSettings()).parse(file);
	}

	public boolean supports(String extension) {
		return ArrayUtils.contains(SUPPORTED_EXTENSIONS, extension);
	}

	@Override
	public <T> void doImport(InputStream inputStream, String domainAlias) {
		new com.univocity.parsers.csv.CsvParser(getParserSettings()).parse(inputStream);
		
	}
}
