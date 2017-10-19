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

package example.app.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.cp.elements.lang.Identifiable;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * The {@link Contact} class is an Abstract Data Type (ADT) modeling contact information for an individual person,
 * such as address, email address and phone number.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see javax.persistence.Entity
 * @see javax.persistence.Table
 * @see org.cp.elements.lang.Identifiable
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @see example.app.model.Address
 * @see example.app.model.Person
 * @see example.app.model.PhoneNumber
 * @since 1.0.0
 */
@Entity
@Table(name = "Contacts")
@Region("Contacts")
@JsonIgnoreProperties(value = { "new", "notNew" }, ignoreUnknown = true)
@SuppressWarnings("unused")
public class Contact implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = 6434575088917742861L;

	private Long id;

	private Address address;

	private Person person;

	private PhoneNumber phoneNumber;

	private String email;

	public static Contact newContact(Person person, Address address) {

		Assert.notNull(person, "Person is required");
		Assert.notNull(address, "Address is required");

		Contact contact = new Contact();

		contact.setPerson(person);
		contact.setAddress(address);

		return contact;
	}

	public static Contact newContact(Person person, String email) {

		Assert.notNull(person, "Person is required");
		Assert.hasText(email, "Email is required");

		Contact contact = new Contact();

		contact.setPerson(person);
		contact.setEmail(email);

		return contact;
	}

	public static Contact newContact(Person person, PhoneNumber phoneNumber) {

		Assert.notNull(person, "Person is required");
		Assert.notNull(phoneNumber, "PhoneNumber is required");

		Contact contact = new Contact();

		contact.setPerson(person);
		contact.setPhoneNumber(phoneNumber);

		return contact;
	}

	public static Contact newContact(Person person, Address address, PhoneNumber phoneNumber) {

		Assert.notNull(person, "Person is required");
		Assert.notNull(address, "Address is required");
		Assert.notNull(phoneNumber, "PhoneNumber is required");

		Contact contact = new Contact();

		contact.setPerson(person);
		contact.setAddress(address);
		contact.setPhoneNumber(phoneNumber);

		return contact;
	}

	public static Contact newContact(Person person, String email, PhoneNumber phoneNumber) {

		Assert.notNull(person, "Person is required");
		Assert.hasText(email, "Email is required");
		Assert.notNull(phoneNumber, "PhoneNumber is required");

		Contact contact = new Contact();

		contact.setPerson(person);
		contact.setEmail(email);
		contact.setPhoneNumber(phoneNumber);

		return contact;
	}

	public static Contact newContact(Person person, Address address, String email, PhoneNumber phoneNumber) {

		Assert.notNull(person, "Person is required");
		Assert.notNull(address, "Address is required");
		Assert.hasText(email, "Email is required");
		Assert.notNull(phoneNumber, "PhoneNumber is required");

		Contact contact = new Contact();

		contact.setPerson(person);
		contact.setAddress(address);
		contact.setEmail(email);
		contact.setPhoneNumber(phoneNumber);

		return contact;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
	@javax.persistence.Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	protected final void setPerson(Person person) {
		Assert.notNull(person, "Person cannot be null");
		this.person = person;
	}

	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	public Person getPerson() {
		return person;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "address_id")
	public Address getAddress() {
		return address;
	}

	public boolean hasAddress() {
		return (getAddress() != null);
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public boolean hasEmail() {
		return StringUtils.hasText(getEmail());
	}

	public void setPhoneNumber(PhoneNumber phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "phone_number_id")
	public PhoneNumber getPhoneNumber() {
		return phoneNumber;
	}

	public boolean hasPhoneNumber() {
		return (getPhoneNumber() != null);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Contact)) {
			return false;
		}

		Contact that = (Contact) obj;

		return ObjectUtils.nullSafeEquals(this.getPerson(), that.getPerson())
			&& ObjectUtils.nullSafeEquals(this.getAddress(), that.getAddress())
			&& ObjectUtils.nullSafeEquals(this.getEmail(), that.getEmail())
			&& ObjectUtils.nullSafeEquals(this.getPhoneNumber(), that.getPhoneNumber());
	}

	@Override
	public int hashCode() {

		int hashValue = 17;

		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getPerson());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getAddress());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getEmail());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getPhoneNumber());

		return hashValue;
	}

	@Override
	public String toString() {

		return String.format(
			"{ @type = %1$s, id = %2$d, person = %3$s, address = %4$s, phoneNumber = %5$s, email = %6$s }",
				getClass().getName(), getId(), getPerson(), getAddress(), getPhoneNumber(), getEmail());
	}

	public Contact with(Address address) {
		setAddress(address);
		return this;
	}

	public Contact with(String email) {
		setEmail(email);
		return this;
	}

	public Contact with(PhoneNumber phoneNumber) {
		setPhoneNumber(phoneNumber);
		return this;
	}
}
