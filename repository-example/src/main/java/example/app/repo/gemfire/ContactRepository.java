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

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.gemfire.repository.query.annotation.Hint;
import org.springframework.data.gemfire.repository.query.annotation.Limit;
import org.springframework.data.gemfire.repository.query.annotation.Trace;
import org.springframework.data.repository.query.Param;

import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.Gender;
import example.app.model.PhoneNumber;
import example.app.model.State;

/**
 * Spring Data {@link GemfireRepository} for performing basic data access, CRUD and querying operations on
 * {@link Contact} objects stored and managed in Apache Geode (or Pivotal GemFire).
 *
 * @author John Blum
 * @see example.app.model.Contact
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface ContactRepository extends GemfireRepository<Contact, Long> {

	@Trace
	List<Contact> findByAddressCityAndAddressState(@Param("city") String city, @Param("state") State state);

	@Trace
	@Hint("EmailIdx")
	Contact findByEmail(@Param ("email") String email);

	@Trace
	List<Contact> findByEmailLike(@Param("email") String emailWildcard);

	@Trace
	List<Contact> findByPersonAgeGreaterThanEqualOrderByPersonLastNameAscPersonAgeDesc(@Param("age") int age);

	@Trace
	List<Contact> findByPersonGender(@Param("gender") Gender gender);

	@Trace
	//@Query("SELECT * FROM /Contacts c WHERE c.person.firstName.equalsIgnoreCase($1) AND c.person.lastName.equalsIgnoreCase($2)")
	//@Query("SELECT * FROM /Contacts c WHERE c.person.firstName.toLowerCase LIKE $1 AND c.person.lastName.toLowerCase LIKE $2")
	List<Contact> findByPersonFirstNameIgnoreCaseAndPersonLastNameIgnoreCase(@Param("firstName") String firstName, @Param("lastName") String lastName);

	@Trace
	List<Contact> findByPersonFirstNameAndPersonLastNameAllIgnoreCase(@Param("firstName") String firstName, @Param("lastName") String lastName);

	@Trace
	Contact findByPersonId(@Param("id") Long id);

	@Trace
	@Hint("PersonLastNameIdx")
	List<Contact> findByPersonLastName(@Param("lastName") String lastName);

	@Trace
	@Limit(5)
	List<Contact> findByPersonLastNameLike(@Param("lastName") String lastNameWildcard, @Param("sort") Sort sort);

	@Trace
	List<Contact> findByPhoneNumber(@Param("phoneNumber") PhoneNumber phoneNumber);

	@Query("SELECT DISTINCT customer"
		+ " FROM /Customers customer, /Contacts contact"
		+ " WHERE customer.firstName = contact.person.firstName AND customer.lastName = contact.person.lastName")
	List<Customer> findAllCustomersWithContactInformation();

}
