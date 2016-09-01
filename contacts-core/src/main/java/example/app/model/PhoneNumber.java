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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.data.annotation.Id;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import example.app.model.support.Identifiable;

/**
 * The PhoneNumber class is an abstract data type (ADT) modeling a US phone number.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see javax.persistence.Entity
 * @see example.app.model.support.Identifiable
 * @see <a href="https://en.wikipedia.org/wiki/Telephone_numbering_plan">Telephone Numbering Plan</a>
 * @since 1.0.0
 */
@Entity
@JsonIgnoreProperties(value = { "new", "notNew" }, ignoreUnknown = true)
@SuppressWarnings("unused")
public class PhoneNumber implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = -7966052569771224197L;

	private Long id;

	private String areaCode;
	private String extension;
	private String prefix;
	private String suffix;

	private Type type;

	public static PhoneNumber newPhoneNumber(String areaCode, String prefix, String suffix) {
		Assert.hasText(areaCode, "areaCode is required");
		Assert.hasText(prefix, "prefix is required");
		Assert.hasText(suffix, "suffix is required");

		PhoneNumber phoneNumber = new PhoneNumber();

		phoneNumber.setAreaCode(areaCode);
		phoneNumber.setPrefix(prefix);
		phoneNumber.setSuffix(suffix);

		return phoneNumber;
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

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@Column(name = "area_code", nullable = false)
	public String getAreaCode() {
		return areaCode;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Column(nullable = false)
	public String getPrefix() {
		return prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Column(nullable = false)
	public String getSuffix() {
		return suffix;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Enumerated(EnumType.STRING)
	public Type getType() {
		return (type != null ? type : Type.DEFAULT);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof PhoneNumber)) {
			return false;
		}

		PhoneNumber that = (PhoneNumber) obj;

		return ObjectUtils.nullSafeEquals(this.getAreaCode(), that.getAreaCode())
			&& ObjectUtils.nullSafeEquals(this.getPrefix(), that.getPrefix())
			&& ObjectUtils.nullSafeEquals(this.getSuffix(), that.getSuffix())
			&& ObjectUtils.nullSafeEquals(this.getExtension(), that.getExtension());
	}

	@Override
	public int hashCode() {
		int hashValue = 17;
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getAreaCode());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getPrefix());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getSuffix());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getExtension());
		return hashValue;
	}

	@Override
	public String toString() {
		String extension = getExtension();

		return String.format("(%1$s) %2$s-%3$s%4$s [Type = %5$s]", getAreaCode(), getPrefix(), getSuffix(),
			StringUtils.hasText(extension) ? String.format(" x%s", extension) : "", getType());
	}

	public PhoneNumber with(String extension) {
		setExtension(extension);
		return this;
	}

	public enum Type {
		BUSINESS,
		IP,
		HOME,
		MOBILE,
		OFFICE,
		WORK;

		public static final Type DEFAULT = Type.HOME;

	}
}
