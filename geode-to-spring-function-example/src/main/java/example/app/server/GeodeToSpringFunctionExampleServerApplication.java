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

package example.app.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctions;

import example.app.server.function.SpellCheckerWithAutoCorrectFunction;

/**
 * The GeodeToSpringFunctionExampleServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "GeodeToSpringFunctionExample")
@EnableGemfireFunctions
@EnableLocator
@EnableManager(start = true)
@SuppressWarnings("unused")
public class GeodeToSpringFunctionExampleServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(GeodeToSpringFunctionExampleServerApplication.class, args);
  }

  @Bean
  SpellCheckerWithAutoCorrectFunction spellCheckerWithAutoCorrectFunction() {
    return new SpellCheckerWithAutoCorrectFunction();
  }
}
