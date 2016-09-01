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
import static example.app.model.Customer.newCustomer;
import static example.app.model.PhoneNumber.newPhoneNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import example.app.model.Address;
import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.PhoneNumber;
import example.app.model.State;
import example.app.model.support.Identifiable;
import example.app.repo.gemfire.ContactRepository;
import example.app.repo.gemfire.CustomerRepository;

/**
 * Test suite of test cases testing the contract and functionality of the {@link CustomerService} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see example.app.service.CustomerService
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTests {

	private static final int COUNT = 1000;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	private ContactRepository mockContactRepository;

	@Mock
	private CustomerRepository mockCustomerRepository;

	private CustomerService customerService;

	@Before
	public void setup() {
		customerService = new CustomerService(mockContactRepository, mockCustomerRepository);
	}

	@Test
	public void getInitializedContactRepositoryReturnsContactRepository() {
		assertThat(customerService.getContactRepository()).isEqualTo(mockContactRepository);
	}

	@Test
	public void getUnitializedContactRepositoryThrowsIllegalStateException() {
		exception.expect(IllegalStateException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("ContactRepository was not properly initialized");

		new CustomerService(null, mockCustomerRepository).getContactRepository();
	}

	@Test
	public void getInitializedCustomerRepositoryReturnsContactRepository() {
		assertThat(customerService.getCustomerRepository()).isEqualTo(mockCustomerRepository);
	}

	@Test
	public void getUnitializedCustomerRepositoryThrowsIllegalStateException() {
		exception.expect(IllegalStateException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("CustomerRepository was not properly initialized");

		new CustomerService(mockContactRepository, null).getCustomerRepository();
	}

	@Test
	public void newAccountNumberIsNotNullAndUnique() {
		Set<String> accountNumbers = new HashSet<>(COUNT);

		for (int count = 0; count < COUNT; count++) {
			accountNumbers.add(customerService.newAccountNumber());
		}

		assertThat(accountNumbers.size()).isEqualTo(COUNT);
	}

	@Test
	public void newIdIsNotNullAndUnique() {
		Set<Long> identifiers = new HashSet<>(COUNT);

		for (int count = 0; count < COUNT; count++) {
			identifiers.add(customerService.newId());
		}

		assertThat(identifiers.size()).isEqualTo(COUNT);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void setIdForIdentifiedObject() {
		Identifiable<Long> mockIdentifiable = mock(Identifiable.class);

		when(mockIdentifiable.getId()).thenReturn(1L);

		customerService.setId(mockIdentifiable);

		verify(mockIdentifiable, never()).setId(anyLong());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void setIdForUnidentifiedObject() {
		Identifiable<Long> mockIdentifiable = mock(Identifiable.class);

		when(mockIdentifiable.isNew()).thenReturn(true);

		customerService.setId(mockIdentifiable);

		verify(mockIdentifiable, times(1)).setId(anyLong());
	}

	@Test
	public void createAccountForNewCustomer() {
		Customer jonDoe = newCustomer("Jon", "Doe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.isNew()).isTrue();
		assertThat(jonDoe.hasAccount()).isFalse();

		when(mockCustomerRepository.save(jonDoe)).thenReturn(jonDoe);

		assertThat(customerService.createAccount(jonDoe)).isEqualTo(jonDoe);
		assertThat(jonDoe.isNew()).isFalse();
		assertThat(jonDoe.hasAccount()).isTrue();

		verify(mockCustomerRepository, times(1)).save(eq(jonDoe));
	}

	@Test
	public void createAccountForExistingCustomer() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);

		try {
			exception.expect(IllegalStateException.class);
			exception.expectCause(is(nullValue(Throwable.class)));
			exception.expectMessage(String.format("Customer [%s] already has an account", jonDoe));

			customerService.createAccount(jonDoe);
		}
		finally {
			verify(mockCustomerRepository, never()).save(any(Customer.class));
		}
	}

	@Test
	public void createAccountIfNotExistsForExistingCustomerWithAccountNumber() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123");
		Customer jonathonDoe = newCustomer("Jonathon", "Doe").with("123");

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonathonDoe);

		assertThat(customerService.createAccountIfNotExists(jonDoe)).isEqualTo(jonathonDoe);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockCustomerRepository, never()).findOne(anyLong());
		verify(mockCustomerRepository, never()).save(any(Customer.class));
	}

	@Test
	public void createAccountIfNotExistsForNonExistingCustomerWithAccountNumber() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123");

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(null);
		when(mockCustomerRepository.save(eq(jonDoe))).thenReturn(jonDoe);

		assertThat(customerService.createAccountIfNotExists(jonDoe)).isEqualTo(jonDoe);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockCustomerRepository, never()).findOne(anyLong());
		verify(mockCustomerRepository, times(1)).save(eq(jonDoe));
	}

	@Test
	public void createAccountIfNotExistsForExistingCustomerWithAccountNumberAndId() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("987").identifiedBy(1L);
		Customer jonathonDoe = newCustomer("Jonathon", "Doe").with("123").identifiedBy(1L);

		when(mockCustomerRepository.findByAccountNumber(anyString())).thenReturn(null);
		when(mockCustomerRepository.findOne(eq(1L))).thenReturn(jonathonDoe);

		assertThat(customerService.createAccountIfNotExists(jonDoe)).isEqualTo(jonathonDoe);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("987"));
		verify(mockCustomerRepository, times(1)).findOne(eq(1L));
		verify(mockCustomerRepository, never()).save(any(Customer.class));
	}

	@Test
	public void createAccountIfNotExistsForNonExistingCustomerWithAccountNumberAndId() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);

		when(mockCustomerRepository.findByAccountNumber(anyString())).thenReturn(null);
		when(mockCustomerRepository.findOne(anyLong())).thenReturn(null);
		when(mockCustomerRepository.save(eq(jonDoe))).thenReturn(jonDoe);

		assertThat(jonDoe.getAccountNumber()).isEqualTo("123");
		assertThat(jonDoe.getId()).isEqualTo(1L);
		assertThat(customerService.createAccountIfNotExists(jonDoe)).isEqualTo(jonDoe);
		assertThat(jonDoe.getAccountNumber()).isNotEqualTo("123");
		assertThat(jonDoe.getId()).isEqualTo(1L);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockCustomerRepository, times(1)).findOne(eq(1L));
		verify(mockCustomerRepository, times(1)).save(eq(jonDoe));
	}

	@Test
	public void createAccountIfNotExistsForNonExistingCustomerWithId() {
		Customer jonDoe = newCustomer("Jon", "Doe").identifiedBy(1L);

		when(mockCustomerRepository.findOne(anyLong())).thenReturn(null);
		when(mockCustomerRepository.save(eq(jonDoe))).thenReturn(jonDoe);

		assertThat(jonDoe.getAccountNumber()).isNull();
		assertThat(jonDoe.getId()).isEqualTo(1L);
		assertThat(customerService.createAccountIfNotExists(jonDoe)).isEqualTo(jonDoe);
		assertThat(jonDoe.getAccountNumber()).isNotNull();
		assertThat(jonDoe.getId()).isEqualTo(1L);

		verify(mockCustomerRepository, never()).findByAccountNumber(anyString());
		verify(mockCustomerRepository, times(1)).findOne(eq(1L));
		verify(mockCustomerRepository, times(1)).save(eq(jonDoe));
	}

	@Test
	public void createAccountIfNotExistsForNonExistingCustomer() {
		Customer jonDoe = newCustomer("Jon", "Doe");

		when(mockCustomerRepository.save(eq(jonDoe))).thenReturn(jonDoe);

		assertThat(jonDoe.getAccountNumber()).isNull();
		assertThat(jonDoe.getId()).isNull();
		assertThat(customerService.createAccountIfNotExists(jonDoe)).isEqualTo(jonDoe);
		assertThat(jonDoe.getAccountNumber()).isNotNull();
		assertThat(jonDoe.getId()).isNotNull();

		verify(mockCustomerRepository, never()).findByAccountNumber(anyString());
		verify(mockCustomerRepository, never()).findOne(anyLong());
		verify(mockCustomerRepository, times(1)).save(eq(jonDoe));
	}

	@Test
	public void findContactInformationForIdentifiedPersonReturnsPerson() {
		Customer jonDoe = newCustomer("Jon", "Doe").identifiedBy(1L);
		Contact expectedContact = newContact(jonDoe, "jonDoe@work.com");

		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(expectedContact);

		assertThat(customerService.findContactInformation(jonDoe)).isEqualTo(expectedContact);

		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
	}

	@Test
	public void findContactInformationForIdentifiedPersonReturnsNull() {
		Customer jonDoe = newCustomer("Jon", "Doe").identifiedBy(1L);

		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(null);

		assertThat(jonDoe.isNotNew()).isTrue();
		assertThat(customerService.findContactInformation(jonDoe)).isNull();

		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
	}

	@Test
	public void findContactInformationForUnidentifiedPersonReturnsNull() {
		Customer jonDoe = newCustomer("Jon", "Doe");

		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(null);

		assertThat(jonDoe.isNew()).isTrue();
		assertThat(customerService.findContactInformation(jonDoe)).isNull();

		verify(mockContactRepository, never()).findByPersonId(anyLong());
	}

	@Test
	public void findContactInformationWithNull() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Customer cannot be null");

		customerService.findContactInformation(null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void saveContactInformationCallsContactRepositorySave() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);
		Contact jonDoeContact = newContact(jonDoe, "jonDoe@work.com");

		Function<Contact, Contact> mockFunction = mock(Function.class);

		when(mockFunction.apply(eq(jonDoeContact))).thenReturn(jonDoeContact);
		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(jonDoeContact);
		when(mockContactRepository.save(eq(jonDoeContact))).thenReturn(jonDoeContact);

		assertThat(customerService.saveContactInformation(jonDoe, mockFunction)).isEqualTo(jonDoeContact);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockFunction, times(1)).apply(eq(jonDoeContact));
		verify(mockContactRepository, times(1)).save(eq(jonDoeContact));
	}

	@Test
	public void addAddressToCustomerWithExistingContact() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);
		Contact jonDoeContact = newContact(jonDoe, "jonDoe@work.com");
		Address expectedAddress = newAddress("100 Main St.", "Portland", State.OREGON, "97205");

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(jonDoeContact);
		when(mockContactRepository.save(eq(jonDoeContact))).thenReturn(jonDoeContact);

		assertThat(jonDoeContact.hasAddress()).isFalse();
		assertThat(customerService.addContactInformation(jonDoe, expectedAddress)).isEqualTo(jonDoeContact);
		assertThat(jonDoeContact.getAddress()).isEqualTo(expectedAddress);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockContactRepository, times(1)).save(eq(jonDoeContact));
	}

	@Test
	public void addAddressToCustomerCreatesNewContact() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);
		Address expectedAddress = newAddress("100 Main St.", "Portland", State.OREGON, "97205");

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(anyLong())).thenReturn(null);

		when(mockContactRepository.save(any(Contact.class))).then(
			(InvocationOnMock invocationOnMock) -> invocationOnMock.getArgumentAt(0, Contact.class));

		Contact contact = customerService.addContactInformation(jonDoe, expectedAddress);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.getAddress()).isEqualTo(expectedAddress);
		assertThat(contact.hasEmail()).isFalse();
		assertThat(contact.hasPhoneNumber()).isFalse();

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockContactRepository, times(1)).save(isA(Contact.class));
	}

	@Test
	public void addEmailToCustomerWithExistingContact() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);
		Contact jonDoeContact = newContact(jonDoe, newPhoneNumber("503", "541", "1234"));

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(jonDoeContact);
		when(mockContactRepository.save(eq(jonDoeContact))).thenReturn(jonDoeContact);

		assertThat(jonDoeContact.hasEmail()).isFalse();
		assertThat(customerService.addContactInformation(jonDoe, "jonDoe@work.com")).isEqualTo(jonDoeContact);
		assertThat(jonDoeContact.getEmail()).isEqualTo("jonDoe@work.com");

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockContactRepository, times(1)).save(eq(jonDoeContact));
	}

	@Test
	public void addEmailToCustomerCreatesNewContact() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(anyLong())).thenReturn(null);

		when(mockContactRepository.save(any(Contact.class))).then(
			(InvocationOnMock invocationOnMock) -> invocationOnMock.getArgumentAt(0, Contact.class));

		Contact contact = customerService.addContactInformation(jonDoe, "jonDoe@work.com");

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isFalse();
		assertThat(contact.getEmail()).isEqualTo("jonDoe@work.com");
		assertThat(contact.hasPhoneNumber()).isFalse();

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockContactRepository, times(1)).save(isA(Contact.class));
	}

	@Test
	public void addPhoneNumberToCustomerWithExistingContact() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);
		Contact jonDoeContact = newContact(jonDoe, "jonDoe@work.com");
		PhoneNumber expectedPhoneNumber = newPhoneNumber("503", "541", "1234");

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(eq(1L))).thenReturn(jonDoeContact);
		when(mockContactRepository.save(eq(jonDoeContact))).thenReturn(jonDoeContact);

		assertThat(jonDoeContact.hasPhoneNumber()).isFalse();
		assertThat(customerService.addContactInformation(jonDoe, expectedPhoneNumber)).isEqualTo(jonDoeContact);
		assertThat(jonDoeContact.getPhoneNumber()).isEqualTo(expectedPhoneNumber);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockContactRepository, times(1)).save(eq(jonDoeContact));
	}

	@Test
	public void addPhoneNumberToCustomerCreatesNewContact() {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").identifiedBy(1L);
		PhoneNumber expectedPhoneNumber = newPhoneNumber("503", "541", "1234");

		when(mockCustomerRepository.findByAccountNumber(eq("123"))).thenReturn(jonDoe);
		when(mockContactRepository.findByPersonId(anyLong())).thenReturn(null);

		when(mockContactRepository.save(any(Contact.class))).then(
			(InvocationOnMock invocationOnMock) -> invocationOnMock.getArgumentAt(0, Contact.class));

		Contact contact = customerService.addContactInformation(jonDoe, expectedPhoneNumber);

		assertThat(contact).isNotNull();
		assertThat(contact.getPerson()).isEqualTo(jonDoe);
		assertThat(contact.hasAddress()).isFalse();
		assertThat(contact.hasEmail()).isFalse();
		assertThat(contact.getPhoneNumber()).isEqualTo(expectedPhoneNumber);

		verify(mockCustomerRepository, times(1)).findByAccountNumber(eq("123"));
		verify(mockContactRepository, times(1)).findByPersonId(eq(1L));
		verify(mockContactRepository, times(1)).save(isA(Contact.class));
	}

	@Test
	public void addressValidationIsCorrect() {
		Address address = newAddress("100 Main St.", "Portland", State.OREGON, "97205");

		assertThat(customerService.validate(address)).isEqualTo(address);
	}

	@Test
	public void emailValidationIsCorrect() {
		assertThat(customerService.validate("jonDoe@work.com")).isEqualTo("jonDoe@work.com");
		assertThat(customerService.validate("janeDoe@home.net")).isEqualTo("janeDoe@home.net");
		assertThat(customerService.validate("cookieDoe@nonprofit.org")).isEqualTo("cookieDoe@nonprofit.org");
		assertThat(customerService.validate("pieDoe@school.edu")).isEqualTo("pieDoe@school.edu");
	}

	@Test
	public void invalidEmailThrowsIllegalArgumentException() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("email [joeDirt@bar.biz] is invalid");

		customerService.validate("joeDirt@bar.biz");
	}

	@Test
	public void phoneNumberValidationIsCorrect() {
		PhoneNumber phoneNumber = newPhoneNumber("503", "541", "1234");

		assertThat(customerService.validate(phoneNumber)).isEqualTo(phoneNumber);
	}

	@Test
	public void invalidPhoneNumberThrowsIllegalArgumentException() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("'555' is not a valid phone number [(503) 555-1234 [Type = HOME]] exchange");

		customerService.validate(newPhoneNumber("503", "555", "1234"));
	}
}
