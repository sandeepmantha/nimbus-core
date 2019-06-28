
package com.antheminc.oss.nimbus.domain.model.state.multitenancy;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import javax.servlet.http.Cookie;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.domain.cmd.Action;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.domain.session.SessionProvider;
import com.antheminc.oss.nimbus.entity.client.user.ClientUser;
import com.antheminc.oss.nimbus.test.domain.support.AbstractFrameworkIntegrationTests;
import com.antheminc.oss.nimbus.test.domain.support.utils.MockHttpRequestBuilder;

/**
 * @author Sandeep Mantha
 */
@AutoConfigureMockMvc
@TestPropertySource(properties = { 
		"nimbus.multitenancy.enabled=true",
		"nimbus.multitenancy.tenants.1.description=ABC",
		"nimbus.multitenancy.tenants.1.prefix=/a/1/b",
		"nimbus.multitenancy.tenants.2.description=DEF",
		"nimbus.multitenancy.tenants.2.prefix=/a/2/b",
})
public class TenantFilterTest extends AbstractFrameworkIntegrationTests {

	@Autowired
	private SessionProvider sessionProvider;

	protected static final String TENANT_1_PREFIX = "/a/1/b";
	protected static final String TENANT_2_PREFIX = "/a/2/b";

	protected static final String VIEW_PARAM_ROOT = TENANT_1_PREFIX + "/p/sample_view";

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private TenantFilter tenantFilter;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(this.tenantFilter).build();
	}

	@Test
	public void testFilter() throws Exception {

		ClientUser clientUser = new ClientUser();
		clientUser.setTenantIds(new HashSet<>());
		clientUser.getTenantIds().add(1L);
		clientUser.getTenantIds().add(2L);
		sessionProvider.setLoggedInUser(clientUser);
		sessionProvider.setAttribute(Constants.ACTIVE_TENANT_COOKIE.code, TENANT_1_PREFIX);
		MockHttpServletRequest req = MockHttpRequestBuilder.withUri(VIEW_PARAM_ROOT).addAction(Action._new).getMock();
		ResultActions res = mvc.perform(post(req.getRequestURI()).content("{}")
				.cookie(new Cookie(Constants.ACTIVE_TENANT_COOKIE.code, TENANT_1_PREFIX)).with(csrf())
				.contentType(APPLICATION_JSON_UTF8));

		res.andExpect(status().isOk());

		sessionProvider.setAttribute(Constants.ACTIVE_TENANT_COOKIE.code, TENANT_2_PREFIX);
		MockHttpServletRequest req2 = MockHttpRequestBuilder.withUri(VIEW_PARAM_ROOT).addAction(Action._new).getMock();
		try {
			// cookie is different in the request
			mvc.perform(post(req2.getRequestURI()).content("{}").with(csrf()).contentType(APPLICATION_JSON_UTF8)
					.cookie(new Cookie(Constants.ACTIVE_TENANT_COOKIE.code, TENANT_1_PREFIX)));
		} catch (FrameworkRuntimeException e) {
			assertEquals(e.getExecuteError().getMessage(),
					"Request is not authorized as the tenant information is not valid. Please contact a system administrator.");
		}

		sessionProvider.setAttribute(Constants.ACTIVE_TENANT_COOKIE.code, TENANT_2_PREFIX);
		MockHttpServletRequest req3 = MockHttpRequestBuilder.withUri(VIEW_PARAM_ROOT).addAction(Action._new).getMock();
		try {
			// prefix to /p (tenantprefix) is not valid with the session value
			mvc.perform(post(req3.getRequestURI()).content("{}").with(csrf()).contentType(APPLICATION_JSON_UTF8)
					.cookie(new Cookie(Constants.ACTIVE_TENANT_COOKIE.code, TENANT_2_PREFIX)));
		} catch (FrameworkRuntimeException e) {
			assertEquals(e.getExecuteError().getMessage(),
					"Request is not authorized as the tenant information is not valid. Please contact a system administrator.");
		}

	}

	@Test
	public void testCreate() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testRead() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testUpdate() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testDelete() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testCreateUnallowed() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testReadUnallowed() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testUpdateUnallowed() {
		Assert.fail("Implement me!");
	}
	
	@Test
	public void testDeleteUnallowed() {
		Assert.fail("Implement me!");
	}
}
