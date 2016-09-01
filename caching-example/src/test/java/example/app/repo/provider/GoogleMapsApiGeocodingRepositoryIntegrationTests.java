package example.app.repo.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.geo.Point;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.CachingExampleConfiguration;
import example.app.model.Address;
import example.app.model.State;
import example.app.service.GeocodingService;

/**
 * Test suite of test cases testing the contract and functionality of the {@link GoogleMapsApiGeocodingRepository}
 * data access object (DAO).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.geo.Point
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.config.CachingExampleConfiguration
 * @see example.app.model.Address
 * @see example.app.repo.provider.GoogleMapsApiGeocodingRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CachingExampleConfiguration.class, webEnvironment = WebEnvironment.NONE)
public class GoogleMapsApiGeocodingRepositoryIntegrationTests {

  @Autowired
  @SuppressWarnings("unused")
  private GeocodingService geocodingService;

  protected Point newPoint(double latitude, double longitude) {
    return new Point(latitude, longitude);
  }

  @Test
  public void gecodeAddressIsSuccessful() {
    Address address = Address.newAddress("1600 Amphitheatre Parkway", "Mountain View", State.CALIFORNIA, "94043");
    Point latitudeLongitude = newPoint(37.4223669, -122.084358d);

    assertThat(geocodingService.geocode(address)).isEqualTo(latitudeLongitude);
  }

  @Test
  public void reverseGeocodeLatitudeLongitudeIsSuccessful() {
    Address address = Address.newAddress("1600 Amphitheatre Parkway", "Mountain View", State.CALIFORNIA, "94043");
    Point latitudeLongitude = newPoint(37.4223669, -122.084358d);

    assertThat(geocodingService.reverseGeocode(latitudeLongitude)).isEqualTo(address);
  }
}
