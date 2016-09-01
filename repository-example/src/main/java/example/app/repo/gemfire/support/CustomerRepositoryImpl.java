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

package example.app.repo.gemfire.support;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import example.app.function.executions.CustomerFunctionExecutions;
import example.app.model.Customer;
import example.app.repo.gemfire.CustomerRepository;
import example.app.repo.gemfire.CustomerRepositoryExtension;

/**
 * The CustomerRepositoryImpl class is a {@link CustomerRepository} extension implementation
 * to support GemFire OQL JOINS on 2 or more collocated PARTITION Regions.
 *
 * @author John Blum
 * @see CustomerRepositoryExtension
 * @since 1.0.0
 */
@Component
@SuppressWarnings("unused")
public class CustomerRepositoryImpl implements CustomerRepositoryExtension {

	@Autowired
	private CustomerFunctionExecutions customerFunctionExecutions;

	@Override
	public List<Customer> findAllCustomersWithContactInformation() {
		return toCustomerList(customerFunctionExecutions.findAllCustomersWithContactInformation());
	}

	@SuppressWarnings("unchecked")
	protected List<Customer> toCustomerList(List<?> list) {
		Assert.notNull(list, "List cannot be null");

		if (list.size() != 1) {
			throw new IncorrectResultSizeDataAccessException(1, list.size());
		}

		Assert.isTrue(list.get(0) instanceof List, "Expected a List of Lists");

		return (List<Customer>) list.get(0);
	}
}
