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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.cp.elements.lang.Identifiable;
import org.cp.elements.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Indexed;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.geo.Point;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The {@link Address} class is an Abstract Data Type (ADT) that models a US address location.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see javax.persistence.Entity
 * @see org.cp.elements.lang.Identifiable
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
@Entity
@Region
@JsonIgnoreProperties(value = { "new", "notNew" }, ignoreUnknown = true)
@SuppressWarnings("unused")
public class Address implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = -1775411208922748140L;

	private Long id;

	@Indexed
	private Point location;

	private State state;

	private String city;
	private String street;
	private String zipCode;

	private Type type = Type.DEFAULT;

	public static Address newAddress(Point location) {

		Assert.notNull(location, "location is required");

		Address address = new Address();

		address.setLocation(location);

		return address;
	}

	public static Address newAddress(String street, String city, State state, String zipCode) {

		Assert.hasText(street, "street is required");
		Assert.hasText(city, "city is required");
		Assert.notNull(state, "state is required");
		Assert.hasText(zipCode, "zipCode is required");

		Address address = new Address();

		address.setStreet(street);
		address.setCity(city);
		address.setState(state);
		address.setZipCode(zipCode);

		return address;
	}

	public static Point newPoint(double x, double y) {
		return new Point(x, y);
	}

	public static Address parse(String addressValue) {

		Assert.hasText(addressValue, "Address value is required");

		String[] addressComponents = addressValue.split(",");

		Assert.isTrue(addressComponents.length == 4, "Expected Street, City, State and Zip");

		String street = StringUtils.trim(addressComponents[0]);
		String city = StringUtils.trim(addressComponents[1]);
		State state = State.valueOfAbbreviation(StringUtils.trim(addressComponents[2]));
		String zipCode = StringUtils.trim(addressComponents[3]);

		return Address.newAddress(street, city, state, zipCode);
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
	@javax.persistence.Id
	@GeneratedValue
	public Long getId() {
		return this.id;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public Point getLocation() {
		return this.location;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	@Column(nullable = false)
	public String getStreet() {
		return this.street;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(nullable = false)
	public String getCity() {
		return this.city;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public State getState() {
		return this.state;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Column(name = "zip_code", nullable = false)
	public String getZipCode() {
		return this.zipCode;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Enumerated(EnumType.STRING)
	public Type getType() {
		return this.type != null ? this.type : Type.DEFAULT;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Address)) {
			return false;
		}

		Address that = (Address) obj;

		return ObjectUtils.nullSafeEquals(this.getLocation(), that.getLocation())
			&& ObjectUtils.nullSafeEquals(this.getStreet(), that.getStreet())
			&& ObjectUtils.nullSafeEquals(this.getCity(), that.getCity())
			&& ObjectUtils.nullSafeEquals(this.getState(), that.getState())
			&& ObjectUtils.nullSafeEquals(this.getZipCode(), that.getZipCode());
	}

	@Override
	public int hashCode() {

		int hashValue = 17;

		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getLocation());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getStreet());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getCity());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getState());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getZipCode());

		return hashValue;
	}

	@Override
	public String toString() {
		return String.format("%1$s %2$s, %3$s %4$s [Location = %5$s, Type = %6$s]",
			getStreet(), getCity(), getState(), getZipCode(), getLocation(), getType());
	}

	public Address with(Point location) {
		setLocation(location);
		return this;
	}

	/**
	 * The Type enum is an enumeration of different {@link Address} types.
	 */
	public enum Type {

		BUSINESS,
		HOME,
		OFFICE,
		PO_BOX,
		RESIDENTIAL,
		WORK;

		public static final Type DEFAULT = Type.HOME;

	}
}
