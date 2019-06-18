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

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.channel.web.WebCommandBuilder;
import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.domain.session.SessionProvider;
import com.antheminc.oss.nimbus.entity.client.user.ClientUser;

/**
 * @author Sandeep Mantha
 */
public class TenantFilter extends OncePerRequestFilter {

	public static final String URI_PATTERN_P = "/**/p/**";
	
	@Value("${nimbus.multitenancy.urls}")
	private String[] urlPatternsToSkipFilter ;

	@Value("${nimbus.multitenancy.enabled}")
	private boolean multiTenancyEnabled ;
	
	private final SessionProvider sessionProvider;
	
	private TenantRepository tenantRepository;
	
	private WebCommandBuilder builder;
	
	private final AntPathMatcher pathMatcher;
	
	public TenantFilter(BeanResolverStrategy beanResolver) {
		this.sessionProvider = beanResolver.get(SessionProvider.class);
		this.tenantRepository = beanResolver.get(TenantRepository.class);
		this.builder = beanResolver.get(WebCommandBuilder.class);
		this.pathMatcher = new AntPathMatcher();
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if(multiTenancyEnabled) {
			ClientUser user = (ClientUser) sessionProvider.getLoggedInUser();
		    if(user == null) {
	        	throw new FrameworkRuntimeException("Tenant/user information is missing. Please contact a system administrator.");
		    }			
		    if(!isAuthorized(user, request)) {
		    	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        	throw new FrameworkRuntimeException("Request is not authorized as the tenant information is not valid. Please contact a system administrator.");
		    }
		}
	    filterChain.doFilter(request, response);		
	}
	
	protected boolean isAuthorized(ClientUser user, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
	    Stream<Cookie> stream = Objects.nonNull(cookies) ? Arrays.stream(cookies) : Stream.empty();
	    String cookieValue = stream.filter(cookie -> Constants.ACTIVE_TENANT_COOKIE.code.equals(cookie.getName()))
	        .findFirst()
	        .orElse(new Cookie(Constants.ACTIVE_TENANT_COOKIE.code, null))
	        .getValue();
	    //check user has access to the tenant passed in the cookie
	    if(StringUtils.isBlank(cookieValue)) {
	    	return false;
	    }
	    Tenant tenant = this.tenantRepository.findOneMatchingPattern(cookieValue);
	    if(tenant == null) {
	    	return false;
	    }
	    Command cmd = this.builder.build(request);
	    String tenantPrefixInSession = this.sessionProvider.getAttribute(Constants.ACTIVE_TENANT_COOKIE.code);
		Long match = user.getTenantIds().stream().filter(tenant.getId()::equals).findAny().orElse(null);
		
		if(match == null || (!StringUtils.equals(cookieValue, tenantPrefixInSession)) || !StringUtils.equals(cmd.getTenantUri(), tenantPrefixInSession)) {
			return false;
		}
		return true;
	}
	
	@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !this.pathMatcher.match(URI_PATTERN_P,request.getRequestURI());
	}

}
