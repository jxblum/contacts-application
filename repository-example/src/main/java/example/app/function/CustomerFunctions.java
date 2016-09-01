/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.function;

import java.util.Collections;
import java.util.List;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import example.app.model.Customer;
import example.app.repo.gemfire.CustomerRepository;

/**
 * The CustomerFunctions class is a POJO containing various methods defining GemFire Functions
 * to process {@link Customer} data.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.function.annotation.GemfireFunction
 * @see com.gemstone.gemfire.cache.execute.RegionFunctionContext
 * @see com.gemstone.gemfire.cache.query.QueryService
 * @see CustomerRepository
 * @see <a href="http://gemfire.docs.pivotal.io/docs-gemfire/latest/developing/partitioned_regions/join_query_partitioned_regions.html">Equi-Join Query on collocated PRs</a>
 * @see <a href="http://geode.docs.pivotal.io/docs/developing/partitioned_regions/join_query_partitioned_regions.html">Equi-Join Query on collocated PRs</a>
 * @see <a href="http://gemfire.docs.pivotal.io/docs-gemfire/latest/developing/querying_basics/restrictions_and_unsupported_features.html">Query Language Restrictions and Unsupported Features</a>
 * @see <a href="http://gemfire.docs.pivotal.io/docs-gemfire/latest/developing/query_additional/partitioned_region_query_restrictions.html#concept_5353476380D44CC1A7F586E5AE1CE7E8">Partition Region Query Restrictions</a>
 * @since 1.0.0
 */
@Component
@SuppressWarnings("unused")
public class CustomerFunctions {

	protected static final String CUSTOMERS_WITH_CONTACTS_QUERY = ""
		+ "SELECT DISTINCT customer FROM /Customers customer, /Contacts contact "
		+ "WHERE customer.firstName = contact.person.firstName "
		+ "AND customer.lastName = contact.person.lastName";

	@Autowired
	private GemfireMappingContext mappingContext;

	@GemfireFunction
	public List<Customer> findAllCustomersWithContactInformation(FunctionContext functionContext) {
		return executeQueryInFunctionContext(toRegionFunctionContext(functionContext));
		//return executeQueryWithRepository(toRegionFunctionContext(functionContext));
	}

	protected List<Customer> executeQueryInFunctionContext(FunctionContext functionContext) {
		return executeQueryInFunctionContext(toRegionFunctionContext(functionContext));
	}

	@SuppressWarnings("unchecked")
	protected List<Customer> executeQueryInFunctionContext(RegionFunctionContext functionContext) {
		try {
			QueryService queryService = getQueryService(functionContext);
			Query query = queryService.newQuery(CUSTOMERS_WITH_CONTACTS_QUERY);
			Object results = query.execute(functionContext);

			Assert.isInstanceOf(SelectResults.class, results);

			return ((SelectResults<Customer>) results).asList();
		}
		catch (Exception e) {
			throw new DataRetrievalFailureException("Failed to find Customers with Contact information", e);
		}
	}

	protected List<Customer> executeQueryWithRepository(FunctionContext functionContext) {
		return executeQueryWithRepository(toRegionFunctionContext(functionContext));
	}

	protected List<Customer> executeQueryWithRepository(RegionFunctionContext functionContext) {
		return newCustomerRepository(getRegion(functionContext)).findAllCustomersWithContactInformation();
	}

	protected QueryService getQueryService(RegionFunctionContext functionContext) {
		return getRegion(functionContext).getRegionService().getQueryService();
	}

	protected <K, V> Region<K, V> getRegion(RegionFunctionContext functionContext) {
		return PartitionRegionHelper.getLocalDataForContext(functionContext);
	}

	protected CustomerRepository newCustomerRepository(Region<Long, Customer> customers) {
		return new GemfireRepositoryFactory(Collections.singleton(customers), mappingContext)
			.getRepository(CustomerRepository.class, null);
	}

	protected RegionFunctionContext toRegionFunctionContext(FunctionContext functionContext) {
		Assert.isInstanceOf(RegionFunctionContext.class, functionContext);
		return (RegionFunctionContext) functionContext;
	}
}
