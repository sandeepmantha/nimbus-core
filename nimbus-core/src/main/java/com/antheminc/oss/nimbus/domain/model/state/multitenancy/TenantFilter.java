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
import org.springframework.web.filter.OncePerRequestFilter;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.domain.session.SessionProvider;
import com.antheminc.oss.nimbus.entity.client.user.ClientUser;

/**
 * @author Sandeep Mantha
 */
public class TenantFilter extends OncePerRequestFilter {

	@Value("${nimbus.multitenancy.urls}")
	private String[] urlPatternsToSkipFilter ;

	@Value("${nimbus.multitenancy.enabled}")
	private boolean multiTenancyEnabled ;
	
	private final SessionProvider sessionProvider;
	
	public TenantFilter(BeanResolverStrategy beanResolver) {
		this.sessionProvider = beanResolver.get(SessionProvider.class);
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if(multiTenancyEnabled) {
		    Cookie[] cookies = request.getCookies();
		    Stream<Cookie> stream = Objects.nonNull(cookies) ? Arrays.stream(cookies) : Stream.empty();
		    String cookieValue = stream.filter(cookie -> Constants.ACTIVE_TENANT_COOKIE.code.equals(cookie.getName()))
		        .findFirst()
		        .orElse(new Cookie(Constants.ACTIVE_TENANT_COOKIE.code, null))
		        .getValue();
			ClientUser user = (ClientUser) sessionProvider.getLoggedInUser();
		    if(user == null) {
	        	throw new FrameworkRuntimeException("User is not authorized. Please contact a system administrator.");
		    }
		    if(StringUtils.isBlank(cookieValue) || (user != null && user.getTenant() != null && !StringUtils.equals(cookieValue, user.getTenant().getPrefix()))) {
		    	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        	throw new FrameworkRuntimeException("Request is missing tenant information and is not allowed to access the system. Please contact a system administrator.");
		    }
		}
	    filterChain.doFilter(request, response);		
	}
	
	@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return Arrays.stream(urlPatternsToSkipFilter).anyMatch(entry -> path.endsWith(entry));
	}

}
