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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.service;

import static example.app.model.Contact.newContact;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import example.app.model.Address;
import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.PhoneNumber;
import example.app.model.support.Identifiable;
import example.app.repo.gemfire.ContactRepository;
import example.app.repo.gemfire.CustomerRepository;

/**
 * The {@link CustomerService} class is a Spring {@link Service @Service} class used to manage customer interactions
 * and relationships.
 *
 * @author John Blum
 * @see org.springframework.stereotype.Service
 * @see org.springframework.transaction.annotation.Transactional
 * @see example.app.model.Address
 * @see example.app.model.Contact
 * @see example.app.model.Customer
 * @see example.app.model.PhoneNumber
 * @see example.app.model.support.Identifiable
 * @see example.app.repo.gemfire.ContactRepository
 * @see example.app.repo.gemfire.CustomerRepository
 * @since 1.0.0
 */
@Service("customerService")
@SuppressWarnings("unused")
public class CustomerService {

	protected static final Pattern EMAIL_PATTERN =
		Pattern.compile("[a-zA-Z0-9_]+@[a-zA-Z0-9_]+(\\.com|\\.net|\\.org|\\.edu)");

	private final ContactRepository contactRepository;

	private final CustomerRepository customerRepository;

	@Autowired
	public CustomerService(ContactRepository contactRepository, CustomerRepository customerRepository) {
		this.contactRepository = contactRepository;
		this.customerRepository = customerRepository;
	}

	protected ContactRepository getContactRepository() {
		Assert.state(contactRepository != null, "ContactRepository is required");
		return contactRepository;
	}

	protected CustomerRepository getCustomerRepository() {
		Assert.state(customerRepository != null, "CustomerRepository is required");
		return customerRepository;
	}

	protected String newAccountNumber() {
		return UUID.randomUUID().toString();
	}

	protected long newId() {
		return System.nanoTime();
	}

	protected <T extends Identifiable<Long>> T setId(T identifiable) {

		Optional.ofNullable(identifiable)
			.filter(Identifiable::isNew)
			.ifPresent(it -> it.setId(newId()));

		return identifiable;
	}

	@Transactional
	public Customer createAccount(Customer customer) {

		Assert.state(!customer.hasAccount(),  String.format("Customer [%s] already has an account", customer));

		return getCustomerRepository().save(setId(customer.with(newAccountNumber())));
	}

	@Transactional
	public Customer createAccountIfNotExists(Customer customer) {

		Customer existingCustomer = Optional.of(customer)
			.filter(Customer::hasAccount)
			.map(it -> getCustomerRepository().findByAccountNumber(it.getAccountNumber()))
			.orElseGet(() ->
				Optional.of(customer)
					.filter(Customer::isNotNew)
					.map(it -> getCustomerRepository().findById(it.getId()).orElse(null))
					.orElse(null));

		if (existingCustomer == null || !customer.hasAccount()) {
			customer.setAccountNumber(null);
			existingCustomer = createAccount(customer);
		}

		return existingCustomer;
	}

	@Transactional(readOnly = true)
	public Contact findContactInformation(Customer customer) {

		Assert.notNull(customer, "Customer is required");

		return Optional.of(customer)
			.filter(Customer::isNotNew)
			.map(it -> getContactRepository().findByPersonId(customer.getId()))
			.orElse(null);
	}

	protected Contact saveContactInformation(Customer customer, Function<Contact, Contact> customerContactFunction) {

		return getContactRepository().save(customerContactFunction.apply(findContactInformation(
			createAccountIfNotExists(customer))));
	}

	@Transactional
	public Contact addContactInformation(Customer customer, Address address) {

		return saveContactInformation(customer, (Contact customerContact) ->
			Optional.ofNullable(customerContact).map(it -> it.with(validate(address)))
				.orElseGet(() -> newContact(customer, validate(address)).identifiedBy(newId())));
	}

	@Transactional
	public Contact addContactInformation(Customer customer, String email) {

		return saveContactInformation(customer, (Contact customerContact) ->
			Optional.ofNullable(customerContact).map(it -> it.with(validate(email)))
				.orElseGet(() -> newContact(customer, validate(email)).identifiedBy(newId())));
	}

	@Transactional
	public Contact addContactInformation(Customer customer, PhoneNumber phoneNumber) {

		return saveContactInformation(customer, (Contact customerContact) ->
			Optional.ofNullable(customerContact).map(it -> it.with(validate(phoneNumber)))
				.orElseGet(() -> newContact(customer, validate(phoneNumber)).identifiedBy(newId())));
	}

	protected Address validate(Address address) {
		return address;
	}

	protected String validate(String email) {

		Assert.isTrue(EMAIL_PATTERN.matcher(email).find(), String.format("Email [%s] is not valid", email));

		return email;
	}

	protected PhoneNumber validate(PhoneNumber phoneNumber) {

		Assert.isTrue(!"555".equals(phoneNumber.getPrefix()), String.format(
			"'555' is not a valid phone number [%s] exchange", phoneNumber));

		return phoneNumber;
	}
}
