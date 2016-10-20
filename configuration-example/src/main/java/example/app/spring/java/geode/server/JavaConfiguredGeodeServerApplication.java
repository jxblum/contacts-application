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

package example.app.spring.java.geode.server;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.server.CacheServerFactoryBean;

import example.app.config.server.EchoServerApplicationConfiguration;

/**
 * The JavaConfiguredGeodeServerApplication class is a {@link SpringBootApplication} that configures and bootstraps
 * a Geode Server application JVM process using Spring Java-based configuration meta-data.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * @see com.gemstone.gemfire.cache.Cache
 * @see EchoServerApplicationConfiguration
 * @since 1.0.0
 */
@SpringBootApplication
@Import(EchoServerApplicationConfiguration.class)
@SuppressWarnings("unused")
public class JavaConfiguredGeodeServerApplication {

	protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

	public static void main(String[] args) {
		SpringApplication.run(JavaConfiguredGeodeServerApplication.class, args);
	}

	String applicationName() {
		return JavaConfiguredGeodeServerApplication.class.getSimpleName();
	}

	@Bean
	static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	Properties gemfireProperties(@Value("${gemfire.log.level:"+DEFAULT_GEMFIRE_LOG_LEVEL+"}") String logLevel,
			@Value("${gemfire.locator.embedded.host-port:localhost[10334]}") String startLocator,
			@Value("${gemfire.manager:true}") boolean jmxManager,
			@Value("${gemfire.manager.port:1099}") int jmxManagerPort,
			@Value("${gemfire.manager.start:false}") boolean jmxManagerStart) {

		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty("name", applicationName());
		gemfireProperties.setProperty("mcast-port", "0");
		gemfireProperties.setProperty("log-level", logLevel);
		gemfireProperties.setProperty("jmx-manager", String.valueOf(jmxManager));
		gemfireProperties.setProperty("jmx-manager-port", String.valueOf(jmxManagerPort));
		gemfireProperties.setProperty("jmx-manager-start", String.valueOf(jmxManagerStart));
		gemfireProperties.setProperty("start-locator", startLocator);

		return gemfireProperties;
	}

	@Bean
	CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
		CacheFactoryBean gemfireCache = new CacheFactoryBean();

		gemfireCache.setClose(true);
		gemfireCache.setProperties(gemfireProperties);

		return gemfireCache;
	}

	@Bean
	CacheServerFactoryBean gemfireCacheServer(Cache gemfireCache,
			@Value("${gemfire.cache.server.bind-address:localhost}") String bindAddress,
			@Value("${gemfire.cache.server.hostname-for-clients:localhost}") String hostnameForClients,
			@Value("${gemfire.cache.server.max-connections:50}") int maxConnections,
			@Value("${gemfire.cache.server.max-time-between-pings:300000}") int maxTimeBetweenPings,
			@Value("${gemfire.cache.server.port:40404}") int port) {

		CacheServerFactoryBean gemfireCacheServer = new CacheServerFactoryBean();

		gemfireCacheServer.setCache(gemfireCache);
		gemfireCacheServer.setAutoStartup(true);
		gemfireCacheServer.setBindAddress(bindAddress);
		gemfireCacheServer.setHostNameForClients(hostnameForClients);
		gemfireCacheServer.setMaxConnections(maxConnections);
		gemfireCacheServer.setMaxTimeBetweenPings(maxTimeBetweenPings);
		gemfireCacheServer.setPort(port);

		return gemfireCacheServer;
	}
}
