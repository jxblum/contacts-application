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

import org.springframework.util.Assert;

/**
 * The State enum is an enumeration of the United States.
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public enum State {
	ALABAMA("AL", "Alabama"),
	ALASKA("AK", "Alaska"),
	ARIZONA("AZ", "Arizona"),
	ARKANSAS("AR", "Arkansas"),
	CALIFORNIA("CA", "California"),
	COLORADO("CO", "Colarado"),
	CONNECTICUT("CT", "Connecticut"),
	DELAWARE("DE", "Delaware"),
	FLORIDA("FL", "Florida"),
	GEORGIA("GA", "Georgia"),
	HAWAII("HI", "Hawaii"),
	IDAHO("ID", "Idaho"),
	ILLINOIS("IL", "Illinois"),
	INDIANA("IN", "Indiana"),
	IOWA("IA", "Iowa"),
	KANSAS("KA", "Kansas"),
	KENTUCKY("KY", "Kentucky"),
	LOUISIANA("LA", "Louisiana"),
	MAINE("ME", "Maine"),
	MARYLAND("MD", "Maryland"),
	MASSACHUSETTS("MA", "Massachusetts"),
	MICHIGAN("MI", "Michigan"),
	MINNESOTA("MN", "Minnesota"),
	MISSISSIPPI("MS", "Mississippi"),
	MISSOURI("MO", "Missouri"),
	MONTANA("MT", "Montana"),
	NEBRASKA("NE", "Nebraska"),
	NEVADA("NV", "Nevada"),
	NEW_HAMPSHIRE("NH", "New Hampshire"),
	NEW_JERSEY("NJ", "New Jersey"),
	NEW_MEXICO("NM", "New Mexico"),
	NEW_YORK("NY", "New York"),
	NORTH_CAROLINA("NC", "North Carolina"),
	NORTH_DAKOTA("ND", "North Dakota"),
	OHIO("OH", "Ohio"),
	OKLAHOMA("OK", "Oklahoma"),
	OREGON("OR", "Oregon"),
	PENNSYLVANIA("PA", "Pennsylvannia"),
	RHODE_ISLAND("RI", "Rhode Island"),
	SOUTH_CAROLINA("SC", "South Carolina"),
	SOUTH_DAKOTA("SD", "South Dakota"),
	TENNESSEE("TN", "Tennessee"),
	TEXAS("TX", "Texas"),
	UTAH("UT", "Utah"),
	VERMONT("VT", "Vermont"),
	VIRGINIA("VA", "Virginia"),
	WASHINGTON("WA", "Washington"),
	WEST_VIRGINIA("WV", "West Virginia"),
	WISCONSIN("WI", "Wisconsin"),
	WYOMING("WY", "Wyoming");

	private String abbreviation;
	private String name;

	public static State valueOfAbbreviation(String abbreviation) {
		for (State state : values()) {
			if (state.getAbbreviation().equalsIgnoreCase(abbreviation)) {
				return state;
			}
		}

		return null;
	}

	public static State valueOfName(String name) {
		for (State state : values()) {
			if (state.getName().equalsIgnoreCase(name)) {
				return state;
			}
		}

		return null;
	}

	State(String abbreviation, String name) {
		Assert.hasText(abbreviation, "State abbreviation is required");
		Assert.hasText(name, "State name is required");

		this.abbreviation = abbreviation;
		this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getAbbreviation();
	}
}
