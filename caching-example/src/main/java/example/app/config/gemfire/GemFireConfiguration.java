package example.app.config.gemfire;

import com.gemstone.gemfire.cache.Cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.geo.Point;

import example.app.model.Address;

/**
 * The GemFireConfiguration class...
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @since 1.0.0
 */
@Configuration
@PeerCacheApplication(name = "CachingExampleApplication")
@SuppressWarnings("unused")
public class GemFireConfiguration {

  @Bean(name = "AddressToLatitudeLongitude")
  public LocalRegionFactoryBean<Address, Point> addressToLatitudeLongitudeRegion(Cache gemfireCache) {
    LocalRegionFactoryBean<Address, Point> addressToLatitudeLongitudeRegion = new LocalRegionFactoryBean<>();

    addressToLatitudeLongitudeRegion.setCache(gemfireCache);
    addressToLatitudeLongitudeRegion.setClose(false);
    addressToLatitudeLongitudeRegion.setPersistent(false);

    return addressToLatitudeLongitudeRegion;
  }

  @Bean(name = "LatitudeLongitudeToAddress")
  public LocalRegionFactoryBean<Address, Point> latitudeLongitudeToAddressRegion(Cache gemfireCache) {
    LocalRegionFactoryBean<Address, Point> addressToLatitudeLongitudeRegion = new LocalRegionFactoryBean<>();

    addressToLatitudeLongitudeRegion.setCache(gemfireCache);
    addressToLatitudeLongitudeRegion.setClose(false);
    addressToLatitudeLongitudeRegion.setPersistent(false);

    return addressToLatitudeLongitudeRegion;
  }
}
