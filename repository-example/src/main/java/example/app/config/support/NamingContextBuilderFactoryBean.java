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

package example.app.config.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.spi.InitialContextFactoryBuilder;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

/**
 * The NamingContextBuilderFactoryBean class is a Spring {@link FactoryBean} responsible for binding named objects
 * to a {@link javax.naming.Context naming context} in the current environment.
 *
 * @author John Blum
 * @see javax.naming.spi.InitialContextFactoryBuilder
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.FactoryBean
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.mock.jndi.SimpleNamingContextBuilder
 * @since 1.0.0
 */
public class NamingContextBuilderFactoryBean implements FactoryBean<InitialContextFactoryBuilder>,
		InitializingBean, DisposableBean {

	private final Map<String, Object> boundObjects = new ConcurrentHashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		SimpleNamingContextBuilder namingContextBuilder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		for (Map.Entry<String, Object> boundObject : boundObjects.entrySet()) {
			namingContextBuilder.bind(boundObject.getKey(), boundObject.getValue());
		}
	}

	@Override
	public void destroy() throws Exception {
		SimpleNamingContextBuilder namingContextBuilder = SimpleNamingContextBuilder.getCurrentContextBuilder();

		if (namingContextBuilder != null) {
			namingContextBuilder.clear();
			namingContextBuilder.deactivate();
		}
	}

	@Override
	public InitialContextFactoryBuilder getObject() throws Exception {
		return SimpleNamingContextBuilder.getCurrentContextBuilder();
	}

	@Override
	public Class<?> getObjectType() {
		InitialContextFactoryBuilder namingContextBuilder = SimpleNamingContextBuilder.getCurrentContextBuilder();

		return (namingContextBuilder != null ? namingContextBuilder.getClass()
			: InitialContextFactoryBuilder.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@SuppressWarnings("all")
	public NamingContextBuilderFactoryBean bind(String name, Object obj) {
		boundObjects.put(name, obj);
		return this;
	}
}
