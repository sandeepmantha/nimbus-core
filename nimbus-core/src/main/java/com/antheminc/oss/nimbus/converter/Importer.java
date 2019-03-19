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

import java.io.InputStream;

import com.antheminc.oss.nimbus.domain.cmd.Command;

/**
 * <p>A base importer interface for handling the import of data.
 * 
 * @author Tony Lopez
 * @author Sandeep Mantha
 *
 */
public interface Importer {

	/**
	 * <p>Import data from the provided resource by converting each record of
	 * data into a Java object and then saving that object. The java object each
	 * record of data will be converted to is determined by the configuration in
	 * the provided {@code Command}.
	 * 
	 * @param command the command that will mandate the conversion rules for
	 *            converting {@code file} into to a Java object
	 * @param stream the object containing the data to import
	 */
	<T> void doImport(Command command, InputStream stream);
}
