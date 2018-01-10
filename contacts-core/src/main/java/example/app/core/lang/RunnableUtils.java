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

package example.app.core.lang;

import static example.app.core.lang.RunnableUtils.SleepDeprivedException.newSleepDeprivedException;

import java.util.Optional;
import java.util.Scanner;

/**
 * {@link RunnableUtils} is an abstract utility class for working with {@link Runnable Runnables}.
 *
 * @author John Blum
 * @see java.lang.Runnable
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class RunnableUtils {

  public static void promptPressEnterToExit() {
    System.err.println("Press <enter> to exit...");
    new Scanner(System.in).nextLine();
  }

  public static boolean safeRunWithPause(long milliseconds, Runnable runner) {
    boolean slept = safeSleep(milliseconds);
    runner.run();
    return slept;
  }

  public static boolean safeRunWithPauseUninterrupted(long milliseconds, Runnable runner) {
    boolean slept = safeSleepUninterrupted(milliseconds);
    runner.run();
    return slept;
  }

  public static <T> T safeRunReturningValueWithPause(long milliseconds, ReturningRunnable<T> runner) {

    return Optional.ofNullable(runner.run())
      .filter(it -> safeSleep(milliseconds))
      .orElseThrow(() ->
        newSleepDeprivedException("Failed to wait the required number of milliseconds [%d]", milliseconds));
  }

  public static <T> T safeRunReturningValueWithoutThrowing(long milliseconds, ReturningRunnable<T> runner) {
    safeSleepUninterrupted(milliseconds);
    return runner.run();
  }

  public static boolean safeSleep(long milliseconds) {

    try {
      Thread.sleep(milliseconds);
      return true;
    }
    catch (InterruptedException cause) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  public static boolean safeSleepUninterrupted(long milliseconds) {

    boolean success = true;

    long timeout = (System.currentTimeMillis() + milliseconds);

    while (System.currentTimeMillis() < timeout) {
      success = safeSleep(milliseconds);
    }

    return success;
  }

  public static Optional<Long> timedRun(Runnable runner) {
    long t0 = System.currentTimeMillis();
    runner.run();
    return Optional.of(System.currentTimeMillis() - t0);
  }

  @FunctionalInterface
  public interface ReturningRunnable<T> {
    T run();
  }

  public static class SleepDeprivedException extends RuntimeException {

    public static SleepDeprivedException newSleepDeprivedException(String message, Object... args) {
      return new SleepDeprivedException(String.format(message, args));
    }

    public static SleepDeprivedException newSleepDeprivedException(Throwable cause, String message, Object... args) {
      return new SleepDeprivedException(String.format(message, args), cause);
    }

    public SleepDeprivedException() {
    }

    public SleepDeprivedException(String message) {
      super(message);
    }

    public SleepDeprivedException(Throwable cause) {
      super(cause);
    }

    public SleepDeprivedException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
