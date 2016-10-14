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

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.GemFireCache;

/**
 * The App class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class App implements Runnable {

  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    newApp(args).run();
  }

  protected static App newApp(String[] args) {
    return new App(args);
  }

  private final String[] args;

  protected App(String[] args) {
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
      .set("log-level", logLevel())
      .create();
  }

  protected String applicationName() {
    return ("GemFirePeerCache" + App.class.getSimpleName());
  }

  protected String logLevel() {
    return System.getProperty("gemfire.log-level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  protected String waitForUserInput(String message) {
    System.err.println(message);
    Scanner in = new Scanner(System.in);
    return in.next();
  }
}
