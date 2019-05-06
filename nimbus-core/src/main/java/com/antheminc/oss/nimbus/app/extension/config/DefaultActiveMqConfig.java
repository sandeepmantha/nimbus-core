
package com.antheminc.oss.nimbus.app.extension.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.domain.model.state.queue.ParamStateMQEventListener;
import com.antheminc.oss.nimbus.integration.mq.ActiveMqConsumer;
import com.antheminc.oss.nimbus.integration.mq.ActiveMqPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Sandeep Mantha
 * @author Tony Lopez
 */
@Configuration
@EnableJms
@ConditionalOnProperty(name = "spring.activemq.broker-url")
public class DefaultActiveMqConfig {

	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;

	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(brokerUrl);

		return activeMQConnectionFactory;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(activeMQConnectionFactory());

		return factory;
	}

	@Bean
	@ConditionalOnProperty(name = "activemq.inbound.channel")
	public ActiveMqConsumer mqconsumer(CommandExecutorGateway executorGateway, ObjectMapper om) {
		return new ActiveMqConsumer(executorGateway, om);
	}
	
	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		return new CachingConnectionFactory(activeMQConnectionFactory());
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
	
	@Bean(name="default.paramStateMqEventListener")
	@ConditionalOnBean(value=ActiveMqPublisher.class)
	public ParamStateMQEventListener paramStateMQEventListener(ActiveMqPublisher publisher){
		return new ParamStateMQEventListener(publisher);
	}
}
