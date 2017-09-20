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

package demo.geode;

import java.util.Scanner;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.GemFireCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeodeApiExampleApplication} class is an example Apache Geode application bootstrapping a Geode Server,
 * peer cache instance, distributed system (cluster) member using Geode's API.
 *
 * @author John Blum
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.GemFireCache
 * @since 1.0.0
 */
public final class GeodeApiExampleApplication implements Runnable {

  private static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    newGeodeApiExampleApplication(args).run();
  }

  public static GeodeApiExampleApplication newGeodeApiExampleApplication(String[] args) {
    return new GeodeApiExampleApplication(args);
  }

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String[] args;

  private GeodeApiExampleApplication(String[] args) {
    this.args = args;
  }

  @Override
  public void run() {
    run(this.args);
  }

  protected void run(String[] args) {
    gemfireCache(args);
    waitForUserInput("Press <enter> to exit");
  }

  @SuppressWarnings({ "unchecked", "unused" })
  protected <T extends GemFireCache> T gemfireCache(String[] args) {

    return (T) new CacheFactory()
      .set("name", applicationName())
      .set("mcast-port", "0")
      .set("locators", "")
      .set("log-level", logLevel())
      .create();
  }

  private String applicationName() {
    return GeodeApiExampleApplication.class.getSimpleName();
  }

  private String logLevel() {
    return System.getProperty("gemfire.log-level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  private String waitForUserInput(String message) {
    logger.info(message);
    Scanner in = new Scanner(System.in);
    return in.next();
  }
}
