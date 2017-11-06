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

package example.app.geode.cache.client;

import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.query.CqAttributesFactory;
import org.apache.geode.cache.query.CqEvent;
import org.apache.geode.cache.query.CqListener;
import org.apache.geode.cache.query.CqQuery;
import org.apache.geode.cache.query.QueryService;

import example.app.geode.cache.client.model.Chat;

/**
 * The NativeChatBotClientApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class NativeChatBotClientApplication extends AbstractChatBotClientApplication implements Runnable {

  protected static final boolean DURABLE = true;

  protected static final String CACHE_SERVER_HOST =
    System.getProperty("example.continuous-query.gemfire.cache.server.host", "localhost");

  protected static final String CHAT_REGION_NAME = "Chat";

  protected static final int CACHE_SERVER_PORT =
    Integer.parseInt(System.getProperty("example.continuous-query.gemfire.cache.server.port", "40404"));

  protected static final String CONTINUOUS_QUERY = "SELECT * FROM /Chat";

  protected static final String POOL_NAME = "DEFAULT";

  public static void main(String[] args) {
    new NativeChatBotClientApplication(args).run();
  }

  private final String[] arguments;

  NativeChatBotClientApplication(String[] args) {
    this.arguments = Optional.ofNullable(args).orElseGet(() -> new String[0]);
  }

  @Override
  public void run() {
    run(this.arguments);
  }

  protected void run(String[] args) {

    try {
      registerContinuousQuery(chatRegion(registerShutdownHook(gemfireCache(gemfireProperties()), DURABLE)));
      promptToExit();
    }
    catch (Exception cause) {
      throw new RuntimeException("Failed to start GemFire native cache client application", cause);
    }
  }

  Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", NativeChatBotClientApplication.class.getSimpleName());
    gemfireProperties.setProperty("log-level", "config");
    gemfireProperties.setProperty("durable-client-id", UUID.randomUUID().toString());

    return gemfireProperties;
  }

  ClientCache gemfireCache(Properties gemfireProperties) {

    return new ClientCacheFactory(gemfireProperties)
      .addPoolServer(CACHE_SERVER_HOST, CACHE_SERVER_PORT)
      .setPoolSubscriptionEnabled(true)
      .create();
  }

  ClientCache registerShutdownHook(ClientCache clientCache, boolean keepAlive) {

    Runtime.getRuntime().addShutdownHook(new Thread(() ->
      Optional.ofNullable(clientCache).ifPresent(cache -> cache.close(keepAlive)),
      "GemFire ClientCache Shutdown Hook"));

    return clientCache;
  }

  Region<Long, Chat> chatRegion(ClientCache gemfireCache) {

    ClientRegionFactory<Long, Chat> chatRegionFactory =
      gemfireCache.createClientRegionFactory(ClientRegionShortcut.PROXY);

    chatRegionFactory.setKeyConstraint(Long.class);
    chatRegionFactory.setValueConstraint(Chat.class);

    return chatRegionFactory.create(CHAT_REGION_NAME);
  }

  Region<Long, Chat> registerContinuousQuery(Region<Long, Chat> chat) throws Exception {

    QueryService queryService = resolveQueryService(chat);

    CqAttributesFactory cqAttributesFactory = new CqAttributesFactory();

    cqAttributesFactory.addCqListener(new CqListenerAdapter() {

      @Override
      public void onEvent(CqEvent event) {
        log((Chat) event.getNewValue());
      }
    });

    CqQuery query = queryService.newCq("NativeChatReceiver", CONTINUOUS_QUERY,
      cqAttributesFactory.create(), DURABLE);

    query.execute();

    return chat;
  }

  QueryService resolveQueryService(Region<?, ?> region) {

    return Optional.ofNullable(PoolManager.find(POOL_NAME))
      .map(Pool::getQueryService)
      .orElseGet(() -> region.getRegionService().getQueryService());
  }

  void promptToExit() {
    System.err.println("Press <ENTER> to exit...");
    new Scanner(System.in).nextLine();
  }

  abstract class CqListenerAdapter implements CqListener {

    @Override
    public void onError(CqEvent event) {
      onEvent(event);
    }

    @Override
    public void close() {
    }
  }
}
