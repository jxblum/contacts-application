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

package example.app.spring.xml.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The {@link XmlConfiguredGeodeServerApplication} class is a {@link SpringBootApplication} that configures
 * and bootstraps an Apache Geode server JVM process using Spring Data for Apache Geodes's XML namespace.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.ImportResource
 * @since 1.0.0
 */
@SpringBootApplication
@ImportResource("spring-server-cache.xml")
public class XmlConfiguredGeodeServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmlConfiguredGeodeServerApplication.class, args);
	}
}
