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
package com.antheminc.oss.nimbus.domain.cmd.exec.internal.fn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;

import com.antheminc.oss.nimbus.domain.AbstractFrameworkIngerationPersistableTests;
import com.antheminc.oss.nimbus.domain.cmd.Action;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.ListParam;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.antheminc.oss.nimbus.test.domain.support.utils.ExtractResponseOutputUtils;
import com.antheminc.oss.nimbus.test.domain.support.utils.MockHttpRequestBuilder;
import com.antheminc.oss.nimbus.test.scenarios.s0.core.SampleCoreEntity;
import com.antheminc.oss.nimbus.test.scenarios.s0.core.SampleCoreNestedEntity;
import com.antheminc.oss.nimbus.test.scenarios.s0.view.VPSampleViewPageBlue.Section_ConvertedNestedEntity;
import com.antheminc.oss.nimbus.test.scenarios.s0.view.VPSampleViewPageGreen;
import com.antheminc.oss.nimbus.test.scenarios.s0.view.VPSampleViewPageGreen.ConvertedNestedEntity;
import com.antheminc.oss.nimbus.test.scenarios.s0.view.VPSampleViewPageRed.Form_ConvertedNestedEntity;

/**
 * @author Sandeep Mantha
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataImportFunctionHandlerTest extends AbstractFrameworkIngerationPersistableTests {

	@Test
	public void t01_excelConverter() {
		Long refId = createOrGetDomainRoot_RefId();
		MockHttpServletRequest fnReq = MockHttpRequestBuilder.withUri(VIEW_PARAM_ROOT).addRefId(refId)
					.addNested("/page_aqua/fromExcel")
					.addAction(Action._get)
					.getMock();
		
		Object fnResp = controller.handleGet(fnReq, null);
		assertNotNull(fnResp);
		
	}
	
}
