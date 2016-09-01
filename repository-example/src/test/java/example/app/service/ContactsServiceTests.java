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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import example.app.model.Contact;
import example.app.model.Person;
import example.app.model.PhoneNumber;
import example.app.model.State;

/**
 * Test suite of test cases testing the contract and functionality of the {@link ContactsService} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see example.app.service.ContactsService
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactsServiceTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	private example.app.repo.gemfire.ContactRepository mockGemfireContactRepository;

	@Mock
	private example.app.repo.jpa.ContactRepository mockJpaContactRepository;

	private ContactsService contactsService;

	@Before
	public void setup() {
		contactsService = new ContactsService(mockGemfireContactRepository, mockJpaContactRepository);
	}

	@Test
	public void getInitializedGemFireContactRepositoryReturnsGemFireContactRepository() {
		assertThat(contactsService.getGemFireContactRepository()).isEqualTo(mockGemfireContactRepository);
	}

	@Test
	public void getUninitializedGemFireContactRepositoryThrowsIllegalStateException() {
		exception.expect(IllegalStateException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("GemFire ContactRepository was not properly initialized");

		new ContactsService(null, mockJpaContactRepository).getGemFireContactRepository();
	}

	@Test
	public void getInitializedJpaContactRepositoryReturnsJpaContactRepository() {
		assertThat(contactsService.getJpaContactRepository()).isEqualTo(mockJpaContactRepository);
	}

	@Test
	public void getUninitializedJpaContactRepositoryThrowsIllegalStateException() {
		exception.expect(IllegalStateException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("JPA ContactRepository was not properly initialized");

		new ContactsService(mockGemfireContactRepository, null).getJpaContactRepository();
	}

	@Test
	public void saveIsSuccessFul() {
		Person jonDoe = newPerson("Jon", "Doe");
		Contact contact = newContact(jonDoe, "jonDoe@work.com");

		when(mockGemfireContactRepository.save(any(Contact.class))).then(
			(InvocationOnMock invocationOnMock) -> invocationOnMock.getArgumentAt(0, Contact.class));

		when(mockJpaContactRepository.save(any(Contact.class))).then(
			(InvocationOnMock invocationOnMock) -> invocationOnMock.getArgumentAt(0, Contact.class));

		assertThat(contactsService.save(contact)).isEqualTo(contact);

		verify(mockGemfireContactRepository, times(1)).save(eq(contact));
		verify(mockJpaContactRepository, times(1)).save(eq(contact));
	}

	@Test
	public void saveNullThrowsIllegalArgumentException() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Contact cannot be null");

		try {
			contactsService.save(null);
		}
		finally {
			verify(mockGemfireContactRepository, never()).save(any(Contact.class));
			verify(mockJpaContactRepository, never()).save(any(Contact.class));
		}
	}
	@Test
	public void removeIsSuccessFul() {
		Person jonDoe = newPerson("Jon", "Doe");
		Contact contact = newContact(jonDoe, "jonDoe@work.com");

		contactsService.remove(contact);

		verify(mockGemfireContactRepository, times(1)).delete(eq(contact));
		verify(mockJpaContactRepository, times(1)).delete(eq(contact));
	}

	@Test
	public void removeNullThrowsIllegalArgumentException() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Contact cannot be null");

		try {
			contactsService.remove(null);
		}
		finally {
			verify(mockGemfireContactRepository, never()).delete(any(Contact.class));
			verify(mockJpaContactRepository, never()).delete(any(Contact.class));
		}
	}

	@Test
	public void validateAddressIsSuccessful() {
		Contact contact = newContact(newPerson("Jon", "Doe"),
			newAddress("100 Main St.", "Portland", State.OREGON, "97205"));

		assertThat(contactsService.validateAddress(contact)).isEqualTo(contact);
	}

	@Test
	public void validateEmailIsSuccessful() {
		Contact contact = newContact(newPerson("Jon", "Doe"), "jonDoe@work.com");

		assertThat(contactsService.validateEmail(contact)).isEqualTo(contact);
	}

	@Test
	public void invalidEmailThrowsIllegalArgumentException() {
		Person jonDoe = newPerson("Jon", "Doe");

		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage(String.format(
			"Email [jonDoe@bar.biz] in Contact for Person [%s] is not valid", jonDoe));

		contactsService.validateEmail(newContact(jonDoe, "jonDoe@bar.biz"));
	}

	@Test
	public void validatePhoneNumberIsSuccessful() {
		Contact contact = newContact(newPerson("Jon", "Doe"), newPhoneNumber("503", "541", "1234"));

		assertThat(contactsService.validatePhoneNumber(contact)).isEqualTo(contact);
	}

	@Test
	public void invalidPhoneNumberThrowsIllegalArgumentExcepion() {
		Person jonDoe = newPerson("Jon", "Doe");
		PhoneNumber phoneNumber = newPhoneNumber("503", "555", "1234");

		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage(String.format(
			"Phone Number [%1$s] in Contact for Person [%2$s] is not valid; '555' is not valid phone number exchange",
				phoneNumber, jonDoe));

		contactsService.validatePhoneNumber(newContact(jonDoe, phoneNumber));
	}
}
