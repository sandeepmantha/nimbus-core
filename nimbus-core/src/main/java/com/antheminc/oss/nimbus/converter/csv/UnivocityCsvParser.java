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
package com.antheminc.oss.nimbus.converter.csv;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.antheminc.oss.nimbus.converter.FileParser;
import com.antheminc.oss.nimbus.converter.RowProcessable;
import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.config.builder.DomainConfigBuilder;
import com.antheminc.oss.nimbus.support.BeanUtils;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import com.univocity.parsers.common.processor.BeanProcessor;
import com.univocity.parsers.common.processor.ConcurrentRowProcessor;
import com.univocity.parsers.common.processor.RowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p>A wrapper around Univocity to expose desired features for parsing CSV
 * files.
 * 
 * @author Tony Lopez
 *
 */
@RequiredArgsConstructor
@Getter
public class UnivocityCsvParser implements FileParser, RowProcessable {

	/**
	 * <p>A simple row processor that invokes the provided {@link BeanWriter}
	 * strategy to write bean data.
	 * 
	 * @author Tony Lopez
	 *
	 * @param <S> the bean type
	 */
	@Getter
	public class BeanWritingRowProcessor<S> extends BeanProcessor<S> {

		private final BeanWriter writer;

		public BeanWritingRowProcessor(Class<S> clazz, BeanWriter writer) {
			super(clazz);
			this.writer = writer;
		}

		@Override
		public void beanProcessed(S bean, ParsingContext context) {
			writer.write(bean);
		}
	}

	/**
	 * <p>A simple wrapper for that invokes the provided {@link RowErrorHandler}
	 * strategy to handle errors when an exception related to parsing row data occurs.
	 * 
	 * @author Tony Lopez
	 *
	 * @param <S> the bean type
	 */
	@RequiredArgsConstructor
	@Getter
	public class RowProcessorErrorHandlerWrapper implements RowProcessorErrorHandler {

		private final RowErrorHandler errorHandler;

		@Override
		public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
			errorHandler.handleError(error, inputRow);
		}

	}

	private final DomainConfigBuilder domainConfigBuilder;

	private BeanWriter onRowProcess;
	private RowErrorHandler onError;
	@Setter
	private boolean parallel;

	@Override
	public void onRowProcess(BeanWriter writer) {
		this.onRowProcess = writer;
	}

	@Override
	public void onRowProcessError(RowErrorHandler onError) {
		this.onError = onError;
	}

	@Override
	public void parse(InputStream stream, Command command) {
		CsvParserSettings settings = buildSettings(command);
		prepareRowProcessing(command, settings);
		prepareErrorHandling(command, settings);
		new CsvParser(settings).parse(stream);
	}

	private CsvParserSettings buildSettings(Command command) {
		CsvParserSettings settings = new CsvParserSettings();
		
		// load any applicable request params into the csv parser settings
		if (null != command.getRequestParams()) {
			Map<String, String> properties = new HashMap<>();
			for(Entry<String, String[]> entry: command.getRequestParams().entrySet()) {
				if (entry.getValue().length > 0) {
					properties.put(entry.getKey(), entry.getValue()[0]);
				}
			}
			BeanUtils.copyProperties(settings, properties);
		}
		
		return settings;
	}

	protected void prepareErrorHandling(Command command, CsvParserSettings settings) {
		if (null != this.onError) {
			settings.setProcessorErrorHandler(new RowProcessorErrorHandlerWrapper(this.onError));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void prepareRowProcessing(Command command, CsvParserSettings settings) {
		if (null != this.onRowProcess) {
			Class<?> beanClass = getDomainConfigBuilder().getModel(command.getRootDomainAlias()).getReferredClass();
			RowProcessor rowProcessor = new BeanWritingRowProcessor(beanClass, this.onRowProcess);
			if (this.parallel) {
				rowProcessor = new ConcurrentRowProcessor(rowProcessor);
			}
			settings.setProcessor(rowProcessor);
		}
	}
}
