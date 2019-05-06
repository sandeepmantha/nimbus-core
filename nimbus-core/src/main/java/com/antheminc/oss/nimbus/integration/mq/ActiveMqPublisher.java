
package com.antheminc.oss.nimbus.integration.mq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class ActiveMqPublisher {

	private final JmsTemplate jmsTemplate;	
	private final ObjectMapper om;
	
	@Value(value="${activemq.outbound.channel}")
	private String queueName;

	public void sendMessage(final Param<?> param) {
		String sMessage;
		try {
			sMessage = this.om.writeValueAsString(param);
		} catch (JsonProcessingException e) {
			throw new FrameworkRuntimeException("Failed to convert message to string. Message: " + param);
		}
		jmsTemplate.convertAndSend(queueName, sMessage);
	}
}
