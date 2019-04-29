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
package com.antheminc.oss.nimbus.domain.model.state;

import org.springframework.cloud.stream.messaging.Source;

import com.antheminc.oss.nimbus.domain.cmd.Action;
import com.antheminc.oss.nimbus.domain.model.state.internal.AbstractEvent;

import lombok.Getter;

/**
 * @author Tony Lopez
 *
 */
@Getter
public class CommandMessageEvent<P> extends AbstractEvent<String, P> {
	
	private final Source source;
	
	public CommandMessageEvent(Source source, Action a, String commandUrl, P payload) {
		super(a.toString(), commandUrl, payload);
		this.source = source;
	}
	
	public String getCommandUrl() {
		return getId();
	}

}
