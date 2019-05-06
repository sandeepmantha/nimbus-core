/**
 *  Copyright 2016-2018 the original author or authors.
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
package com.antheminc.oss.nimbus.integration.mq;

import java.io.IOException;

import org.springframework.jms.annotation.JmsListener;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.cmd.CommandMessage;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.support.JustLogit;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Sandeep Mantha
 * @author Tony Lopez
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ActiveMqConsumer {

	public static final JustLogit LOG = new JustLogit(ActiveMqConsumer.class);
	
	private final CommandExecutorGateway executorGateway;
	private final ObjectMapper om;
	
	@JmsListener(destination = "${activemq.inbound.channel}")
	public void receive(String message) {
		LOG.debug(() -> "received message=" + message);
		// TODO Create session?
		getExecutorGateway().execute(toCommandMessage(message));
	}

	private CommandMessage toCommandMessage(String message) {
		MqMessage msg;
		try {
			msg = this.om.readValue(message, MqMessage.class);
		} catch (IOException e) {
			throw new FrameworkRuntimeException("Failed to parse message from message queue. Please ensure the message is in the correct format. Message: " + message);
		}
		return new CommandMessage(CommandBuilder.withUri(msg.getCommandUrl()).getCommand(), msg.getRawPayload());
	}
}
