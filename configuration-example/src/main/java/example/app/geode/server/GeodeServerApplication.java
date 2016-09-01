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

package example.app.geode.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.server.CacheServer;

import org.springframework.util.Assert;

import example.app.geode.cache.loader.EchoCacheLoader;

/**
 * The GeodeServerApplication class is a {@link Runnable} {@code main} class implementation that uses GemFire's
 * public, Java API to configure and bootstrap a GemFire Server in a JVM application process.
 *
 * @author John Blum
 * @see java.lang.Runnable
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.CacheFactory
 * @see com.gemstone.gemfire.cache.server.CacheServer
 * @see <a href="http://geode.incubator.apache.org/releases/latest/javadoc/index.html">Geode Java API</a>
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeServerApplication implements Runnable {

	protected static final int DEFAULT_GEMFIRE_CACHE_SERVER_MAX_CONNECTIONS = 50;
	protected static final int DEFAULT_GEMFIRE_CACHE_SERVER_MAX_TIME_BETWEEN_PINGS =
		intValue(TimeUnit.MINUTES.toMillis(5));
	protected static final int DEFAULT_GEMFIRE_CACHE_SERVER_PORT = 40404;
	protected static final int DEFAULT_GEMFIRE_JMX_MANAGER_PORT = 1099;

	protected static final String DEFAULT_GEMFIRE_CACHE_SERVER_BIND_ADDRESS = "localhost";
	protected static final String DEFAULT_GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS =
		DEFAULT_GEMFIRE_CACHE_SERVER_BIND_ADDRESS;
	protected static final String DEFAULT_GEMFIRE_JMX_MANAGER = "true";
	protected static final String DEFAULT_GEMFIRE_JMX_MANAGER_START = "true";
	protected static final String DEFAULT_GEMFIRE_EMBEDDED_LOCATOR_HOST_PORT = "localhost[10334]";
	protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

	public static void main(String[] args) {
		GeodeServerApplication.run(args);
	}

	protected static int intValue(Number value) {
		return value.intValue();
	}

	public static GeodeServerApplication newGeodeServerApplication(String[] args) {
		return new GeodeServerApplication(args);
	}

	public static GeodeServerApplication run(String[] args) {
		GeodeServerApplication geodeServerApplication = newGeodeServerApplication(args);
		geodeServerApplication.run();
		return geodeServerApplication;
	}

	private final AtomicBoolean cacheXmlDriven = new AtomicBoolean(false);

	private final List<String> arguments;

	/**
	 * Constructs an instance of the {@link GeodeServerApplication} class to configure and fork/launch
	 * an Apache Geode data server node.
	 *
	 * @param args {@link String} array of program arguments.
	 * @throws NullPointerException if the program arguments are null.
	 * @see #GeodeServerApplication(List)
	 */
	public GeodeServerApplication(String[] args) {
		this(Arrays.asList(args));
	}

	/**
	 * Constructs an instance of the {@link GeodeServerApplication} class to configure and fork/launch
	 * an Apache Geode data server node.
	 *
	 * @param args {@link List} of program arguments.
	 * @throws IllegalArgumentException if the program arguments are null.
	 */
	public GeodeServerApplication(List<String> args) {
		Assert.notNull(args, "Program argument must be be null");
		this.arguments = args;
	}

	/* (non-Javadoc) */
	@Override
	public void run() {
		run(arguments);
	}

	protected void run(List<String> arguments) {
		Cache gemfireCache = null;

		try {
			if (hasArguments(arguments)) {
				cacheXmlDriven.set(true);
				gemfireCache = gemfireCache(gemfireProperties(), arguments.get(0));
			}
			else {
				gemfireCache = gemfireCache(gemfireProperties());
			}

			gemfireCacheServer(gemfireCache);
			echoRegion(gemfireCache);
		}
		catch (Exception uhOh) {
			uhOh.printStackTrace(System.err);
			close(gemfireCache);
		}
	}

	boolean hasArguments(Object... arguments) {
		return (arguments != null && arguments.length > 0);
	}

	boolean hasArguments(List<?> arguments) {
		return (arguments != null && !arguments.isEmpty());
	}

	boolean close(GemFireCache cache) {
		try {
			if (cache != null) {
				cache.close();
				return true;
			}
		}
		catch (Exception ignore) {
		}

		return false;
	}

	String applicationName() {
		return String.format("%1$s%2$s", (cacheXmlDriven.get() ? "CacheXmlBased" : ""),
			GeodeServerApplication.class.getSimpleName());
	}

	boolean jmxManager() {
		return Boolean.parseBoolean(System.getProperty("gemfire.manager.enabled", DEFAULT_GEMFIRE_JMX_MANAGER));
	}

	int jmxManagerPort() {
		return Integer.getInteger("gemfire.manager.port", DEFAULT_GEMFIRE_JMX_MANAGER_PORT);
	}

	boolean jmxManagerStart() {
		return Boolean.parseBoolean(System.getProperty("gemfire.manager.start", DEFAULT_GEMFIRE_JMX_MANAGER_START));
	}

	String logLevel() {
		return System.getProperty("gemfire.log.level", DEFAULT_GEMFIRE_LOG_LEVEL);
	}

	String startLocator() {
		return System.getProperty("gemfire.locator.embedded.host-port", DEFAULT_GEMFIRE_EMBEDDED_LOCATOR_HOST_PORT);
	}

	Properties gemfireProperties() {
		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty("name", applicationName());
		gemfireProperties.setProperty("mcast-port", "0");
		gemfireProperties.setProperty("log-level", logLevel());
		gemfireProperties.setProperty("jmx-manager", String.valueOf(jmxManager()));
		gemfireProperties.setProperty("jmx-manager-port", String.valueOf(jmxManagerPort()));
		gemfireProperties.setProperty("jmx-manager-start", String.valueOf(jmxManagerStart()));
		gemfireProperties.setProperty("start-locator", startLocator());

		return gemfireProperties;
	}

	Cache gemfireCache(Properties gemfireProperties) {
		return new CacheFactory(gemfireProperties).create();
	}

	Cache gemfireCache(Properties gemfireProperties, String cacheXmlPathname) {
		return new CacheFactory(gemfireProperties).set("cache-xml-file", cacheXmlPathname).create();
	}

	CacheServer gemfireCacheServer(Cache gemfireCache) throws IOException {
		CacheServer gemfireCacheServer = gemfireCache.addCacheServer();

		gemfireCacheServer.setBindAddress(DEFAULT_GEMFIRE_CACHE_SERVER_BIND_ADDRESS);
		gemfireCacheServer.setHostnameForClients(DEFAULT_GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS);
		gemfireCacheServer.setMaxConnections(DEFAULT_GEMFIRE_CACHE_SERVER_MAX_CONNECTIONS);
		gemfireCacheServer.setMaximumTimeBetweenPings(DEFAULT_GEMFIRE_CACHE_SERVER_MAX_TIME_BETWEEN_PINGS);
		gemfireCacheServer.setPort(DEFAULT_GEMFIRE_CACHE_SERVER_PORT);
		gemfireCacheServer.start();

		return gemfireCacheServer;
	}

	Region<String, String> echoRegion(Cache gemfireCache) {
		RegionFactory<String, String> echoRegion = gemfireCache.createRegionFactory(RegionShortcut.PARTITION);

		echoRegion.setCacheLoader(EchoCacheLoader.getInstance());
		echoRegion.setKeyConstraint(String.class);
		echoRegion.setValueConstraint(String.class);

		return echoRegion.create("Echo");
	}
}
