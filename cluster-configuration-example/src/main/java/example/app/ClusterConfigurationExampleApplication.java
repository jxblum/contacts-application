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

package example.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Scanner;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

/**
 * The ClusterConfigurationExampleApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = "example\\.app\\.geode\\.security\\..*"))
@SuppressWarnings("unused")
public class ClusterConfigurationExampleApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(ClusterConfigurationExampleApplication.class, args);
  }

  @Resource(name = "ClientDefinedRegion")
  private Region<String, String> clientDefinedRegion;

  @Override
  public void run(String... args) throws Exception {

    assertThat(putThenGet("myKey", "test")).isEqualTo("test");
    assertThat(putThenGet("myKey", "testing")).isEqualTo("testing");
    assertThat(putThenGet("myKey", "tested")).isEqualTo("tested");

    promptToExit();
  }

  private String putThenGet(String key, String value) {
    this.clientDefinedRegion.put(key, value);
    return this.clientDefinedRegion.get(key);
  }

  private void promptToExit() {
    System.err.printf("Press <ENTER> to exit.%n");
    Scanner scanner = new Scanner(System.in);
    scanner.nextLine();
  }
}
