/*
 * Copyright 2018 the original author or authors.
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
package example.tests.spring.data.geode.cache;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionService;
import org.cp.elements.util.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * This Spring {@link ApplicationListener} assumes that when all application {@link Region Regions} are accessible
 * from {@link RegionService#getRegion(String)} that GemFire/Geode is ready for service across the entire cluster.
 *
 * This logic can be modified or tuned if that is not case and additional checks need to be performed to ensure
 * that the cluster, and all the GemFire/Geode objects managed in a Spring context, have been fully and properly
 * initialized before use by application (service) components.
 *
 * @author John Blum
 * @see java.util.concurrent.CountDownLatch
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.RegionService
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationListener
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeClusterReadyApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    ApplicationContext applicationContext = event.getApplicationContext();

    GemFireCache cache = applicationContext.getBean(GemFireCache.class);

    // NOTE: In reviewing the Apache Geode source code
    // (https://github.com/apache/geode/blob/rel/v1.9.0/geode-core/src/main/java/org/apache/geode/internal/cache/GemFireCacheImpl.java#L3333-L3366),
    // the very act of calling GemFireCache.rootRegions() ensures all Regions are fully initialized, actually.
    // Buuut, just in cases... o.O
    CollectionUtils.nullSafeSet(CollectionUtils.nullSafeSet(cache.rootRegions()))
      .forEach(region -> cache.getRegion(region.getFullPath()));

    applicationContext.publishEvent(new GeodeClusterReadyApplicationEvent(cache));
  }

  public class GeodeClusterReadyApplicationEvent extends ApplicationEvent {

    public GeodeClusterReadyApplicationEvent(Object source) {
      super(source);
    }
  }
}
