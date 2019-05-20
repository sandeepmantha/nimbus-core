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
package com.antheminc.oss.nimbus.domain.cmd.exec.internal.nav;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * @author Tony Lopez
 *
 */
@Builder
@Data
public class PageNavigationResponse {
	
	@Getter
	public static enum Type {
		INTERNAL,
		EXTERNAL;
	}
	
	@Builder.Default
	private Type type = Type.INTERNAL;
	private String pageId;
	private String redirectUrl;
}
