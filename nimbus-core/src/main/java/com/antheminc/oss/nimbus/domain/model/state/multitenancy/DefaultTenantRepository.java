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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.AntPathMatcher;

import com.antheminc.oss.nimbus.domain.model.state.multitenancy.MultitenancyProperties.TenantDetail;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>A default {@link TenantRepository} implementation that reads tenant
 * information from application properties.
 * 
 * @author Tony Lopez
 * @since 2.0
 *
 */
@Getter
@Setter
public class DefaultTenantRepository implements TenantRepository {

	private final MultitenancyProperties multitenancyProperties;
	private final AntPathMatcher pathMatcher;

	public DefaultTenantRepository(MultitenancyProperties multitenancyProperties) {
		this.multitenancyProperties = multitenancyProperties;
		this.pathMatcher = new AntPathMatcher();
	}

	@Override
	public List<Tenant> findByClientId(String clientId) {
		if (null == clientId) {
			return new ArrayList<>();
		}

		List<Tenant> tenants = new ArrayList<>();
		List<TenantDetail> tenantDetails = this.multitenancyProperties.getTenants();
		for (int i = 0; i <= tenantDetails.size(); i++) {
			TenantDetail tenantDetail = tenantDetails.get(i);
			if (clientId.equals(tenantDetail.getClientId())) {
				tenants.add(this.toTenant(i, tenantDetail));
			}
		}
		return tenants;
	}

	@Override
	public Tenant findById(Long id) {
		if (null == id || CollectionUtils.isEmpty(this.multitenancyProperties.getTenants())
				|| id > this.multitenancyProperties.getTenants().size()) {
			return null;
		}
		int idInt = id.intValue();
		return this.toTenant(idInt, this.multitenancyProperties.getTenants().get(idInt));
	}

	@Override
	public Tenant findOneMatchingPattern(String value) {
		List<Tenant> tenants = new ArrayList<>();
		List<TenantDetail> tenantDetails = this.multitenancyProperties.getTenants();
		for (int i = 0; i < tenantDetails.size(); i++) {
			TenantDetail tenantDetail = tenantDetails.get(i);
			if (this.pathMatcher.match(tenantDetail.getPattern(), value)) {
				tenants.add(this.toTenant(i, tenantDetail));
			}
		}

		if (CollectionUtils.isEmpty(tenants)) {
			return null;
		}

		// TODO find best match
		return tenants.get(0);
	}

	private Tenant toTenant(int id, TenantDetail tenantDetail) {
		Tenant tenant = new Tenant();
		tenant.setId(new Long(id));
		tenant.setClientId(tenantDetail.getClientId());
		tenant.setPattern(tenantDetail.getPattern());
		return tenant;
	}
}
