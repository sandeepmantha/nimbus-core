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
package com.antheminc.oss.nimbus.domain.model.state.multitenancy;

import org.apache.commons.lang3.StringUtils;

import com.antheminc.oss.nimbus.domain.defn.Constants;

import lombok.Data;

/**
 * @author Tony Lopez
 * @since 2.0
 *
 */
@Data
public class TenantID {
	
	private String clientId;
	private String orgId;
	private String appCode;
	private String version;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendIfNotEmpty(sb, clientId);
		appendIfNotEmpty(sb, orgId);
		appendIfNotEmpty(sb, appCode);
		appendIfNotEmpty(sb, version);
		return sb.toString();
	}
	
	private void appendIfNotEmpty(StringBuilder sb, String value) {
		if (!StringUtils.isEmpty(value)) {
			sb.append(Constants.SEPARATOR_URI.code).append(value);
		}
	}
}
