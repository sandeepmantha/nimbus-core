/**
 *  Copyright 2016-2018 the original author or authors.
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
package com.antheminc.oss.nimbus.domain.cmd.exec.internal;

import org.drools.core.util.StringUtils;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.cmd.exec.AbstractCommandExecutor;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.Input;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.Output;
import com.antheminc.oss.nimbus.domain.cmd.exec.internal.nav.PageNavigationResponse;
import com.antheminc.oss.nimbus.domain.cmd.exec.internal.nav.PageNavigationResponse.Type;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.support.EnableLoggingInterceptor;

/**
 * @author Soham Chakravarti
 * @author Tony Lopez
 *
 */
@EnableLoggingInterceptor
public class DefaultActionExecutorNav<T> extends AbstractCommandExecutor<PageNavigationResponse> {

	public DefaultActionExecutorNav(BeanResolverStrategy beanResolver) {
		super(beanResolver);
	}

	@Override
	protected Output<PageNavigationResponse> executeInternal(Input input) {
		PageNavigationResponse response = buildResponse(input); 
		if (null == response) {
			throw new InvalidConfigException(
					"Unable to determine a navigation strategy from the provided command message. Please ensure the correct parameters were sent: "
							+ input.getContext().getCommandMessage());
		}
		
		return Output.instantiate(input, input.getContext(), response);
	}
	
	protected PageNavigationResponse buildResponse(Input input) {
		String pageId = input.getContext().getCommandMessage().getCommand()
				.getFirstParameterValue(Constants.KEY_NAV_ARG_PAGE_ID.code);
		if (!StringUtils.isEmpty(pageId)) {
			return PageNavigationResponse.builder().pageId(pageId).build();
		}

		String redirectUrl = input.getContext().getCommandMessage().getCommand()
				.getFirstParameterValue(Constants.KEY_NAV_ARG_REDIRECT_URL.code);
		if (!StringUtils.isEmpty(redirectUrl)) {
			return PageNavigationResponse.builder().type(Type.EXTERNAL).redirectUrl(redirectUrl).build();
		}
		
		return null;
	}
}