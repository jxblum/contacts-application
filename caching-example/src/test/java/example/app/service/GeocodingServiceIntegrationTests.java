package example.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.CachingExampleConfiguration;
import example.app.model.Address;
import example.app.model.State;
import example.app.repo.GeocodingRepository;
import example.app.service.GeocodingServiceIntegrationTests.TestConfiguration;

/**
 * Test suite of test cases testing the contract and functionality of the {@link GeocodingService} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.geo.Point
 * @see example.app.config.gemfire.GemFireConfiguration
 * @see example.app.model.Address
 * @see example.app.service.GeocodingService
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CachingExampleConfiguration.class, TestConfiguration.class },
  webEnvironment = WebEnvironment.NONE)
public class GeocodingServiceIntegrationTests {

  protected static Address addressOne = Address.newAddress("100 Main St.", "Portland", State.OREGON, "12345")
    .with(newPoint(51.675213098d, -81.5478123d));

  protected static Address addressTwo = Address.newAddress("5050 Halfway Lane", "Portland", State.MAINE, "54321")
    .with(newPoint(77.676248091d, -42.084213579d));

  @Autowired
  @SuppressWarnings("unused")
  private GeocodingService geocodingService;

  @SuppressWarnings("unused")
  @Resource(name = "AddressToLatitudeLongitude")
  private Region<Address, Point> addressToLatitudeLongitude;

  @SuppressWarnings("unused")
  @Resource(name = "LatitudeLongitudeToAddress")
  private Region<Address, Point> latitudeLongitudeToAddress;

  protected static Point newPoint(double latitude, double longitude) {
    return new Point(latitude, longitude);
  }

  @Before
  public void setup() {
    addressToLatitudeLongitude.clear();
    latitudeLongitudeToAddress.clear();

    assertThat(addressToLatitudeLongitude.isEmpty()).isTrue();
    assertThat(latitudeLongitudeToAddress.isEmpty()).isTrue();
    assertThat(geocodingService.isCacheMiss()).isFalse();
  }

  @Test
  public void geocodeCachesSuccessfully() {
    assertThat(geocodingService.geocode(addressOne)).isEqualTo(addressOne.getLocation());
    assertThat(geocodingService.isCacheMiss()).isTrue();
    assertThat(geocodingService.geocode(addressOne)).isEqualTo(addressOne.getLocation());
    assertThat(geocodingService.isCacheMiss()).isFalse();
    assertThat(geocodingService.geocode(addressTwo)).isEqualTo(addressTwo.getLocation());
    assertThat(geocodingService.isCacheMiss()).isTrue();
  }

  @Test
  public void reverseGeocodeCachesSuccessfully() {
    assertThat(geocodingService.reverseGeocode(addressOne.getLocation())).isEqualTo(addressOne);
    assertThat(geocodingService.isCacheMiss()).isTrue();
    assertThat(geocodingService.reverseGeocode(addressOne.getLocation())).isEqualTo(addressOne);
    assertThat(geocodingService.isCacheMiss()).isFalse();
    assertThat(geocodingService.reverseGeocode(addressTwo.getLocation())).isEqualTo(addressTwo);
    assertThat(geocodingService.isCacheMiss()).isTrue();
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    @SuppressWarnings("unused")
    GeocodingRepository geocodingRepository() {
      GeocodingRepository mockGeocodingRepository = mock(GeocodingRepository.class);

      when(mockGeocodingRepository.geocode(addressOne)).thenReturn(addressOne.getLocation());
      when(mockGeocodingRepository.geocode(addressTwo)).thenReturn(addressTwo.getLocation());
      when(mockGeocodingRepository.reverseGeocode(addressOne.getLocation())).thenReturn(addressOne);
      when(mockGeocodingRepository.reverseGeocode(addressTwo.getLocation())).thenReturn(addressTwo);

      return mockGeocodingRepository;
    }
  }
}
