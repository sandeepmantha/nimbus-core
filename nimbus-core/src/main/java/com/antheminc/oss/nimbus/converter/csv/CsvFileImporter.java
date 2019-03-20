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
import java.util.function.BiConsumer;

import org.apache.commons.lang.ArrayUtils;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.converter.FileImporter;
import com.antheminc.oss.nimbus.converter.FileParser;
import com.antheminc.oss.nimbus.converter.RowProcessable;
import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.domain.config.builder.DomainConfigBuilder;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;
import com.antheminc.oss.nimbus.support.JustLogit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Enums;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>An csv file importer interface that imports data from an .csv file into
 * the provided {@link ModelRepository}.
 * 
 * @author Tony Lopez
 * @author Sandeep Mantha
 *
 */
@Getter
@Setter
public class CsvFileImporter extends FileImporter {

	public static enum ErrorHandling {
		SILENT, STRICT;
	}

	public static final JustLogit LOG = new JustLogit();

	public static final String ARG_ERROR_HANDLING = "errors";
	public static final String ARG_PARALLEL = "parallel";
	public static final String CSV = "csv";
	public static final String[] SUPPORTED_EXTENSIONS = new String[] { CSV };

	private final CommandExecutorGateway commandGateway;
	private final DomainConfigBuilder domainConfigBuilder;
	private final ObjectMapper om;

	private FileParser fileParser;

	private BiConsumer<RuntimeException, Object[]> silentErrorHandler = (e, rowData) -> {
	};
	private BiConsumer<RuntimeException, Object[]> strictErrorHandler = (e, rowData) -> {
		throw new FrameworkRuntimeException(e);
	};

	public CsvFileImporter(CommandExecutorGateway commandGateway, DomainConfigBuilder domainConfigBuilder,
			ObjectMapper om) {
		this.commandGateway = commandGateway;
		this.domainConfigBuilder = domainConfigBuilder;
		this.om = om;
		
		// TODO consider moving this to a bean
		this.fileParser = new UnivocityCsvParser(domainConfigBuilder);
	}

	@Override
	public <T> void doImport(Command command, InputStream stream) {
		prepareRowProcessing(command);
		prepareErrorHandling(command);
		getFileParser().parse(stream, command);
	}

	protected void prepareRowProcessing(Command command) {
		((RowProcessable) getFileParser()).onRowProcess((Object bean) -> {
			String payload;
			try {
				payload = om.writeValueAsString(bean);
			} catch (JsonProcessingException e) {
				throw new FrameworkRuntimeException("Failed to convert row data to JSON during import.", e);
			}
			commandGateway.execute(command, payload);
		});
		((RowProcessable) getFileParser()).setParallel(Boolean.valueOf(command.getFirstParameterValue(ARG_PARALLEL)));
	}

	protected void prepareErrorHandling(Command command) {
		ErrorHandling errorHandling = ErrorHandling.SILENT;
		String sErrorHandling = command.getFirstParameterValue(ARG_ERROR_HANDLING);
		if (null != sErrorHandling) {
			errorHandling = Enums.getIfPresent(ErrorHandling.class, sErrorHandling.toUpperCase())
					.or(ErrorHandling.SILENT);
		}

		final BiConsumer<RuntimeException, Object[]> onErrorHandler;
		if (ErrorHandling.SILENT == errorHandling) {
			onErrorHandler = getSilentErrorHandler();
		} else if (ErrorHandling.STRICT == errorHandling) {
			onErrorHandler = getStrictErrorHandler();
		} else {
			throw new UnsupportedOperationException("Error handling for " + errorHandling + " is not supported.");
		}

		((RowProcessable) getFileParser()).onRowProcessError(onErrorHandler);
	}

	@Override
	public boolean supports(String extension) {
		return ArrayUtils.contains(SUPPORTED_EXTENSIONS, extension);
	}
}
