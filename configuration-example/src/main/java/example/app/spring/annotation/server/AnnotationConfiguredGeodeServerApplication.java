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

package example.app.spring.annotation.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableCacheServer;
import org.springframework.data.gemfire.config.annotation.EnableCacheServers;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;

import example.app.config.server.EchoServerApplicationConfiguration;

/**
 * The {@link AnnotationConfiguredGeodeServerApplication} class is a {@link SpringBootApplication} that configures
 * and bootstraps an Apache Geode server JVM process using Spring Data for Apache Geode's new Java annotation-based
 * configuration model.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableCacheServer
 * @see org.springframework.data.gemfire.config.annotation.EnableCacheServers
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @see example.app.config.server.EchoServerApplicationConfiguration
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "AnnotationConfiguredGeodeServerApplication",
	port = AnnotationConfiguredGeodeServerApplication.GEODE_CACHE_SERVER_PORT)
@EnableCacheServers(servers = { @EnableCacheServer(port = 12480), @EnableCacheServer(port = 40404) })
@EnableLocator
@EnableManager
@Import(EchoServerApplicationConfiguration.class)
@SuppressWarnings("all")
public class AnnotationConfiguredGeodeServerApplication {

	public static final int GEODE_CACHE_SERVER_PORT = 11235;

	public static void main(String[] args) {
		SpringApplication.run(AnnotationConfiguredGeodeServerApplication.class, args);
	}
}
