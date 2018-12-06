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

package org.spring.session.utils;

import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The RequestUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class RequestUtils {

  public static Optional<RequestAttributes> getRequestAttributes() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes());
  }

  public static Optional<HttpServletRequest> getHttpServletRequest() {

    return getRequestAttributes()
      .filter(ServletRequestAttributes.class::isInstance)
      .map(ServletRequestAttributes.class::cast)
      .map(ServletRequestAttributes::getRequest);
  }

  public static Optional<HttpServletResponse> getHttpServletResponse() {

    return getRequestAttributes()
      .filter(ServletRequestAttributes.class::isInstance)
      .map(ServletRequestAttributes.class::cast)
      .map(ServletRequestAttributes::getResponse);
  }

  public static Optional<HttpSession> getHttpSession() {

    return getRequestAttributes()
      .filter(ServletRequestAttributes.class::isInstance)
      .map(ServletRequestAttributes.class::cast)
      .map(ServletRequestAttributes::getRequest)
      .map(HttpServletRequest::getSession);
  }

  public static Object resolveRequestIdentifier() {

    return getHttpServletRequest()
      .map(HttpServletRequest::getRequestURL)
      .map(Object::toString)
      .orElse(null);
  }

  public static Object resolveServerIdentifier() {

    return getHttpServletRequest()
      .map(ServletRequest::getServerPort)
      .orElse(null);
  }

  public static String resolveSessionId() {

    return getHttpSession()
      .map(HttpSession::getId)
      .filter(StringUtils::hasText)
      .orElse(null);
  }
}
