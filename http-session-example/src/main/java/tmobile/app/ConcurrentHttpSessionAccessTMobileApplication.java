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

package tmobile.app;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.spring.session.gemfire.SpyingGemFireOperationsSessionRepository;
import org.spring.session.gemfire.serialization.data.SpyingDataSerializableSessionSerializer;
import org.spring.session.logging.SystemOutLogger;
import org.spring.session.time.TimeUtils;
import org.spring.session.utils.RequestUtils;
import org.spring.session.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The {@link ConcurrentHttpSessionAccessTMobileApplication} class...
 *
 * @author T-Mobile
 * @author John Blum
 * @see javax.servlet.http.HttpSession
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
 * @see org.springframework.stereotype.Controller
 * @see org.springframework.web.bind.annotation.CrossOrigin
 * @since 1.0.0
 */
@SpringBootApplication
@Controller
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8181" }, allowCredentials = "true")
@SuppressWarnings("unused")
public class ConcurrentHttpSessionAccessTMobileApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConcurrentHttpSessionAccessTMobileApplication.class, args);
  }

  @GetMapping("/ping")
  public @ResponseBody String ping() {
    return "PONG";
  }

  @GetMapping("/")
  public String gotoHomePage(HttpSession httpSession) {
    System.out.printf("Goto Home Page - Session ID [%s]%n", httpSession.getId());
    return "test";
  }

  @GetMapping("/countKeys")
  public @ResponseBody int countKeys(HttpSession session) {

    List<String> sessionAttributeNames = SessionUtils.getAttributeNames(session);

    log("%s - Count Keys%n",
      toString(session, sessionAttributeNames), session.getId(), sessionAttributeNames);

    return sessionAttributeNames.size();
  }

  @GetMapping("/getKeys")
  public @ResponseBody String getKeys(HttpSession session) {

    List<String> sessionAttributeNames = SessionUtils.getAttributeNames(session);

    log("%s - Get Keys%n",
      toString(session, sessionAttributeNames), session.getId(), sessionAttributeNames);

    return "KEYS ".concat(sessionAttributeNames.toString());
  }

  @GetMapping("/getKeysValues")
  public @ResponseBody String getKeysValues(HttpSession session) {

    List<String> sessionAttributeNames = SessionUtils.getAttributeNames(session);

    log("%s - Get Keys/Values%n",
      toString(session, sessionAttributeNames), session.getId(), sessionAttributeNames);

    Map<String, String> sessionAttributeNamesValues = sessionAttributeNames.stream()
      .collect(Collectors.toMap(Function.identity(),
        attributeName -> String.valueOf(session.getAttribute(attributeName))));

    return "KEYS/VALUES ".concat(sessionAttributeNamesValues.toString());
  }

  @GetMapping("/insertKeys")
  public String insertKeys(HttpSession session, @RequestParam(name = "number", defaultValue = "0") int number) {

    Set<String> keyNames = new HashSet<>();

    for (int count = number; count > 0; count--) {

      String attributeName = String.format("KEY-%d", count);
      String attributeValue = String.format("VALUE-%d", count);

      session.setAttribute(attributeName, attributeValue);
      keyNames.add(attributeName);
    }

    return String.format("Inserted [%s]", keyNames);
  }

  @GetMapping("/insertKey0")
  public @ResponseBody String insertKeyZero(HttpSession session) {

    log("%s - Insert [KEY-0]%n", toString(session));

    session.setAttribute("KEY-0", "VALUE-0");

    return "Inserted [KEY-0]";
  }

  @GetMapping("/insertKey1")
  public @ResponseBody String insertKeyOne(HttpSession session) {

    log("%s - Insert [KEY-1]%n", toString(session));

    session.setAttribute("KEY-1", "VALUE-1");

    return "Inserted [KEY-1]";
  }

  @GetMapping("/insertKey123")
  public @ResponseBody String insertKeyOneTwoThree(HttpSession session) {

    log("%s - Insert [KEY-1, KEY-2, KEY-3]%n", toString(session));

    return insertKeys(session, 3);
  }

  @GetMapping("/insertKey4")
  public @ResponseBody String insertKeyFour(HttpSession session) {

    log("%s - Insert [KEY-4]%n", toString(session));

    session.setAttribute("KEY-4", "VALUE-4");

    return "Inserted [KEY-4]";
  }

  @GetMapping("/removeKey")
  public @ResponseBody String removeKey(HttpSession session, @RequestParam("key") String key) {

    log("%s - Remove [%s]%n", toString(session), key);

    session.removeAttribute(key);

    return String.format("Removed [%s]", key);
  }

  @GetMapping(value = "/removeKeys")
  public @ResponseBody String removeKeys(HttpSession session) {

    List<String> sessionAttributeNames = SessionUtils.getAttributeNames(session);

    log("%s - Remove All Keys%n", toString(session, sessionAttributeNames));

    sessionAttributeNames.forEach(session::removeAttribute);

    return String.format("Removed [%s]", sessionAttributeNames);
  }

  private void log(String message, Object... args) {

    SystemOutLogger.log(String.format("[%s; id-%s] - %s",
      TimeUtils.formattedTimestamp(), RequestUtils.resolveServerIdentifier(), message), args);
  }


  private long toSeconds(long milliseconds) {
    return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
  }

  private String toString(HttpSession session) {
    return toString(session, SessionUtils.getAttributeNames(session));
  }

  private String toString(HttpSession session, Collection<String> attributeNames) {
    return String.format("Using Session [%s], Last Accessed [%s], Dirty [%s] and Attributes [%s]",
      session.getId(), session.getLastAccessedTime(), "?", attributeNames);
  }

  @ClientCacheApplication(copyOnRead = true, subscriptionEnabled = true)
  @EnableGemFireHttpSession(
    clientRegionShortcut = ClientRegionShortcut.PROXY,
    poolName = "DEFAULT",
    regionName = "Sessions",
    sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
  )
  @Profile("spring-session-gemfire")
  static class GeodeHttpSessionConfiguration { }


  @ClientCacheApplication(copyOnRead = true, subscriptionEnabled = true)
  @EnableGemFireHttpSession(
    clientRegionShortcut = ClientRegionShortcut.PROXY,
    exposeConfigurationAsProperties = true,
    poolName = "DEFAULT",
    regionName = "Sessions",
    sessionSerializerBeanName = "spySessionSerializer"
  )
  @Profile("spring-session-gemfire-debug")
  static class DebuggingGeodeHttpSessionConfiguration {

    @Bean
    SpyingDataSerializableSessionSerializer spySessionSerializer() {
      return new SpyingDataSerializableSessionSerializer();
    }

    @Bean("sessionRepository")
    SpyingGemFireOperationsSessionRepository spySessionRepository(
        @Qualifier("sessionRegionTemplate") GemfireOperations gemfireOperations,
        @Value("${spring.session.data.gemfire.session.expiration.max-inactive-interval-seconds}") int maxInactiveIntervalInSeconds) {

      SpyingGemFireOperationsSessionRepository sessionRepository =
        new SpyingGemFireOperationsSessionRepository(gemfireOperations);

      sessionRepository.setMaxInactiveIntervalInSeconds(maxInactiveIntervalInSeconds);
      sessionRepository.setUseDataSerialization(true);

      return sessionRepository;
    }
  }


  @EnableRedisHttpSession
  @Profile("spring-session-redis")
  static class RedisHttpSessionConfiguration { }

}
