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

package example.app.repo.provider;

import java.util.Arrays;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import example.app.model.Address;
import example.app.model.State;
import example.app.repo.support.GeocodingRepositoryAdapter;

/**
 * The GoogleMapsApiGeocodingRepository class is an implementation of the {@link example.app.repo.GeocodingRepository}
 * interface using Google's Maps API Goecoding web service to convert addresses and geographic coordinates.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see example.app.model.State
 * @see example.app.repo.support.GeocodingRepositoryAdapter
 * @see com.google.maps.GeocodingApi
 * @see com.google.maps.model.AddressComponent
 * @see com.google.maps.model.LatLng
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
@Repository("googleGeocodingRepository")
@SuppressWarnings("unused")
public class GoogleMapsApiGeocodingRepository extends GeocodingRepositoryAdapter {

  @Value("${google.apis.maps.geocoding.key}")
  private String apiKey;

  protected GeoApiContext newGeoApiContext() {
    return new GeoApiContext().setApiKey(apiKey);
  }

  protected Address toAddress(AddressComponent[] addressComponents) {
    String street = findAddressComponent(addressComponents, AddressComponentType.STREET_NUMBER)
      .concat(" ").concat(findAddressComponent(addressComponents, AddressComponentType.ROUTE));

    String city = findAddressComponent(addressComponents, AddressComponentType.LOCALITY);

    State state = State.valueOfName(findAddressComponent(addressComponents,
      AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1));

    String zipCode = findAddressComponent(addressComponents, AddressComponentType.POSTAL_CODE);

    return Address.newAddress(street, city, state, zipCode);
  }

  protected String findAddressComponent(AddressComponent[] addressComponents,
      AddressComponentType... componentTypes) {

    for (AddressComponent addressComponent : addressComponents) {
      if (Arrays.asList(addressComponent.types).stream().anyMatch(
          (element) -> Arrays.asList(componentTypes).contains(element))) {

        return addressComponent.longName;
      }
    }

    throw new IllegalStateException(String.format("no address component of types [%s] found",
      Arrays.toString(componentTypes)));
  }

  protected LatLng toLatitudeLongitude(Address address) {
    return toLatitudeLongitude(address.getLocation());
  }

  protected LatLng toLatitudeLongitude(Point point) {
    return new LatLng(point.getX(), point.getY());
  }

  protected String toString(Address address) {
    return String.format("%1$s, %2$s, %3$s %4$s", address.getStreet(),
      address.getCity(), address.getState(), address.getZipCode());
  }

  @Override
  public Point geocode(Address address) {
    String stringAddress = toString(address);

    try {
      GeoApiContext context = newGeoApiContext();

      GeocodingResult[] results = GeocodingApi.geocode(context, stringAddress).await();

      if (!ObjectUtils.isEmpty(results)) {
        GeocodingResult result = results[0];
        return new Point(result.geometry.location.lat, result.geometry.location.lng);
      }

      throw new GoogleMapsApiGeocodingException(String.format(
        "geographic coordinates (latitude/longitude) for address [%s] not found", stringAddress));
    }
    catch (Exception e) {
      throw new GoogleMapsApiGeocodingException(String.format(
        "failed to convert address [%s] into geographic coordinates (latitude/longitude)", stringAddress), e);
    }
  }

  @Override
  public Address reverseGeocode(Point location) {
    try {
      GeoApiContext context = newGeoApiContext();

      GeocodingResult[] results = GeocodingApi.reverseGeocode(context, toLatitudeLongitude(location)).await();

      if (!ObjectUtils.isEmpty(results)) {
        GeocodingResult result = results[0];

        return toAddress(result.addressComponents);
      }

      throw new GoogleMapsApiGeocodingException(String.format(
        "address for geographic coordinates (latitude/longitude) [%s] not found", location));
    }
    catch (Exception e) {
      throw new GoogleMapsApiGeocodingException(String.format(
        "failed to reverse geocode geographic coordinates (latitude/longitude) [%s] to a physical address",
          location), e);
    }
  }
}
