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
package example.app.spring.beans;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

/**
 * A {@link Runnable}, {@link PostConstruct} Spring bean.
 *
 * @author John Blum
 * @see java.lang.Runnable
 * @see javax.annotation.PostConstruct
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class RunnablePostConstructableBean implements Runnable {

  private final Runnable runnable;

  public RunnablePostConstructableBean(Runnable runnable) {

    Assert.notNull(runnable, "Runnable is required");

    this.runnable = runnable;
  }

  protected Optional<Runnable> getRunnable() {
    return Optional.ofNullable(this.runnable);
  }

  @PostConstruct
  public void run() {
    getRunnable().ifPresent(Runnable::run);
  }
}
