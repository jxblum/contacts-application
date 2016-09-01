package example.app.repo.support;

import org.springframework.data.geo.Point;

import example.app.model.Address;
import example.app.repo.GeocodingRepository;

/**
 * The GeocodingRepositoryAdapter class is an implementation of {@link GeocodingRepository} providing
 * default unsupported (no-op) implementations of the geocoding data access operations.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see example.app.repo.GeocodingRepository
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class GeocodingRepositoryAdapter implements GeocodingRepository {

  /* (non-Javadoc) */
  @Override
  public Point geocode(Address address) {
    throw new UnsupportedOperationException("geocoding addresses is not supported");
  }

  /* (non-Javadoc) */
  @Override
  public Address reverseGeocode(Point point) {
    throw new UnsupportedOperationException(
      "reverse geocoding geographic coordinates (e.g. Latitude/Longitude) is not supported");
  }
}
