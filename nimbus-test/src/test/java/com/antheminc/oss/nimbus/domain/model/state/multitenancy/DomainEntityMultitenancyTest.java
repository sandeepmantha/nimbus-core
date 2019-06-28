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
package com.antheminc.oss.nimbus.domain.model.state.multitenancy;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.channel.web.WebCommandDispatcher;
import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.MultiOutput;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecutorGateway;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.antheminc.oss.nimbus.test.domain.support.AbstractFrameworkIntegrationTests;
import com.antheminc.oss.nimbus.test.scenarios.s0.core.SampleCoreEntity;


/**
 * @author Tony Lopez
 *
 */
@TestPropertySource(properties = { 
		"nimbus.multitenancy.tenants.1.description=ABC",
		"nimbus.multitenancy.tenants.1.prefix=/foo/1/app",
		"nimbus.multitenancy.tenants.2.description=DEF",
		"nimbus.multitenancy.tenants.2.prefix=/foo/2/app",
})
public class DomainEntityMultitenancyTest extends AbstractFrameworkIntegrationTests {

	@Autowired
	private WebCommandDispatcher webCommandDispatcher;
	
	@Autowired
	private CommandExecutorGateway commandGateway;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMongoRecordLevelTenancyCreate() {
		Command cmd = CommandBuilder.withUri("/foo/2/app/p/sample_core/_new").getCommand();
		String payload = "{\"attr_String\": \"foo\"}";
		
		MultiOutput output = this.webCommandDispatcher.handle(cmd, payload);
		Param<SampleCoreEntity> result = (Param<SampleCoreEntity>) output.getSingleResult();
		Assert.assertEquals(Long.valueOf(2), result.getState().get_tenantId());
	}
	
	@Test(expected = FrameworkRuntimeException.class)
	public void testMongoRecordLevelTenancyMissingTenantId() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testMongoRecordLevelExampleSearch() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testMongoRecordLevelQuerySearch() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testMongoRecordLevelLookupSearch() {
		Assert.fail("Implement me!");
	}
}
