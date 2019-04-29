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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.cmd.CommandMessage;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.support.JustLogit;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO Remove the following docs before finalizing code...
 * 
 * 1. Start RabbitMQ:
 *   docker run -d --hostname my-rabbit --name nimbus-rabbitmq -p 15672:15672 -p 5672:5672 rabbitmq:3-management
 *   
 * 2. Ensure application.yml properties are set
 *   -> spring.cloud.stream.bindings.input.destination
 *   -> spring.cloud.stream.bindings.input.group
 *   
 * 3. Start petclinic-web
 * 
 * 4. Use Postman/ARC to post the following data:
 * 
 * 		URL: http://localhost:8080/petclinic/client/org/app/p/event/mq
 * 		{
 *			"commandUrl": "/a/b/p/owner/_new",
 *		  	"rawPayload": "{ \"firstName\": \"Silly\", \"lastName\": \"Salmon\" }"
 *		}
 *  
 * @author Tony Lopez
 *
 */
@EnableBinding(Sink.class)
@Getter @Setter
public class MQInputReceiver {
	
	public static final JustLogit LOG = new JustLogit(MQInputReceiver.class);
	
	@Autowired
	private CommandExecutorGateway executorGateway;

	@StreamListener(Sink.INPUT)
	public void input(SimpleCommandMessage msg) {
		LOG.debug(() -> "MQInputReceiver received a message: " + msg);
		validate(msg);
		getExecutorGateway().execute(toCommandMessage(msg));
	}
	
	private CommandMessage toCommandMessage(SimpleCommandMessage msg) {
		return new CommandMessage(CommandBuilder.withUri(msg.getCommandUrl()).getCommand(), msg.getRawPayload());
	}

	private void validate(SimpleCommandMessage msg) {
		if (null == msg) {
			throw new InvalidConfigException("An event message must not be null.");
		}
		if (null == msg.getCommandUrl()) {
			throw new InvalidConfigException("An event message must have a command that is not null.");
		}
	}
}
