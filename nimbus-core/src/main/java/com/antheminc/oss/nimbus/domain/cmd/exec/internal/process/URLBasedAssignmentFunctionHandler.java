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
package com.antheminc.oss.nimbus.domain.cmd.exec.internal.process;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.cmd.CommandMessage;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.MultiOutput;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.domain.cmd.exec.ExecutionContext;
import com.antheminc.oss.nimbus.domain.cmd.exec.FunctionHandler;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;

/**
 * @author Jayant Chaudhuri
 *
 */
abstract public class URLBasedAssignmentFunctionHandler<T,R,S> implements FunctionHandler<T,R> {
	
	@Autowired
	private CommandExecutorGateway executorGateway; 
	
	@SuppressWarnings("unchecked")
	@Override
	public R execute(ExecutionContext executionContext, Param<T> actionParameter) {
		CommandMessage commandFromContext = null;
		S state = null;
		//TODO - Expose 2 other flavors for _set. 1. Set by value 2. Set by executing rule file.
		//TODO - When we set by value, if the value is something like Status.INACTIVE, have to use querydsl replace or come up with some other approach
		Param<S> targetParameter = findTargetParam(executionContext);
		if(StringUtils.isNotBlank(executionContext.getCommandMessage().getCommand().getFirstParameterValue("value"))) {
			commandFromContext =  executionContext.getCommandMessage();
			state = (S) commandFromContext.getCommand().getFirstParameterValue("value");
		} else {
			state = isInternal(executionContext.getCommandMessage()) ? getInternalState(executionContext): getExternalState(executionContext);
		}
		return assign(executionContext,actionParameter,targetParameter,state);
	}
	
	abstract public R assign(ExecutionContext executionContext, Param<T> actionParameter,Param<S> targetParameter, S state);

	//TODO - need to revisit the design of how to pass the where clause in a url. In the below example the _search where clause is being truncated as we are looking at only the url query parameter
	//Ex - /pageOrgUserGroupList/tileUserGroups/sectionUserGroups/userGroupList.m/_process?fn=_set&url=/p/clientusergroup/_search?fn=query&where=clientusergroup.organizationId.eq('<!/.m/id!>')
	protected String getUrl(CommandMessage commandMessage){
		String url = commandMessage.getCommand().getFirstParameterValue("url");
		//Temporarily added the below code. Need to revisit.
		String where = commandMessage.getCommand().getFirstParameterValue("where");
		String orderby = commandMessage.getCommand().getFirstParameterValue("orderby");
		String fetch = commandMessage.getCommand().getFirstParameterValue("fetch");
		String converter = commandMessage.getCommand().getFirstParameterValue("converter");
		String aggregate = commandMessage.getCommand().getFirstParameterValue("aggregate");
		String project = commandMessage.getCommand().getFirstParameterValue("project");
		
		if(StringUtils.isNotBlank(where)) {
			url = url+"&where="+where;
		}
		if(StringUtils.isNotBlank(orderby)) {
			url =  url+"&orderby="+orderby;
		}		
		if(StringUtils.isNotBlank(fetch)) {
			url =  url+"&fetch="+fetch;
		}
		if(StringUtils.isNotBlank(converter)) {
			url =  url+"&converter="+converter;
		}
		if(StringUtils.isNotBlank(aggregate)) {
			url =  url+"&aggregate="+aggregate;
		}
		if(StringUtils.isNotBlank(project)) {
			url =  url+"&project="+project;
		}
		return url;
	}
	
	protected CommandMessage buildExternalCommand(CommandMessage commandMessage){
		String url = getUrl(commandMessage);
		url = commandMessage.getCommand().getRelativeUri(url);
		Command command = CommandBuilder.withUri(url).getCommand();
		
		// TODO Sandeep: decide on which commands should get the payload. Scenario is - we are searching for a form based input. In the below query we are do a member search based on the variable search criteria 
		// Ex - /pageAdvancedMemberSearch/tileAdvancedMemberSearch/sectionMemberSearchResults/patientResult.m/_process?fn=_set&url=/p/patient/_search?fn=example 
		// To decide if we have to resolve the payload and then create the command message
		
		CommandMessage newCommandMessage = new CommandMessage(command, commandMessage.hasPayload() ? commandMessage.getRawPayload() :null);
		return newCommandMessage;
	}
	

	protected boolean isInternal(CommandMessage commandMessage){
		String url = commandMessage.getCommand().getFirstParameterValue("url");
		if(StringUtils.startsWith(url, Constants.SEPARATOR_URI_PLATFORM.code)) {
			return false;
		}
		return true;
	}
	
	protected Param<S> findTargetParam(ExecutionContext context){
		String parameterPath = context.getCommandMessage().getCommand().getAbsoluteDomainAlias();
		return context.getRootModel().findParamByPath(parameterPath);
	}	
	
	protected S getInternalState(ExecutionContext executionContext){
		String url = getUrl(executionContext.getCommandMessage());
		return executionContext.findStateByPath(url);
		
	}
	
	protected S getExternalState(ExecutionContext executionContext){
		CommandMessage commandToExecute = buildExternalCommand(executionContext.getCommandMessage());
		
		MultiOutput response = executorGateway.execute(commandToExecute);
		//TODO Soham: temp fix, need to talk to Jayant
		return (S)response.getOutputs().get(0).getValue();
	}	

}
