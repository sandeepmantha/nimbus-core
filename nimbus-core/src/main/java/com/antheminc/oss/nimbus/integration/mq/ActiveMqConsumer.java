
package com.antheminc.oss.nimbus.integration.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.cmd.CommandMessage;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sandeep Mantha
 */
@Getter
@Setter
public class ActiveMqConsumer {

	@Autowired
	private CommandExecutorGateway executorGateway;
	
	@JmsListener(destination = "${activemq.channel}")
	public void receive(MqMessage message) {
		getExecutorGateway().execute(toCommandMessage(message));
		System.out.println("received message='{}'" + message);
	}

	private CommandMessage toCommandMessage(MqMessage msg) {
		return new CommandMessage(CommandBuilder.withUri(msg.getCommandUrl()).getCommand(), msg.getRawPayload());
	}
}
