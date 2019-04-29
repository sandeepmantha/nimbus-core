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
package com.antheminc.oss.nimbus.domain.model.state.mq;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.domain.model.state.CommandMessageEvent;
import com.antheminc.oss.nimbus.domain.model.state.event.listener.EventListener;
import com.antheminc.oss.nimbus.support.JustLogit;

/**
 * @author Tony Lopez
 *
 */
public class MQEventListener implements EventListener<CommandMessageEvent<String>> {

	public static final JustLogit LOG = new JustLogit(MQEventListener.class);

	@Override
	public boolean listen(CommandMessageEvent<String> event) {
		LOG.debug(() -> "Received a command message to execute: " + event);
		if (null == event.getSource()) {
			throw new InvalidConfigException(
					"Source must be provided with the event message to send to the message queue.");
		}

		return publish(event.getSource(), toSimpleCommandMessage(event));
	}

	public boolean publish(Source source, Object payload) {
		return source.output().send(MessageBuilder.withPayload(payload).build());
	}

	private SimpleCommandMessage toSimpleCommandMessage(CommandMessageEvent<String> event) {
		SimpleCommandMessage cmdMsg = new SimpleCommandMessage();
		cmdMsg.setCommandUrl(event.getCommandUrl());
		cmdMsg.setRawPayload(event.getPayload());
		return cmdMsg;
	}

}
