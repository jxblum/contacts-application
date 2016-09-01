package example.app.config;

import com.gemstone.gemfire.cache.Cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.support.GemfireCacheManager;

import example.app.config.gemfire.GemFireConfiguration;
import example.app.repo.GeocodingRepository;
import example.app.repo.provider.GoogleMapsApiGeocodingRepository;
import example.app.service.GeocodingService;

/**
 * The CachingExampleConfiguration class is the Contacts Application, Caching Example Spring {@link Configuration} class
 * to enable caching using Spring Data Geode along with Apache Geode as the [JSR-107] caching provider.
 *
 * @author John Blum
 * @see example.app.config.gemfire.GemFireConfiguration
 * @see example.app.repo.GeocodingRepository
 * @see example.app.repo.provider.GoogleMapsApiGeocodingRepository
 * @see example.app.service.GeocodingService
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.support.GemfireCacheManager
 * @since 1.0.0
 */
@Configuration
@EnableCaching
@Import(GemFireConfiguration.class)
@SuppressWarnings("unused")
public class CachingExampleConfiguration {

  @Bean
  public GemfireCacheManager cacheManager(Cache gemfireCache) {
    GemfireCacheManager cacheManager = new GemfireCacheManager();
    cacheManager.setCache(gemfireCache);
    return cacheManager;
  }

  @Bean
  public GeocodingRepository geocodingRepository() {
    return new GoogleMapsApiGeocodingRepository();
  }

  @Bean
  public GeocodingService geocodingService() {
    return new GeocodingService(geocodingRepository());
  }
}
