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

package example.app.service;

import static example.app.model.Address.newAddress;
import static example.app.model.Contact.newContact;
import static example.app.model.Person.newPerson;
import static example.app.model.PhoneNumber.newPhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.GlobalTransactionApplicationConfiguration;
import example.app.model.Contact;
import example.app.model.Person;
import example.app.model.PhoneNumber;
import example.app.model.State;

/**
 * The ContactsServiceJtaTransactionIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
	classes = GlobalTransactionApplicationConfiguration.class)
@SuppressWarnings("all")
public class ContactsServiceJtaTransactionIntegrationTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Autowired
	private example.app.repo.gemfire.ContactRepository gemfireContactRepository;

	@Autowired
	private example.app.repo.jpa.ContactRepository jpaContactRepository;

	@Autowired
	private ContactsService contactsService;

	@After
	public void tearDown() {
		gemfireContactRepository.deleteAll();
		jpaContactRepository.deleteAll();
	}

	@Test
	public void transactionWithValidContactCommits() {
		assertThat(gemfireContactRepository.count()).isEqualTo(0);
		assertThat(jpaContactRepository.count()).isEqualTo(0);

		Contact jonDoe = newContact(newPerson("Jon", "Doe"), "jonDoe@home.com")
			.with(newAddress("100 Main St.", "Portland", State.OREGON, "97205"))
			.with(newPhoneNumber("503", "541", "1234"));

		contactsService.save(jonDoe);

		assertThat(gemfireContactRepository.count()).isEqualTo(1);
		assertThat(jpaContactRepository.count()).isEqualTo(1);
	}

	@Test
	public void transactionWithInvalidContactRollsBack() {
		assertThat(gemfireContactRepository.count()).isEqualTo(0);
		assertThat(jpaContactRepository.count()).isEqualTo(0);

		Person jackHandy = newPerson("Jack", "Handy");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");

		Contact contact = newContact(jackHandy, "jackHandy@work.com")
			.with(newAddress("1 Park Way", "Portland", State.OREGON, "97205"))
			.with(phoneNumber);

		try {
			exception.expect(IllegalArgumentException.class);
			exception.expectCause(is(nullValue(Throwable.class)));
			exception.expectMessage(String.format(
				"Phone Number [%1$s] in Contact for Person [%2$s] is not valid; '555' is not valid phone number exchange",
					phoneNumber, jackHandy));

			contactsService.save(contact);
		}
		finally {
			assertThat(jpaContactRepository.count()).isEqualTo(0);
			assertThat(gemfireContactRepository.count()).isEqualTo(0);
		}
	}
}
