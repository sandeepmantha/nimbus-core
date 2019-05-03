
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
