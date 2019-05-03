
package com.antheminc.oss.nimbus.app.extension.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.integration.mq.ActiveMqConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Sandeep Mantha
 * @author Tony Lopez
 */
@Configuration
@EnableJms
@ConditionalOnProperty(name = "spring.activemq.broker-url")
public class DefaultActiveMqConsumerConfig {

	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;

	@Bean
	public ActiveMQConnectionFactory receiverActiveMQConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(brokerUrl);

		return activeMQConnectionFactory;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(receiverActiveMQConnectionFactory());

		return factory;
	}

	@Bean
	@ConditionalOnProperty(name = "activemq.inbound.channel")
	public ActiveMqConsumer mqconsumer(CommandExecutorGateway executorGateway, ObjectMapper om) {
		return new ActiveMqConsumer(executorGateway, om);
	}
}
