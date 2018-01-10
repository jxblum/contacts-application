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

package example.app.geode.cache.util;

import javax.swing.plaf.synth.Region;

import org.apache.geode.cache.GemFireCache;

/**
 * {@link CacheUtils} is an abstract utility class encapsulating functionality common
 * to all types of Apache Geode caches.
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CacheUtils {

  public static boolean close(GemFireCache gemfireCache) {

    try {
      if (gemfireCache != null) {
        gemfireCache.close();
      }

      return true;
    }
    catch (Exception ignore) {
      return false;
    }
  }

  public static String toRegionPath(String regionName) {
    return String.format("%1$s%2$s", Region.SEPARATOR, regionName);
  }
}
