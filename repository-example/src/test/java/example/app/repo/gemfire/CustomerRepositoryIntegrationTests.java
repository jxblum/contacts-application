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

package example.app.repo.gemfire;

import static example.app.model.Address.newAddress;
import static example.app.model.Contact.newContact;
import static example.app.model.Customer.newCustomer;
import static example.app.model.PhoneNumber.newPhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.ApplicationConfiguration;
import example.app.function.executions.CustomerFunctionExecutions;
import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.State;

/**
 * Test suite of test cases testing the contract and functionality of the {@link CustomerRepository} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see example.app.repo.gemfire.CustomerRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class CustomerRepositoryIntegrationTests {

	protected static final AtomicLong ID_GENERATOR = new AtomicLong(0L);

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private CustomerFunctionExecutions customerFunctionExecutions;

	@Autowired
	private CustomerRepository customerRepository;

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	protected String newAccountNumber() {
		return UUID.randomUUID().toString();
	}

	protected long newId() {
		return ID_GENERATOR.incrementAndGet();
	}

	@After
	public void tearDown() {
		contactRepository.deleteAll();
		customerRepository.deleteAll();
	}

	@Test
	public void findCustomerForAccountIsSuccessful() {
		Customer jonDoe = newCustomer("Jon", "Doe").with(newAccountNumber()).identifiedBy(newId());
		Customer janeDoe = newCustomer("Jane", "Doe").with(newAccountNumber()).identifiedBy(newId());

		customerRepository.save(jonDoe);
		customerRepository.save(janeDoe);

		Customer jonDoeByAccountNumber = customerRepository.findByAccountNumber(jonDoe.getAccountNumber());

		assertThat(jonDoeByAccountNumber).isEqualTo(jonDoe);
	}

	/**
	 * @see <a href="http://gemfire.docs.pivotal.io/docs-gemfire/latest/developing/query_additional/partitioned_region_query_restrictions.html#concept_5353476380D44CC1A7F586E5AE1CE7E8">Partition Region Query Restrictions</a>
	 */
	@Test
	public void findAllCustomersWithContactsIsSuccessful() {
		Customer jonDoe = newCustomer("Jon","Doe").with(newAccountNumber()).identifiedBy(newId());
		Customer janeDoe = newCustomer("Jane", "Doe").with(newAccountNumber()).identifiedBy(newId());
		Customer jackHandy = newCustomer("Jack", "Handy").with(newAccountNumber()).identifiedBy(newId());

		contactRepository.save(newContact(customerRepository.save(jonDoe), "jonDoe@home.com")
			.with(newPhoneNumber("503", "555", "1234")).<Contact>identifiedBy(newId()));

		customerRepository.save(janeDoe);

		contactRepository.save(newContact(customerRepository.save(jackHandy), "jackHandy@office.com")
			.with(newAddress("100 Main St.", "Portland", State.OREGON, "97205")).<Contact>identifiedBy(newId()));

		List<Customer> customers = customerRepository.findAllCustomersWithContactInformation();

		assertThat(customers).isNotNull();
		assertThat(customers.size()).isEqualTo(2);
		assertThat(customers).containsAll(Arrays.asList(jonDoe, jackHandy));
	}

	@Test
	public void findAllCustomersWithContactsUsingFunctionIsSuccessful() {
		Customer jonDoe = newCustomer("Jon","Doe").with(newAccountNumber()).identifiedBy(newId());
		Customer janeDoe = newCustomer("Jane", "Doe").with(newAccountNumber()).identifiedBy(newId());
		Customer jackHandy = newCustomer("Jack", "Handy").with(newAccountNumber()).identifiedBy(newId());

		contactRepository.save(newContact(customerRepository.save(jonDoe), "jonDoe@home.com")
			.with(newPhoneNumber("503", "555", "1234")).<Contact>identifiedBy(newId()));

		customerRepository.save(janeDoe);

		contactRepository.save(newContact(customerRepository.save(jackHandy), "jackHandy@office.com")
			.with(newAddress("100 Main St.", "Portland", State.OREGON, "97205")).<Contact>identifiedBy(newId()));

		List<?> results = customerFunctionExecutions.findAllCustomersWithContactInformation();

		List<Customer> customers = toCustomerList(results);

		assertThat(customers).isNotNull();
		assertThat(customers.size()).isEqualTo(2);
		assertThat(customers).containsAll(Arrays.asList(jonDoe, jackHandy));
	}

	@SuppressWarnings("unchecked")
	protected List<Customer> toCustomerList(List<?> list) {
		assertThat(list.size()).isEqualTo(1);
		assertThat(list.get(0)).isInstanceOf(List.class);
		return (List<Customer>) list.get(0);
	}
}
