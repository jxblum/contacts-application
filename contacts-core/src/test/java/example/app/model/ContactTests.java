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

package example.app.model;

import static example.app.model.Address.newAddress;
import static example.app.model.Contact.newContact;
import static example.app.model.Person.newPerson;
import static example.app.model.PhoneNumber.newPhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite of test cases testing the contract and functionality of the {@link Contact} class.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see example.app.model.Contact
 * @see example.app.model.Person
 * @see example.app.model.PhoneNumber
 * @since 1.0.0
 */
public class ContactTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void newContactWithPersonAndAddress() {
		Person jonDoe = newPerson("Jon", "Doe");
		Address address = newAddress("100 Main St.", "Portland", State.OREGON, "97205");

		Contact contact = newContact(jonDoe, address);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isTrue();
		assertThat(contact.getAddress()).isEqualTo(address);
		assertThat(contact.hasEmail()).isFalse();
		assertThat(contact.getEmail()).isNull();
		assertThat(contact.hasPhoneNumber()).isFalse();
		assertThat(contact.getPhoneNumber()).isNull();
	}

	@Test
	public void newContactWithNullPersonAndAddress() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person is required");

		newContact(null, newAddress("100 Main St.", "Portland", State.OREGON, "97205"));
	}

	@Test
	public void newContactWithPersonAndNullAddress() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Address is required");

		newContact(newPerson("Jon", "Doe"), (Address) null);
	}

	@Test
	public void newContactWithPersonAndEmail() {
		Person jonDoe = newPerson("Jon", "Doe");

		Contact contact = newContact(jonDoe, "jonDoe@work.com");

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isFalse();
		assertThat(contact.getAddress()).isNull();
		assertThat(contact.hasEmail()).isTrue();
		assertThat(contact.getEmail()).isEqualTo("jonDoe@work.com");
		assertThat(contact.hasPhoneNumber()).isFalse();
		assertThat(contact.getPhoneNumber()).isNull();
	}

	@Test
	public void newContactWithNullPersonAndEmail() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person is required");

		newContact(null, "jonDoe@work.com");
	}

	@Test
	public void newContactWithPersonAndNullEmail() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Email is required");

		newContact(newPerson("Jon", "Doe"), (String) null);
	}

	@Test
	public void newContactWithPersonAndPhoneNumber() {
		Person jonDoe = newPerson("Jon", "Doe");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");

		Contact contact = newContact(jonDoe, phoneNumber);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isFalse();
		assertThat(contact.getAddress()).isNull();
		assertThat(contact.hasEmail()).isFalse();
		assertThat(contact.getEmail()).isNull();
		assertThat(contact.hasPhoneNumber()).isTrue();
		assertThat(contact.getPhoneNumber()).isEqualTo(phoneNumber);
	}

	@Test
	public void newContactWithNullPersonAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person is required");

		newContact(null, newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonAndNullPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("PhoneNumber is required");

		newContact(newPerson("Jon", "Doe"), (PhoneNumber) null);
	}

	@Test
	public void newContactWithPersonAddressAndPhoneNumber() {
		Person jonDoe = newPerson("Jon", "Doe");
		Address address = newAddress("100 Main St.", "Portland", State.OREGON, "97205");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");

		Contact contact = newContact(jonDoe, address, phoneNumber);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isTrue();
		assertThat(contact.getAddress()).isEqualTo(address);
		assertThat(contact.hasEmail()).isFalse();
		assertThat(contact.getEmail()).isNull();
		assertThat(contact.hasPhoneNumber()).isTrue();
		assertThat(contact.getPhoneNumber()).isEqualTo(phoneNumber);
	}

	@Test
	public void newContactWithNullPersonAddressAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person is required");

		newContact(null, newAddress("100 Main St.", "Portland", State.OREGON, "97205"),
			newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonNullAddressAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Address is required");

		newContact(newPerson("Jon", "Doe"), (Address) null, newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonAddressAndNullPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("PhoneNumber is required");

		newContact(newPerson("Jon", "Doe"), newAddress("100 Main St.", "Portland", State.OREGON, "97205"), null);
	}

	@Test
	public void newContactWithPersonEmailAndPhoneNumber() {
		Person jonDoe = newPerson("Jon", "Doe");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");

		Contact contact = newContact(jonDoe, "jonDoe@work.com", phoneNumber);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isFalse();
		assertThat(contact.getAddress()).isNull();
		assertThat(contact.hasEmail()).isTrue();
		assertThat(contact.getEmail()).isEqualTo("jonDoe@work.com");
		assertThat(contact.hasPhoneNumber()).isTrue();
		assertThat(contact.getPhoneNumber()).isEqualTo(phoneNumber);
	}

	@Test
	public void newContactWithNullPersonEmailAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person is required");

		newContact(null, "jonDoe@work.com", newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonNullEmailAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Email is required");

		newContact(newPerson("Jon", "Doe"), (String) null, newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonEmailAndNullPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("PhoneNumber is required");

		newContact(newPerson("Jon", "Doe"), "jonDoe@work.com", null);
	}

	@Test
	public void newContactWithPersonAddressEmailAndPhoneNumber() {
		Person jonDoe = newPerson("Jon", "Doe");
		Address address = newAddress("100 Main St.", "Portland", State.OREGON, "97205");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");

		Contact contact = newContact(jonDoe, address, "jonDoe@work.com", phoneNumber);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isTrue();
		assertThat(contact.getAddress()).isEqualTo(address);
		assertThat(contact.hasEmail()).isTrue();
		assertThat(contact.getEmail()).isEqualTo("jonDoe@work.com");
		assertThat(contact.hasPhoneNumber()).isTrue();
		assertThat(contact.getPhoneNumber()).isEqualTo(phoneNumber);
	}

	@Test
	public void newContactWithNullPersonAddressEmailAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person is required");

		newContact(null, newAddress("100 Main St.", "Portland", State.OREGON, "97205"),
			"jonDoe@work.com", newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonNullAddressEmailAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Address is required");

		newContact(newPerson("Jon", "Doe"), null, "jonDoe@work.com", newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonAddressNullEmailAndPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Email is required");

		newContact(newPerson("Jon", "Doe"), newAddress("100 Main St.", "Portland", State.OREGON, "97205"), null,
			newPhoneNumber("503", "555", "1234"));
	}

	@Test
	public void newContactWithPersonAddressEmailAndNullPhoneNumber() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("PhoneNumber is required");

		newContact(newPerson("Jon", "Doe"), newAddress("100 Main St.", "Portland", State.OREGON, "97205"),
			"jonDoe@work.com", null);
	}

	@Test
	public void setAndGetPersonIsSuccess() {
		Contact contact = new Contact();

		assertThat(contact.getPerson()).isNull();
		assertThat(contact.getAddress()).isNull();
		assertThat(contact.getEmail()).isNull();
		assertThat(contact.getPhoneNumber()).isNull();

		Person jonDoe = newPerson("Jon", "Doe");

		contact.setPerson(jonDoe);

		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.getAddress()).isNull();
		assertThat(contact.getEmail()).isNull();
		assertThat(contact.getPhoneNumber()).isNull();
	}

	@Test
	public void setPersonToNullThrowsIllegalArgumentException() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Person cannot be null");

		new Contact().setPerson(null);
	}

	@Test
	public void newContactSetPersonWithAddressWithEmailWithPhoneNumberIdentifiedBy() {
		Address address = newAddress("100 Main St.", "Portand", State.OREGON, "97205");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");
		Person jonDoe = newPerson("Jon", "Doe");

		Contact contact = new Contact().with(address).with("jonDoe@work.com").with(phoneNumber).identifiedBy(1L);

		assertThat(contact).isNotNull();

		contact.setPerson(jonDoe);

		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isTrue();
		assertThat(contact.getAddress()).isEqualTo(address);
		assertThat(contact.hasEmail()).isTrue();
		assertThat(contact.getEmail()).isEqualTo("jonDoe@work.com");
		assertThat(contact.hasPhoneNumber()).isTrue();
		assertThat(contact.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(contact.getId()).isEqualTo(1L);
	}
}
