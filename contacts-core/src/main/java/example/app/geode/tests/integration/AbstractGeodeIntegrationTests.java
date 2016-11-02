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

package example.app.geode.tests.integration;

import static org.cp.elements.lang.concurrent.ThreadUtils.waitFor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.cp.elements.io.IOUtils;
import org.cp.elements.lang.Assert;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.net.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractGeodeIntegrationTests} class is an abstrat base class for developing
 * Apache Geode Integration Tests.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.lang.Process
 * @see org.apache.geode.cache.client.ClientCache
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractGeodeIntegrationTests {

  protected static final boolean DEFAULT_DAEMON_THREAD = true;

  protected static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

  protected static final long DEFAULT_WAIT_TIME_DURATION = TimeUnit.SECONDS.toMillis(20);
  protected static final long DEFAULT_WAIT_TIME_INTERVAL = 500L; // milliseconds

  protected static final File JAVA_HOME = new File(System.getProperty("java.home"));
  protected static final File JAVA_EXE = new File(new File(JAVA_HOME, "bin"), "java");
  protected static final File USER_HOME = new File(System.getProperty("user.home"));
  protected static final File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));

  protected static final Logger logger = LoggerFactory.getLogger(AbstractGeodeIntegrationTests.class);

  protected static final Set<String> JAVA_LAUNCHER_OPTIONS = new HashSet<>();

  protected static final String DEFAULT_LABEL = "";
  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

  static {
    JAVA_LAUNCHER_OPTIONS.addAll(Arrays.asList(
      "-d32", "-d64", "-server",
      "-cp", "-classpath",
      "-D",
      "-verbose",
      "-version", "-showversion",
      "-jre-restrict-search", "-no-jre-restrict-search",
      "-?", "-help",
      "-X",
      "-ea", "-enableassertions",
      "-da", "-disableassertions",
      "-esa", "-enablesystemassertions",
      "-dsa", "-disablesystemassertions",
      "-agentlib", "-agentpath", "-javaagent",
      "-splash"
    ));
  }

  /* (non-Javadoc) */
  protected static String asApplicationName(Class<?> type) {
    return type.getSimpleName();
  }

  /* (non-Javadoc) */
  protected static String asDirectoryName(Class<?> type) {
    return String.format("%1$s-%2$s", asApplicationName(type).toLowerCase(),
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")));
  }

  /* (non-Javadoc) */
  protected static File createDirectory(String pathname) {
    File directory = new File(WORKING_DIRECTORY, pathname);

    Assert.isTrue(directory.exists() || directory.mkdirs(), "Failed to create directory [%s]", directory);

    directory.deleteOnExit();

    return directory;
  }

  /* (non-Javadoc) */
  protected static String logLevel() {
    return System.getProperty("gemfire.log.level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  /* (non-Javadoc) */
  protected static Process run(Class type, String... args) throws IOException {
    return run(asDirectoryName(type), type, args);
  }

  /* (non-Javadoc) */
  protected static Process run(String pathname, Class type, String... args) throws IOException {
    return run(createDirectory(pathname), type, args);
  }

  /* (non-Javadoc) */
  protected static Process run(File workingDirectory, Class type, String... args) throws IOException {
    if (Boolean.FALSE.equals(Boolean.getBoolean("run.manual"))) {
      String[] javaCommandLine = buildJavaCommandLine(type, args);

      logger.info("Java command-line {}", Arrays.toString(javaCommandLine));

      Process process = new ProcessBuilder()
        .command(javaCommandLine)
        .directory(workingDirectory)
        .redirectErrorStream(true)
        .start();

      Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(process)));

      newThread("Process STDOUT Stream Reader", newInputStreamLoggingRunnable("FORK", process.getInputStream())).start();

      return process;
    }

    return null;
  }

  /* (non-Javadoc) */
  protected static String[] buildJavaCommandLine(Class<?> type, String... args) {
    List<String> javaCommandLine = new ArrayList<>();

    javaCommandLine.add(JAVA_EXE.getAbsolutePath());
    javaCommandLine.add("-server");
    javaCommandLine.add("-ea");
    javaCommandLine.addAll(extractJvmArguments(args));
    javaCommandLine.add("-classpath");
    javaCommandLine.add(System.getProperty("java.class.path"));
    javaCommandLine.add(type.getName());
    javaCommandLine.addAll(extractProgramArguments(args));

    return javaCommandLine.toArray(new String[javaCommandLine.size()]);
  }

  /* (non-Javadoc) */
  protected static List<String> extractJvmArguments(String... args) {
    List<String> jvmArguments = new ArrayList<>(args.length);

    for (String arg : args) {
      if (isJvmArgument(arg)) {
        jvmArguments.add(arg);
      }
    }

    return jvmArguments;
  }

  /* (non-Javadoc) */
  protected static boolean isJvmArgument(String arg) {
    if (StringUtils.hasText(arg)) {
      for (String javaOption : JAVA_LAUNCHER_OPTIONS) {
        if (isNotServerOrEnableAssertionsOption(javaOption) && arg.startsWith(javaOption)) {
          return true;
        }
      }
    }

    return false;
  }

  /* (non-Javadoc) */
  protected static boolean isNotJvmArgument(String arg) {
    return !isJvmArgument(arg);
  }

  /* (non-Javadoc) */
  protected static boolean isNotServerOrEnableAssertionsOption(String option) {
    return !isServerOrEnableAssertionsOption(option);
  }

  /* (non-Javadoc) */
  protected static boolean isServerOrEnableAssertionsOption(String option) {
    return (option.startsWith("-server") || option.startsWith("-ea") || option.startsWith("-enableassertions"));
  }

  /* (non-Javadoc) */
  protected static List<String> extractProgramArguments(String... args) {
    List<String> programArguments = new ArrayList<>(args.length);

    for (String arg : args) {
      if (isNotServerOrEnableAssertionsOption(arg) && isNotJvmArgument(arg)) {
        programArguments.add(arg);
      }
    }

    return programArguments;
  }

  protected static Runnable newInputStreamLoggingRunnable(InputStream in) {
    return newInputStreamLoggingRunnable(DEFAULT_LABEL, in);
  }

  protected static Runnable newInputStreamLoggingRunnable(String label, InputStream in) {
    return () -> {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      try {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          logger.info("{} - {}", label, line);
        }
      }
      catch (IOException ignore) {
      }
      finally {
        IOUtils.close(reader);
      }
    };
  }

  protected static Thread newThread(String name, Runnable runnable) {
    return newThread(name, runnable, DEFAULT_DAEMON_THREAD, DEFAULT_THREAD_PRIORITY);
  }

  protected static Thread newThread(String name, Runnable runnable, boolean daemon, int priority) {
    Thread thread = new Thread(runnable, name);

    thread.setDaemon(daemon);
    thread.setPriority(priority);
    thread.setUncaughtExceptionHandler((t, throwable) -> logger.warn("Thread {} threw an unhandled exception {}",
      t.getName(), throwable));

    return thread;
  }
  protected static boolean stop(Process process) {
    return stop(process, DEFAULT_WAIT_TIME_DURATION);
  }

  protected static boolean stop(Process process, long duration) {
    if (isAlive(process)) {
      process.destroy();
      waitFor(duration).checkEvery(DEFAULT_WAIT_TIME_INTERVAL).on(() -> isRunning(process));

      if (isAlive(process)) {
        process.destroyForcibly();
      }

      return isRunning(process);
    }

    return true;
  }

  /* (non-Javadoc) */
  protected static boolean isAlive(Process process) {
    return (process != null && process.isAlive());
  }

  /* (non-Javadoc) */
  protected static boolean isRunning(Process process) {
    try {
      if (process != null) {
        process.exitValue();
      }

      return false;
    }
    catch (IllegalThreadStateException ignore) {
      return true;
    }
  }

  /* (non-Javadoc) */
  protected static boolean waitForGeodeCacheClientToClose() {
    return waitForGeodeCacheClientToClose(DEFAULT_WAIT_TIME_DURATION);
  }

  /* (non-Javadoc) */
  protected static boolean waitForGeodeCacheClientToClose(long duration) {
    try {
      ClientCache clientCache = ClientCacheFactory.getAnyInstance();

      if (clientCache != null) {
        clientCache.close();
        return waitFor(duration).checkEvery(DEFAULT_WAIT_TIME_INTERVAL).on(clientCache::isClosed);
      }
    }
    catch (CacheClosedException ignore) {
    }

    return true;
  }

  /* (non-Javadoc) */
  protected static boolean waitForServerToStart(Process process, String host, int port) {
    return waitForServerToStart(process, host, port, DEFAULT_WAIT_TIME_DURATION);
  }

  /* (non-Javadoc) */
  protected static boolean waitForServerToStart(Process process, String host, int port, long duration) {
    if (isAlive(process)) {
      AtomicBoolean connected = new AtomicBoolean(false);

      waitFor(duration).checkEvery(DEFAULT_WAIT_TIME_INTERVAL).on(() -> {
        Socket socket = null;

        try {
          if (!connected.get()) {
            socket = new Socket(host, port);
            connected.set(true);
          }
        }
        catch (IOException ignore) {
        }
        finally {
          NetworkUtils.close(socket);
        }

        return connected.get();
      });

      return connected.get();
    }

    return false;
  }
}
