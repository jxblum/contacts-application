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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.MappedSuperclass;

import org.springframework.data.gemfire.mapping.Region;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * The Customer class is an abstract data type (ADT) that models a customer.
 *
 * @author John Blum
 * @see example.app.model.Person
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@MappedSuperclass
@DiscriminatorValue("customer")
@Region("Customers")
@SuppressWarnings("unused")
public class Customer extends Person {

	private String accountNumber;

	public static Customer newCustomer(String firstName, String lastName) {
		Assert.hasText(firstName, "firstName is required");
		Assert.hasText(lastName, "lastName is required");

		Customer customer = new Customer();

		customer.setFirstName(firstName);
		customer.setLastName(lastName);

		return customer;
	}

	public boolean hasAccount() {
		return StringUtils.hasText(getAccountNumber());
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	@Column(name = "account_number", nullable = false, unique = true)
	public String getAccountNumber() {
		return accountNumber;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}

		if (!(obj instanceof Customer)) {
			return false;
		}

		Customer that = (Customer) obj;

		return super.equals(obj)
			&& ObjectUtils.nullSafeEquals(this.getAccountNumber(), that.getAccountNumber());
	}

	@Override
	public int hashCode() {
		int hashValue = super.hashCode();
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getAccountNumber());
		return hashValue;
	}

	@Override
	public String toString() {
		return String.format("{ @type = %1$s, name = %2$s, accountNumber = %3$s }",
			getClass().getName(), getName(), getAccountNumber());
	}

	public Customer with(String accountNumber) {
		setAccountNumber(accountNumber);
		return this;
	}
}
