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

import static example.app.model.Person.newPerson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test suite of test cases testing the contract and functionality of the {@link Person} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see example.app.model.Person
 * @since 1.0.0
 */
public class PersonTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	protected LocalDate getBirthDateFor(int age) {
		return LocalDate.now().minusYears(age);
	}

	@Test
	public void newPersonWithFirstAndLastName() {
		Person jonDoe = newPerson("Jon", "Doe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getId()).isNull();
		assertThat(jonDoe.getBirthDate()).isNull();
		assertThat(jonDoe.getFirstName()).isEqualTo("Jon");
		assertThat(jonDoe.getGender()).isNull();
		assertThat(jonDoe.getLastName()).isEqualTo("Doe");
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
	}

	@Test
	public void newPersonWithNameBirthDateGenderAndId() {
		LocalDate birthDate = LocalDate.of(1969, Month.APRIL, 1);

		Person janeDoe = newPerson("Jane", "Doe").as(Gender.FEMALE).born(birthDate).identifiedBy(1L);

		assertThat(janeDoe).isNotNull();
		assertThat(janeDoe.getId()).isEqualTo(1L);
		assertThat(janeDoe.getBirthDate()).isEqualTo(birthDate);
		assertThat(janeDoe.getGender()).isEqualTo(Gender.FEMALE);
		assertThat(janeDoe.getName()).isEqualTo("Jane Doe");
	}

	@Test
	public void getAge() {
		Person jonDoe = newPerson("Jon", "Doe").born(getBirthDateFor(21));

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.getBirthDate()).isNotNull();
		assertThat(jonDoe.getAge()).isEqualTo(21);
	}

	@Test
	public void getAgeWhenBirthDateIsNullThrowsIllegalStateException() {
		Person jonDoe = newPerson("Jon", "Doe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.getBirthDate()).isNull();

		exception.expect(IllegalStateException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("birth date of person [Jon Doe] is unknown");

		jonDoe.getAge();
	}

	@Test
	public void setBirthDateForAgeIsSuccessful() {
		Person jonDoe = newPerson("Jon", "Doe");
		int age = 21;

		jonDoe.setBirthDateFor(age);

		LocalDate now = LocalDate.now();
		LocalDate birthDate = jonDoe.getBirthDate();

		assertThat(birthDate).isNotNull();
		assertThat(birthDate.getYear()).isEqualTo(now.getYear() - age);
		assertThat(birthDate.getMonth()).isEqualTo(now.getMonth());
		assertThat(birthDate.getDayOfMonth()).isEqualTo(now.getDayOfMonth());
	}

	@Test
	public void setBirthDateForAgeZeroIsSuccessful() {
		Person jonDoe = newPerson("Jon", "Doe");
		int age = 0;

		jonDoe.setBirthDateFor(age);

		LocalDate now = LocalDate.now();
		LocalDate birthDate = jonDoe.getBirthDate();

		assertThat(birthDate).isNotNull();
		assertThat(birthDate.getYear()).isEqualTo(now.getYear() - age);
		assertThat(birthDate.getMonth()).isEqualTo(now.getMonth());
		assertThat(birthDate.getDayOfMonth()).isEqualTo(now.getDayOfMonth());
	}

	@Test
	public void setBirthDateForNegativeAgeThrowsIllegalArgumentException() {
		exception.expect(IllegalArgumentException.class);
		exception.expectCause(is(nullValue(Throwable.class)));
		exception.expectMessage("Age must be greater than equal to 0");

		newPerson("Jon", "Doe").setBirthDateFor(-2);
	}

	@Test
	public void setBirthDateToFutureThrowsIllegalArgumentException() {
		Person jonDoe = newPerson("Jon", "Doe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.getBirthDate()).isNull();

		try {
			exception.expect(IllegalArgumentException.class);
			exception.expectCause(is(nullValue(Throwable.class)));
			exception.expectMessage(String.format("[Jon Doe] cannot be born after today [%s]",
				jonDoe.toString(LocalDate.now())));

			jonDoe.setBirthDate(getBirthDateFor(-10));
		}
		finally {
			assertThat(jonDoe.getBirthDate()).isNull();
		}
	}

	@Test
	public void setBirthDateToNullIsAllowed() {
		LocalDate birthDate = getBirthDateFor(42);

		Person jonDoe = Person.newPerson("Jon", "Doe").born(birthDate);

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.getBirthDate()).isEqualTo(birthDate);

		jonDoe.setBirthDate(null);

		assertThat(jonDoe.getBirthDate()).isNull();
	}
}
