
package com.antheminc.oss.nimbus.integration.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Sandeep Mantha
 */
public class ActiveMqPublisher {

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Value(value="${activemq.channel}")
	private String queueName;

	public void sendMessage(final MqMessage message) {
		jmsTemplate.convertAndSend(queueName, message);
	}

}
