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

package example.app.core.mapping.json.jackson.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import example.app.model.Address;
import example.app.model.Contact;
import example.app.model.Gender;
import example.app.model.Person;
import example.app.model.PhoneNumber;
import example.app.model.State;

/**
 * Test suite of test cases testing the mapping of JSON data to this example application's domain object model
 * using Jackson's {@link ObjectMapper}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see example.app.model.Address
 * @see example.app.model.Contact
 * @see example.app.model.Gender
 * @see example.app.model.Person
 * @see example.app.model.PhoneNumber
 * @see example.app.model.State
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class JsonToObjectMappingIntegrationTests {

	protected void assertAddress(Address address, String street, String city, State state, String zipCode) {
		assertAddress(address, street, city, state, zipCode, Address.Type.HOME);
	}

	protected void assertAddress(Address address, String street, String city, State state, String zipCode,
			Address.Type type) {

		assertThat(address).isNotNull();
		assertThat(address.getStreet()).isEqualTo(street);
		assertThat(address.getCity()).isEqualTo(city);
		assertThat(address.getState()).isEqualTo(state);
		assertThat(address.getZipCode()).isEqualTo(zipCode);
		assertThat(address.getType()).isEqualTo(type);
	}

	protected void assertPerson(Person person, String name, Gender gender, int age) {
		assertPerson(person, name, gender, birthDateFor(age));
	}

	protected void assertPerson(Person person, String name, Gender gender, LocalDate birthDate) {
		assertThat(person).isNotNull();
		assertThat(person.getName()).isEqualTo(name);
		assertThat(person.getGender()).isEqualTo(gender);
		assertThat(person.getBirthDate()).isEqualTo(birthDate);
	}

	protected void assertPhoneNumber(PhoneNumber phoneNumber, String areaCode, String prefix, String suffix,
			String extension) {

		assertPhoneNumber(phoneNumber, areaCode, prefix, suffix, extension, PhoneNumber.Type.HOME);
	}

	protected void assertPhoneNumber(PhoneNumber phoneNumber, String areaCode, String prefix, String suffix,
			String extension, PhoneNumber.Type type) {

		assertThat(phoneNumber).isNotNull();
		assertThat(phoneNumber.getAreaCode()).isEqualTo(areaCode);
		assertThat(phoneNumber.getPrefix()).isEqualTo(prefix);
		assertThat(phoneNumber.getSuffix()).isEqualTo(suffix);
		assertThat(phoneNumber.getExtension()).isEqualTo(extension);
		assertThat(phoneNumber.getType()).isEqualTo(type);
	}

	protected int ageFor(LocalDate birthDate) {
		return Period.between(birthDate, LocalDate.now()).getYears();
	}

	protected LocalDate birthDateFor(int age) {
		return LocalDate.now().minusYears(age);
	}

	protected InputStream toInputStream(String pathname) throws IOException {
		return new ClassPathResource(pathname, JsonToObjectMappingIntegrationTests.class).getInputStream();
	}

	@Test
	public void convertJaneDoeJsonToContactIsSuccessful() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		Contact contact = objectMapper.readValue(toInputStream("/janeDoeWithAgeContact.json"), Contact.class);

		System.err.printf("Contact is [%s]%n", contact);

		assertThat(contact).isNotNull();
		assertThat(contact.getId()).isEqualTo(2L);
		assertThat(contact.getEmail()).isEqualTo("janeDoe@springone.com");

		Person janeDoe = contact.getPerson();

		assertPerson(janeDoe, "Jane Doe", Gender.FEMALE, 21);

		Address address = contact.getAddress();

		assertAddress(address, "3730 S Las Vegas Blvd", "Las Vegas", State.NEVADA, "89158");

		PhoneNumber phoneNumber = contact.getPhoneNumber();

		assertPhoneNumber(phoneNumber, "702", "590", "7111", null);
	}

	@Test
	public void convertPieDoeJsonToContactIsSuccessful() throws Exception {
		ObjectMapper objectMapper = LocalDateDeserializer.register(new ObjectMapper());

		Contact contact = objectMapper.readValue(toInputStream("/pieDoeWithBirthDateContact.json"), Contact.class);

		System.err.printf("Contact is [%s]%n", contact);

		assertThat(contact).isNotNull();
		assertThat(contact.getId()).isEqualTo(3L);
		assertThat(contact.getEmail()).isEqualTo("pieDoe@springone.com");

		Person pieDoe = contact.getPerson();

		assertPerson(pieDoe, "Pie Doe", Gender.FEMALE, LocalDate.of(2000, Month.AUGUST, 4));

		Address address = contact.getAddress();

		assertAddress(address, "3730 S Las Vegas Blvd", "Las Vegas", State.NEVADA, "89158");

		PhoneNumber phoneNumber = contact.getPhoneNumber();

		assertPhoneNumber(phoneNumber, "702", "590", "7111", null);
	}
}
