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

/**
 * The ClusterConfigurationExampleApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ClusterConfigurationExampleApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(ClusterConfigurationExampleApplication.class, args);
  }

  @Resource(name = "ClientDefinedRegion")
  private Region<String, String> clientDefinedRegion;

  @Override
  public void run(String... args) throws Exception {

    assertThat(this.clientDefinedRegion.put("myKey", "test")).isNull();
    assertThat(this.clientDefinedRegion.put("myKey", "testing")).isEqualTo("test");
    assertThat(this.clientDefinedRegion.put("myKey", "tested")).isEqualTo("tested");
    assertThat(this.clientDefinedRegion.get("myKey")).isEqualTo("tested");

    promptToExit();
  }

  private void promptToExit() {
    System.err.printf("Press <ENTER> to exit.%n");
    Scanner scanner = new Scanner(System.in);
    scanner.nextLine();
  }
}
