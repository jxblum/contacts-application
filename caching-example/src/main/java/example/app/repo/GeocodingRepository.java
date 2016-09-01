package example.app.repo;

import org.springframework.data.geo.Point;

import example.app.model.Address;

/**
 * The GeocodingRepository class is a Spring {@link org.springframework.stereotype.Repository} for performing
 * geocoding data access operations.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
public interface GeocodingRepository {

  Point geocode(Address address);

  Address reverseGeocode(Point point);

}
