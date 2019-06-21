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
package com.antheminc.oss.nimbus.channel;

import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.MultiOutput;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.domain.model.state.multitenancy.TenantRepository;

import lombok.Getter;

/**
 * @author Tony Lopez
 *
 */
@Getter
public abstract class CommandDispatcher {

	protected final CommandExecutorGateway gateway;
	protected final TenantRepository tenantRepository;
	
	public CommandDispatcher(BeanResolverStrategy beanResolver) {
		this.gateway = beanResolver.get(CommandExecutorGateway.class);
		this.tenantRepository = beanResolver.get(TenantRepository.class);
	}
	
	public MultiOutput handle(Command cmd, String payload) {
		beforeCommandExecution(cmd);
		return getGateway().execute(cmd, payload);
	}
	
	protected void beforeCommandExecution(Command cmd) {
		handleTenancy(cmd);
	}
	
	protected void handleTenancy(Command cmd) {
		new CommandBuilder(cmd).setTenant(this.tenantRepository);
	}
}
