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

package example.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpSession;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The ConcurrentHttpSessionAccessExampleApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@Controller
@SuppressWarnings("unused")
public class ConcurrentHttpSessionAccessExampleApplication {

  private static final AtomicLong identifierSequence = new AtomicLong(0L);

  private static final String IDENTIFIER_PREFIX =
    System.getProperty("app.id.prefix", UUID.randomUUID().toString());

  private static final String ATTRIBUTE_VALUE_REQUEST_PARAMETER_NAME = "attributeValue";
  private static final String INDEX_TEMPLATE_VIEW_NAME = "index";
  private static final String PING_RESPONSE = "PONG";
  private static final String SESSION_ATTRIBUTES_ATTRIBUTE_NAME = "sessionAttributes";
  private static final String SESSION_ID_ATTRIBUTE_NAME = "sessionId";

  private final Semaphore semaphore = new Semaphore(0, false);

  public static void main(String[] args) {
    SpringApplication.run(ConcurrentHttpSessionAccessExampleApplication.class, args);
  }

  private String newIdentifier() {
    return String.format("%1$s-%2$s", IDENTIFIER_PREFIX, identifierSequence.incrementAndGet());
  }

  @ExceptionHandler
  @ResponseBody
  public String errorHandler(Throwable error) {
    StringWriter writer = new StringWriter();
    error.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  @GetMapping("/ping")
  @ResponseBody
  public String ping() {
    return PING_RESPONSE;
  }

  // WARNING: this is NOT Thread-safe since this method does not properly protect against the "compound operation" as a
  // single, indivisible, atomic operation.  However, it does make a best effort to minimize the effects of
  // a race condition and is reliable enough for this example test.
  @GetMapping("/semaphore/release")
  @ResponseBody
  public int releaseSemaphore() {

    if (this.semaphore.hasQueuedThreads()) {
      while (this.semaphore.availablePermits() < 1) {
        this.semaphore.release();
      }
    }

    return this.semaphore.availablePermits();
  }

  @GetMapping("/session/attribute/count")
  @ResponseBody
  public long countSessionAttributes(HttpSession session) {
    return countAttributes(session);
  }

  @GetMapping("/session/view")
  public String listSessionAttributes(HttpSession session, ModelMap model) {

    model.addAttribute(SESSION_ATTRIBUTES_ATTRIBUTE_NAME, getAttributes(session));

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  @GetMapping("/session/updateThenBlock")
  public String blockAfterSessionAttributeUpdate(HttpSession session, ModelMap model,
      @RequestParam(name = ATTRIBUTE_VALUE_REQUEST_PARAMETER_NAME) String attributeValue) throws Exception {

    model.addAttribute(SESSION_ATTRIBUTES_ATTRIBUTE_NAME,
      getAttributes(setAttribute(session, String.valueOf(newIdentifier()),
        defaultStringIfValueNotSet(attributeValue))));

    this.semaphore.acquire();

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  @GetMapping("/session/blockThenUpdate")
  public String blockBeforeSessionAttributeUpdate(HttpSession session, ModelMap model,
      @RequestParam(name = ATTRIBUTE_VALUE_REQUEST_PARAMETER_NAME) String attributeValue) throws Exception {

    this.semaphore.acquire();

    model.addAttribute(SESSION_ATTRIBUTES_ATTRIBUTE_NAME,
      getAttributes(setAttribute(session, String.valueOf(newIdentifier()),
        defaultStringIfValueNotSet(attributeValue))));

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  @GetMapping("/session/update")
  public String nonBlockingSessionAttributeUpdate(HttpSession session, ModelMap model,
      @RequestParam(name = ATTRIBUTE_VALUE_REQUEST_PARAMETER_NAME, required = false) String attributeValue) {

    model.addAttribute(SESSION_ATTRIBUTES_ATTRIBUTE_NAME,
      getAttributes(setAttribute(session, String.valueOf(newIdentifier()),
        defaultStringIfValueNotSet(attributeValue))));

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  @GetMapping("/session/remove")
  public String removeSessionAttribute(HttpSession session,
      @RequestParam(name = ATTRIBUTE_VALUE_REQUEST_PARAMETER_NAME, required = false) String attributeValue) {

    getAttributeNames(session).stream()
      .filter(attributeName -> isNotSet(attributeValue) || session.getAttribute(attributeName).equals(attributeValue))
      .forEach(session::removeAttribute);

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  private HttpSession setAttribute(HttpSession session, String attributeName, String attributeValue) {

    session.setAttribute(attributeName, attributeValue);

    return session;
  }

  private Set<String> getAttributeNames(HttpSession session) {

    return StreamSupport.stream(toIterable(session.getAttributeNames()).spliterator(), false)
      .collect(Collectors.toSet());
  }

  private Map<String, String> getAttributes(HttpSession session) {

    Map<String, String> sessionAttributes = new HashMap<>();

    getAttributeNames(session).forEach(attributeName ->
      sessionAttributes.put(attributeName, String.valueOf(session.getAttribute(attributeName))));

    sessionAttributes.put(SESSION_ID_ATTRIBUTE_NAME, session.getId());

    return sessionAttributes;
  }

  private long countAttributes(HttpSession session) {
    return getAttributeNames(session).size();
  }

  private String defaultStringIfValueNotSet(String value) {
    return StringUtils.hasText(value) ? value : randomString();
  }

  private boolean isNotSet(String value) {
    return !isSet(value);
  }

  private boolean isSet(String value) {
    return StringUtils.hasText(value);
  }

  private String randomString() {

    String uuid = UUID.randomUUID().toString();

    return uuid.substring(0, 3) + uuid.substring(uuid.length() - 3);
  }

  private <T> Iterable<T> toIterable(Enumeration<T> enumeration) {

    return () -> Optional.ofNullable(enumeration)
      .map(CollectionUtils::toIterator)
      .orElseGet(Collections::emptyIterator);
  }

  @ClientCacheApplication(copyOnRead = true, subscriptionEnabled = true)
  @EnableGemFireHttpSession(
    clientRegionShortcut = ClientRegionShortcut.PROXY,
    poolName = "DEFAULT",
    regionName = "Sessions",
    sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
  )
  @Profile("spring-session-gemfire")
  static class GeodeHttpSessionConfiguration {

    @Bean
    CookieSerializer nonEncodingCookieSerializer() {

      DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

      cookieSerializer.setUseBase64Encoding(false);

      return cookieSerializer;
    }
  }

  @EnableRedisHttpSession
  @Profile("spring-session-redis")
  static class RedisHttpSessionConfiguration { }

}
