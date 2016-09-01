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
import static example.app.model.Person.newPerson;
import static example.app.model.PhoneNumber.newPhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.ApplicationConfiguration;
import example.app.model.Contact;
import example.app.model.Gender;
import example.app.model.State;

/**
 * Test suite of test cases testing the contract and functionality of the {@link ContactRepository} DAO interface.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see example.app.repo.gemfire.ContactRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class ContactRepositoryIntegrationTests {

	protected static final AtomicLong ID_GENERATOR = new AtomicLong(0L);

	@Autowired
	private ContactRepository contactRepository;

	protected Long newId() {
		return ID_GENERATOR.incrementAndGet();
	}

	protected Sort newSort(String... properties) {
		return new Sort(properties);
	}

	protected Contact save(Contact contact) {
		if (contact.isNew()) {
			contact.setId(newId());
		}

		return contactRepository.save(contact);
	}

	@After
	public void tearDown() {
		contactRepository.deleteAll();
	}

	@Test
	public void saveFindAndDeleteIsSuccessful() {
		Contact savedJonDoe = newContact(newPerson("Jon", "Doe"), "jonDoe@home.com")
			.with(newAddress("100 Main St.", "Portland", State.OREGON, "12345"))
			.with(newPhoneNumber("503", "555", "1234"))
			.identifiedBy(newId());

		contactRepository.save(savedJonDoe);

		Contact loadedJonDoe = contactRepository.findOne(savedJonDoe.getId());

		assertThat(loadedJonDoe).isEqualTo(savedJonDoe);

		contactRepository.delete(loadedJonDoe);

		assertThat(contactRepository.count()).isEqualTo(0);
	}

	@Test
	public void findByAddressCityAndState() {
		Contact joeDirt = save(newContact(newPerson("Joe", "Dirt"), newAddress("100 Main St.", "Eugene", State.OREGON, "54321")));
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe"), newAddress("100 Main St.", "Portland", State.OREGON, "97205")));
		Contact benDover = save(newContact(newPerson("Ben", "Dover"), newAddress("100 Main St.", "San Francisco", State.CALIFORNIA, "9876")));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), newAddress("100 Main St.", "Portland", State.MAINE, "12345")));

		List<Contact> contacts = contactRepository.findByAddressCityAndAddressState("Portland", State.OREGON);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jonDoe));

		contacts = contactRepository.findByAddressCityAndAddressState("Portland", State.MAINE);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jackHandy));

		contacts = contactRepository.findByAddressCityAndAddressState("Eugene", State.OREGON);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(joeDirt));

		contacts = contactRepository.findByAddressCityAndAddressState("San Francisco", State.CALIFORNIA);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(benDover));

		contacts = contactRepository.findByAddressCityAndAddressState("Hollywood", State.CALIFORNIA);

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();

		contacts = contactRepository.findByAddressCityAndAddressState("Hollywood", State.NORTH_DAKOTA);

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();
	}

	@Test
	public void findByEmail() {
		Contact jonDoe = save(newContact(newPerson("Jon","Doe"), "jonDoe@gmail.com"));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), "jackHandy@yahoo.com"));

		Contact contact = contactRepository.findByEmail(jonDoe.getEmail());

		assertThat(contact).isEqualTo(jonDoe);

		contact = contactRepository.findByEmail(jackHandy.getEmail());

		assertThat(contact).isEqualTo(jackHandy);
	}

	@Test
	public void findByEmailLike() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe"), "jonDoe@gmail.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe"), "janeDoe@gmail.com"));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), "jackHandy@yahoo.com"));

		List<Contact> contacts = contactRepository.findByEmailLike("%@gmail.com");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(2);
		assertThat(contacts).containsAll(Arrays.asList(jonDoe, janeDoe));

		contacts = contactRepository.findByEmailLike("%@yahoo.com");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jackHandy));

		contacts = contactRepository.findByEmailLike("%@aol.com");

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();
	}

	@Test
	public void findByPersonAgeGreaterThanEqualTwentyOne() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe").age(42), "jonDoe@work.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe").age(36), "janeDoe@home.com"));
		Contact cookieDoe = save(newContact(newPerson("Cookie", "Doe").age(4), "cookieDoe@home.com"));
		Contact pieDoe = save(newContact(newPerson("Pie", "Doe").age(16), "pieDoe@school.com"));
		Contact sourDoe = save(newContact(newPerson("Sour", "Doe").age(21), "sourDoe@college.com"));
		Contact froDoe = save(newContact(newPerson("Fro", "Doe").age(22), "froDoe@home.com"));
		Contact hoeDoe = save(newContact(newPerson("Hoe", "Doe").age(30), "hoeDoe@office.com"));
		Contact joeDoe = save(newContact(newPerson("Joe", "Doe").age(59), "joeDoe@beach.com"));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy").age(17), "jackHandy@home.com"));
		Contact sandyHandy = save(newContact(newPerson("Sandy", "Handy").age(9), "jackHandy@home.com"));
		Contact joeDirt = save(newContact(newPerson("Joe", "Dirt").age(29), "joeDirt@bar.com"));
		Contact jackBlack = save(newContact(newPerson("Jack", "Black").age(33), "jackBlack@home.com"));

		List<Contact> contacts = contactRepository.findByPersonAgeGreaterThanEqualOrderByPersonLastNameAscPersonAgeDesc(21);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(8);
		assertThat(contacts).isEqualTo(Arrays.asList(jackBlack, joeDirt, joeDoe, jonDoe, janeDoe, hoeDoe, froDoe, sourDoe));
	}

	@Test
	public void findByPersonGender() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe").as(Gender.MALE), "jonDoe@office.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe").as(Gender.FEMALE), "janeDoe@home.com"));
		Contact pieDoe = save(newContact(newPerson("Pie", "Doe").as(Gender.FEMALE), "pieDoe@school.com"));

		List<Contact> contacts = contactRepository.findByPersonGender(Gender.FEMALE);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(2);
		assertThat(contacts).containsAll(Arrays.asList(janeDoe, pieDoe));

		contacts = contactRepository.findByPersonGender(Gender.MALE);

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jonDoe));
	}

	@Test
	public void findByPersonFirstAndLastNameIgnoringCase() {
		Contact jonDoeOne = save(newContact(newPerson("Jon", "Doe"), "jonDoe@home.com"));
		Contact jonDoeTwo = save(newContact(newPerson("Jon", "Doe"), "jonDoe@home.com"));
		Contact jonBloom = save(newContact(newPerson("Jon", "Bloom"), "jonBloom@home.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe"), "janeDoe@home.com"));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), "jackHandy@home.com"));

		List<Contact> contacts = contactRepository.findByPersonFirstNameIgnoreCaseAndPersonLastNameIgnoreCase(
			"Jon", "Doe");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(2);
		assertThat(contacts).containsAll(Arrays.asList(jonDoeOne, jonDoeTwo));

		contacts = contactRepository.findByPersonFirstNameIgnoreCaseAndPersonLastNameIgnoreCase("jon", "bloom");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jonBloom));

		contacts = contactRepository.findByPersonFirstNameIgnoreCaseAndPersonLastNameIgnoreCase("JANE", "DOE");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(janeDoe));

		contacts = contactRepository.findByPersonFirstNameIgnoreCaseAndPersonLastNameIgnoreCase("JacK", "HANDy");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jackHandy));

		contacts = contactRepository.findByPersonFirstNameIgnoreCaseAndPersonLastNameIgnoreCase("Jack", "Black");

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();
	}

	@Test
	public void findById() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe").identifiedBy(newId()), "jonDoe@home.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe").identifiedBy(newId()), "janeDoe@home.com"));

		Contact contact = contactRepository.findByPersonId(jonDoe.getPerson().getId());

		assertThat(contact).isEqualTo(jonDoe);

		contact = contactRepository.findByPersonId(janeDoe.getPerson().getId());

		assertThat(contact).isEqualTo(janeDoe);

		contact = contactRepository.findByPersonId(System.currentTimeMillis());

		assertThat(contact).isNull();
	}

	@Test
	public void findByPersonLastName() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe"), "jonDoe@home.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe"), "janeDoe@home.com"));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), "jackHandy@home.com"));

		List<Contact> contacts = contactRepository.findByPersonLastName("Doe");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(2);
		assertThat(contacts).containsAll(Arrays.asList(jonDoe, janeDoe));

		contacts = contactRepository.findByPersonLastName("Handy");

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jackHandy));

		contacts = contactRepository.findByPersonLastName("Smith");

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();
	}

	@Test
	public void findByPersonLastNameLike() {
		Contact joeDirt = save(newContact(newPerson("Joe", "Dirt"), "joeDirt@office.com"));
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe"), "jonDoe@home.com"));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), "jackHandy@bar.com"));

		List<Contact> contacts = contactRepository.findByPersonLastNameLike("D%", newSort("person.lastName"));

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(2);
		assertThat(contacts).isEqualTo(Arrays.asList(joeDirt, jonDoe));

		contacts = contactRepository.findByPersonLastNameLike("%and%", newSort("person.lastName"));

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jackHandy));

		contacts = contactRepository.findByPersonLastNameLike("Smi%", newSort("person.lastName"));

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();
	}

	@Test
	public void findByLastNameLikeLimitedToFiveResults() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe"), "jonDoe@home.com"));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe"), "janeDoe@home.com"));
		Contact cookieDoe = save(newContact(newPerson("Cookie", "Doe"), "cookieDoe@home.com"));
		Contact pieDoe = save(newContact(newPerson("Pie", "Doe"), "pieDoe@home.com"));
		Contact sourDoe = save(newContact(newPerson("Sour", "Doe"), "sourDoe@home.com"));
		Contact moeDoe = save(newContact(newPerson("Moe", "Doe"), "moeDoe@home.com"));
		Contact joeDoe = save(newContact(newPerson("Joe", "Doe"), "joeDoe@home.com"));
		Contact hoeDoe = save(newContact(newPerson("Hoe", "Doe"), "hoeDoe@home.com"));
		Contact froDoe = save(newContact(newPerson("Fro", "Doe"), "froDoe@home.com"));

		List<Contact> expectedContacts = Arrays.asList(cookieDoe, froDoe, hoeDoe, janeDoe, joeDoe);

		List<Contact> actualContacts = contactRepository.findByPersonLastNameLike("Doe%", newSort("person.firstName"));

		assertThat(actualContacts).isNotNull();
		assertThat(actualContacts.size()).isEqualTo(5);
		assertThat(actualContacts).describedAs("Expected [%1$s];%n%t but was [%2$s]", expectedContacts, actualContacts)
			.isEqualTo(expectedContacts);
	}

	@Test
	public void findByPhoneNumber() {
		Contact jonDoe = save(newContact(newPerson("Jon", "Doe"), newPhoneNumber("503", "555", "1234")));
		Contact janeDoe = save(newContact(newPerson("Jane", "Doe"), newPhoneNumber("503", "555", "1234")));
		Contact jackHandy = save(newContact(newPerson("Jack", "Handy"), newPhoneNumber("971", "555", "1234")));

		List<Contact> contacts = contactRepository.findByPhoneNumber(newPhoneNumber("503", "555", "1234"));

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(2);
		assertThat(contacts).containsAll(Arrays.asList(jonDoe, janeDoe));

		contacts = contactRepository.findByPhoneNumber(newPhoneNumber("971", "555", "1234"));

		assertThat(contacts).isNotNull();
		assertThat(contacts.size()).isEqualTo(1);
		assertThat(contacts).containsAll(Collections.singletonList(jackHandy));

		contacts = contactRepository.findByPhoneNumber(newPhoneNumber("503", "555", "9876"));

		assertThat(contacts).isNotNull();
		assertThat(contacts).isEmpty();
	}
}
