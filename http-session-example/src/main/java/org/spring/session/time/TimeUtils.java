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

package org.spring.session.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The TimeUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class TimeUtils {

  public static final DateTimeFormatter DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("YYYY-MM-dd  HH:mm:ss  SSS");

  public static String formattedTimestamp() {
    return LocalDateTime.now().format(DATE_TIME_FORMATTER);
  }

  public static long milliTimestamp() {
    return System.currentTimeMillis();
  }

  public static long nanoTimestamp() {
    return System.nanoTime();
  }

  public static long timestamp() {
    return milliTimestamp();
  }
}
