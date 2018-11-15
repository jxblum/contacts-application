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

package example.app.geode.cache.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpSession;

import org.apache.shiro.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The SessionExampleApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication(subscriptionEnabled = true)
@EnableGemFireHttpSession(
  regionName = "Sessions",
  poolName = "DEFAULT",
  sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
)
@EnablePdx
@Controller
@SuppressWarnings("unused")
public class SessionExampleApplication {

  private static final String INDEX_TEMPLATE_VIEW_NAME = "index";
  private static final String PING_RESPONSE = "PONG";
  private static final String REQUEST_COUNT_ATTRIBUTE_NAME = "requestCount";

  public static void main(String[] args) {
    SpringApplication.run(SessionExampleApplication.class, args);
  }

  @GetMapping("/")
  public String listCustomers(HttpSession session, ModelMap modelMap) {

    modelMap.addAttribute("sessionAttributes", attributes(updateRequestCount(session)));

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  @PostMapping("/customers")
  public String addCustomer(HttpSession session, ModelMap modelMap,
      @RequestParam(name = "customerId", required = false) String customerId,
      @RequestParam(name = "customerName", required = false) String customerName) {

    modelMap.addAttribute("sessionAttributes",
      attributes(setAttribute(updateRequestCount(session), customerId, Customer.newCustomer(customerName))));

    return INDEX_TEMPLATE_VIEW_NAME;
  }

  @ExceptionHandler
  @ResponseBody
  public String errorHandler(Throwable error) {
    StringWriter writer = new StringWriter();
    error.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  @GetMapping("/exception")
  public String throwException() {
    throw new RuntimeException("test");
  }

  @GetMapping("/ping")
  @ResponseBody
  public String ping() {
    return PING_RESPONSE;
  }

  @SuppressWarnings("all")
  HttpSession updateRequestCount(HttpSession session) {

    synchronized (session) {
      Integer currentRequestCount = (Integer) session.getAttribute(REQUEST_COUNT_ATTRIBUTE_NAME);
      session.setAttribute(REQUEST_COUNT_ATTRIBUTE_NAME, nullSafeIncrement(currentRequestCount));
      return session;
    }
  }

  Integer nullSafeIncrement(Integer value) {
    return nullSafeIntValue(value) + 1;
  }

  int nullSafeIntValue(Number value) {
    return Optional.ofNullable(value).map(Number::intValue).orElse(0);
  }

  HttpSession setAttribute(HttpSession session, String attributeName, Object attributeValue) {

    if (isSet(attributeName, attributeValue)) {
      session.setAttribute(attributeName, attributeValue);
    }

    return session;
  }

  boolean isSet(Object... values) {

    boolean set = true;

    for (Object value : values) {
      set &= value != null && StringUtils.hasText(value.toString());
    }

    return set;
  }

  Map<String, String> attributes(HttpSession session) {

    Map<String, String> sessionAttributes = new HashMap<>();

    StreamSupport.stream(toIterable(session.getAttributeNames()).spliterator(), false)
      .forEach(attributeName -> sessionAttributes.put(attributeName,
        String.valueOf(session.getAttribute(attributeName))));

    return sessionAttributes;
  }

  <T> Iterable<T> toIterable(Enumeration<T> enumeration) {

    return () -> Optional.ofNullable(enumeration)
      .map(CollectionUtils::toIterator)
      .orElseGet(Collections::emptyIterator);
  }

  @Data
  @EqualsAndHashCode
  @RequiredArgsConstructor(staticName = "newCustomer")
  static class Customer {

    @NonNull
    private String name;

    @Override
    public String toString() {
      return getName();
    }
  }
}
