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

package example.app.spring.annotation.geode.client;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableSsl;

import example.app.config.client.EchoClientApplicationConfiguration;
import example.app.spring.annotation.geode.server.AnnotationConfiguredGeodeServerApplication;

/**
 * The AnnotationConfiguredGeodeClientApplication class is a {@link SpringBootApplication} that configures and bootstraps
 * and Geode cache client application JVM process using Spring Data Geode's new and improved Java annotation-based
 * configuration approach.
 *
 * @author John Blum
 * @see org.springframework.boot.CommandLineRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see example.app.config.client.EchoClientApplicationConfiguration
 * @see AnnotationConfiguredGeodeServerApplication
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication(servers = { @ClientCacheApplication.Server(port = AnnotationConfiguredGeodeServerApplication.GEODE_CACHE_SERVER_PORT)})
@EnableSsl(components = { EnableSsl.Component.SERVER },
	keystore = "/Users/jblum/pivdev/springonePlatform-2016/configuration-example/etc/geode/security/trusted.keystore",
	keystorePassword = "s3cr3t",
	keystoreType = "JKS",
	truststore = "/Users/jblum/pivdev/springonePlatform-2016/configuration-example/etc/geode/security/trusted.keystore",
	truststorePassword = "s3cr3t")
@Import(EchoClientApplicationConfiguration.class)
@SuppressWarnings("all")
public class AnnotationConfiguredGeodeClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AnnotationConfiguredGeodeClientApplication.class, args);
	}

	@Resource(name = "Echo")
	private Region<String, String> echo;

	@Override
	public void run(String... args) throws Exception {
		assertThat(sendEchoRequest("Hello")).isEqualTo("Hello");
		assertThat(sendEchoRequest("TEST")).isEqualTo("TEST");
		assertThat(sendEchoRequest("Good-Bye")).isEqualTo("Good-Bye");
	}

	private String sendEchoRequest(String echoRequest) {
		String echoResponse = echo.get(echoRequest);
		System.err.printf("Client says [%1$s]; Server says [%2$s]%n", echoRequest, echoResponse);
		return echoResponse;
	}
}
