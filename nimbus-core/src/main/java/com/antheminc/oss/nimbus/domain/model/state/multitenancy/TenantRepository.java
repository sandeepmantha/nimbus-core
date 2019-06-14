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

import java.util.Set;

/**
 * <p>A repository for providing tenant objects.
 * 
 * @author Tony Lopez
 * @since 2.0
 *
 */
public interface TenantRepository {

	/**
	 * <p>Find a list of {@link Tenant} objects that have a clientCode
	 * equivalent to {@code clientId}.
	 * @param clientId the {@code clientId} to match against
	 * @return the list of {@link Tenant} objects
	 */
	Set<Tenant> findByClientId(String clientId);

	/**
	 * <p>Find a {@link Tenant} by it's unique id.
	 * @param id the of the {@link Tenant} to find
	 * @return the {@link Tenant} object
	 */
	Tenant findById(Long id);
	
	/**
	 * <p>Find a list of {@link Tenant} by all unique ids.
	 * @param ids the set of id's for {@link Tenant} objects to find
	 * @return the {@link Tenant} object
	 */
	Set<Tenant> findByIds(Set<Long> ids);

	/**
	 * <p>Find a {@link Tenant} object where the provided {@code value} matches
	 * the tenant object's pattern.
	 * @param value the value to match against all known {@link Tenant} object's
	 *            pattern.
	 * @return the {@link Tenant} object
	 */
	Tenant findOneMatchingPattern(String value);
}
