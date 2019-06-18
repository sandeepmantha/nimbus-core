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
package com.antheminc.oss.nimbus.app.extension.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.model.state.multitenancy.DefaultTenantRepository;
import com.antheminc.oss.nimbus.domain.model.state.multitenancy.MultitenancyProperties;
import com.antheminc.oss.nimbus.domain.model.state.multitenancy.TenantFilter;
import com.antheminc.oss.nimbus.domain.model.state.multitenancy.TenantRepository;

/**
 * @author Tony Lopez
 *
 */
public class DefaultMultitenancyConfig {

	@Bean
	public MultitenancyProperties multitenancyProperties() {
		return new MultitenancyProperties();
	}
	
	@Bean
	@ConditionalOnProperty(name = "nimbus.multitenancy.enabled", havingValue = "true")
	public TenantRepository tenantRepository(MultitenancyProperties multitenancyProperties) {
		return new DefaultTenantRepository(multitenancyProperties);
	}
	
	@Bean
	public TenantFilter tenantFilter(BeanResolverStrategy beanResolver) {
		return new TenantFilter(beanResolver);
	}
	
}
