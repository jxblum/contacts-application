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

package example.app.repo.jpa;

import static example.app.model.Address.newAddress;
import static example.app.model.Contact.newContact;
import static example.app.model.Customer.newCustomer;
import static example.app.model.PhoneNumber.newPhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.jpa.JpaConfiguration;
import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.Gender;
import example.app.model.State;

/**
 * Test suite of test case testing the contract and functionality of the {@link ContactRepository}
 * data access object (DAO) using JPA.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.config.jpa.JpaConfiguration
 * @see example.app.repo.jpa.ContactRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = JpaConfiguration.class)
@DirtiesContext
@SuppressWarnings("all")
public class ContactRepositoryIntegrationTests {

	@Autowired
	private ContactRepository contactRepository;

	protected String newAccountNumber() {
		return UUID.randomUUID().toString();
	}

	@Test
	public void saveFindAndDeleteIsSuccessful() {
		assertThat(contactRepository.count()).isEqualTo(0);

		Customer jonDoe = newCustomer("Jon", "Doe").with(newAccountNumber())
			.as(Gender.MALE).age(42);

		Contact expectedContact = newContact(jonDoe, "jonDoe@work.com")
			.with(newAddress("100 Main St.", "Portland", State.OREGON, "97205"))
			.with(newPhoneNumber("503", "541", "1234"));

		expectedContact = contactRepository.save(expectedContact);

		assertThat(contactRepository.count()).isEqualTo(1);
		assertThat(expectedContact.getId()).isNotNull();

		Contact actualContact = contactRepository.findOne(expectedContact.getId());

		assertThat(actualContact).isNotNull();
		assertThat(actualContact).isNotSameAs(expectedContact);
		assertThat(actualContact).isEqualTo(expectedContact);
	}
}
