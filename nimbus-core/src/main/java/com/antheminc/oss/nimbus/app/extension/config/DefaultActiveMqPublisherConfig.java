
package com.antheminc.oss.nimbus.app.extension.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.antheminc.oss.nimbus.integration.mq.ActiveMqPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Sandeep Mantha
 * @author Tony Lopez
 */
@Configuration
@ConditionalOnProperty(name = "spring.activemq.broker-url")
public class DefaultActiveMqPublisherConfig {

	@Value(value = "${spring.activemq.broker-url}")
	private String brokerUrl;

	@Bean
	public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(brokerUrl);

		return activeMQConnectionFactory;
	}

	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		return new CachingConnectionFactory(senderActiveMQConnectionFactory());
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		return new JmsTemplate(cachingConnectionFactory());
	}

	@Bean
	@ConditionalOnProperty(name = "activemq.outbound.channel")
	public ActiveMqPublisher publisher(ObjectMapper om) {
		return new ActiveMqPublisher(jmsTemplate(), om);
	}

}
