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

package example.app.config.gemfire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.client.ClientCache;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.gemfire.config.GemfireConstants;

/**
 * The GemFireDependsOnBeanFactoryPostProcessor class is a Spring {@link BeanFactoryPostProcessor} that adds
 * dependencies to a GemFire cache bean defined in the Spring {@link org.springframework.context.ApplicationContext}.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.GemFireCache
 * @see com.gemstone.gemfire.cache.client.ClientCache
 * @since 1.0.0
 */
public class GemFireDependsOnBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private final List<String> dependsOnBeanNames = new ArrayList<>();

	@SafeVarargs
	static <T> List<T> asList(T... array) {
		return (array != null ? Arrays.asList(array) : Collections.emptyList());
	}

	public GemFireDependsOnBeanFactoryPostProcessor add(String... dependsOnBeanNames) {
		this.dependsOnBeanNames.addAll(Arrays.asList(dependsOnBeanNames));
		return this;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {

		for (String beanName : gemfireCacheBeanNames(beanFactory)) {
			BeanDefinition bean = beanFactory.getBeanDefinition(beanName);
			List<String> beanDependencies = new ArrayList<>(asList(bean.getDependsOn()));
			beanDependencies.addAll(dependsOnBeanNames);
			bean.setDependsOn(beanDependencies.toArray(new String[beanDependencies.size()]));
		}
	}

	String[] gemfireCacheBeanNames(ConfigurableListableBeanFactory beanFactory) {
		List<String> gemfireCacheBeanNames = new ArrayList<>();

		for (Class<?> gemfireCacheType : Arrays.asList(GemFireCache.class, Cache.class, ClientCache.class)) {
			gemfireCacheBeanNames.addAll(Arrays.asList(beanFactory.getBeanNamesForType(gemfireCacheType, true, false)));
		}

		if (gemfireCacheBeanNames.isEmpty()) {
			gemfireCacheBeanNames.add(GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME);
		}

		return gemfireCacheBeanNames.toArray(new String[gemfireCacheBeanNames.size()]);
	}
}
