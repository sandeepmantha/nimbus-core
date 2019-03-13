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

import org.springframework.core.io.Resource;

/**
 * <p>A base importer interface for handling the import of data.
 * 
 * @author Tony Lopez
 * @author Sandeep Mantha
 *
 */
public interface Importer {

	/**
	 * <p>Import data from the provided {@code resource} object by converting to
	 * each record of data into a Java object and then insert that data into the
	 * configured provided repository. The java object each record of data will
	 * be converted to is determined by the configuration found for
	 * {@code domainAlias}.
	 * @param resource the object containing the data to import
	 * @param domainAlias the alias that will mandate the conversion rules for
	 *            converting {@code resource} into to a Java object
	 */
	<T> void doImport(Resource resource, String domainAlias);
}
