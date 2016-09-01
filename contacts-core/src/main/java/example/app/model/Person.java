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
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.Region;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import example.app.model.support.Identifiable;

/**
 * The Person class is an abstract data type (ADT) modeling a person.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see javax.persistence.Entity
 * @see javax.persistence.Table
 * @see org.springframework.data.gemfire.mapping.Region
 * @see example.app.model.support.Identifiable
 * @since 1.0.0
 */
@Entity
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("person")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "People")
@Region("People")
@JsonIgnoreProperties(value = { "new", "notNew" }, ignoreUnknown = true)
@SuppressWarnings("unused")
public class Person implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = -7204456214709927355L;

	private Long id;

	private LocalDate birthDate;

	private Gender gender;

	private String firstName;

	private String lastName;

	public static Person newPerson(String firstName, String lastName) {
		Assert.hasText(firstName, "firstName is required");
		Assert.hasText(lastName, "lastName is required");

		Person person = new Person();

		person.setFirstName(firstName);
		person.setLastName(lastName);

		return person;
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

	@Transient
	@SuppressWarnings("all")
	public int getAge() {
		LocalDate birthDate = getBirthDate();
		Assert.state(birthDate != null, String.format("birth date of person [%s] is unknown", getName()));
		Period period = Period.between(birthDate, LocalDate.now());
		return period.getYears();
	}

	public void setBirthDateFor(int age) {
		Assert.isTrue(age >= 0, "Age must be greater than equal to 0");
		setBirthDate(LocalDate.now().minusYears(age));
	}

	public void setBirthDate(LocalDate birthDate) {
		if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException(String.format("[%s] cannot be born after today [%s]",
				getName(), toString(LocalDate.now())));
		}

		this.birthDate = birthDate;
	}

	@Column(name = "birth_date")
	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "first_name")
	public String getFirstName() {
		return firstName;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Enumerated(EnumType.STRING)
	public Gender getGender() {
		return gender;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "last_name")
	public String getLastName() {
		return lastName;
	}

	@Transient
	public String getName() {
		return String.format("%1$s %2$s", getFirstName(), getLastName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Person)) {
			return false;
		}

		Person that = (Person) obj;

		return ObjectUtils.nullSafeEquals(this.getBirthDate(), that.getBirthDate())
			&& ObjectUtils.nullSafeEquals(this.getFirstName(), that.getFirstName())
			&& ObjectUtils.nullSafeEquals(this.getLastName(), that.getLastName());
	}

	@Override
	public int hashCode() {
		int hashValue = 17;
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getBirthDate());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getFirstName());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getLastName());
		return hashValue;
	}

	@Override
	public String toString() {
		return getName();
		//return String.format("{ @type = %1$s, firstName = %2$s, lastName = %3$s, birthDate = %4$s, gender = %5$s }",
		//	getClass().getName(), getFirstName(), getLastName(), toString(getBirthDate()), getGender());
	}

	protected String toString(LocalDate date) {
		return (date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
	}

	@SuppressWarnings("unchecked")
	public <T extends Person> T age(int age) {
		setBirthDateFor(age);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends Person> T as(Gender gender) {
		setGender(gender);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends Person> T born(LocalDate birthDate) {
		setBirthDate(birthDate);
		return (T) this;
	}
}
