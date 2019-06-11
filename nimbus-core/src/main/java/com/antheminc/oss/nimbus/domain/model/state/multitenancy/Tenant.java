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

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.antheminc.oss.nimbus.domain.defn.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Tony Lopez
 * @since 2.0
 *
 */
@Getter
@Setter
public class Tenant implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private Long id;
	
	private String clientId;
	private String orgId;
	private String appCode;
	
	@CreatedBy
	private String createdBy;

	@CreatedDate
	private ZonedDateTime createdDate;

	@LastModifiedBy
	private String lastModifiedBy;

	@LastModifiedDate
	private ZonedDateTime lastModifiedDate;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(sb, clientId);
		append(sb, orgId);
		append(sb, appCode);
		return sb.toString();
	}

	private void append(StringBuilder sb, String value) {
		String appendValue = !StringUtils.isEmpty(value) ? value : "null";
		sb.append(Constants.SEPARATOR_URI.code).append(appendValue);
	}
}
