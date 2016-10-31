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

package example.app.shiro.realm.support;

import java.security.MessageDigest;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.cp.elements.lang.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import example.app.shiro.realm.SecurityRepositoryAuthorizingRealm;

/**
 * The {@link CredentialsMatcher} class is used by the {@link SecurityRepositoryAuthorizingRealm}
 * as the default user authenticating, credential matching algorithm.
 *
 * @author John Blum
 * @see org.apache.shiro.authc.credential.CredentialsMatcher
 * @see org.springframework.core.convert.converter.Converter
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CredentialsMatcher implements org.apache.shiro.authc.credential.CredentialsMatcher {

  protected static final Converter<Object, byte[]> DEFAULT_OBJECT_TO_BYTE_ARRAY_CONVERTER =
    ObjectToByteArrayConverter.INSTANCE;

  private Converter<Object, byte[]> objectToByteArray;

  /**
   * Factory method to construct an uninitialized instance of the {@link CredentialsMatcher} class.
   *
   * @return a new instance of the {@link CredentialsMatcher}.
   * @see example.app.shiro.realm.support.CredentialsMatcher
   */
  public static CredentialsMatcher newCredentialsMatcher() {
    return new CredentialsMatcher();
  }

  /**
   * Sets the Spring {@link Converter} used to convert an {@link Object} to a {@code byte[]}.
   *
   * @param objectToByteArray {@link Object} to {@code byte[]} {@link Converter}.
   * @see org.springframework.core.convert.converter.Converter
   */
  public void setConverter(Converter<Object, byte[]> objectToByteArray) {
    this.objectToByteArray = objectToByteArray;
  }

  /**
   * Returns a reference to the Spring {@link Converter} used to convert an {@link Object} to a {@code byte[]}.
   *
   * @return a reference to the {@link Object} to {@code byte[]} {@link Converter}.
   * @see org.springframework.core.convert.converter.Converter
   */
  @SuppressWarnings("unchecked")
  protected Converter<Object, byte[]> getConverter() {
    return ObjectUtils.defaultIfNull(this.objectToByteArray, DEFAULT_OBJECT_TO_BYTE_ARRAY_CONVERTER);
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
    return match(info.getCredentials(), token.getCredentials());
  }

  /**
   * Determines whether the {@code actualCredentials} match the {@code expectedCredentials}.
   *
   * @param expectedCredentials expected credentials stored for a valid user account.
   * @param actualCredentials actual credentials provided by a Subject during login.
   * @return a boolean value indicating whether the credentials match.
   * @see java.security.MessageDigest#isEqual(byte[], byte[])
   * @see #getConverter()
   */
  public boolean match(Object expectedCredentials, Object actualCredentials) {
    Converter<Object, byte[]> converter = getConverter();

    return MessageDigest.isEqual(converter.convert(expectedCredentials), converter.convert(actualCredentials));
  }

  /**
   * Configures this {@link CredentialsMatcher} with a given Spring {@link Converter} to convert an {@link Object}
   * to a {@code byte[]}.
   *
   * @param objectToByteArray {@link Object} to {@code byte[]} {@link Converter}.
   * @return this {@link CredentialsMatcher}.
   * @see #setConverter(Converter)
   */
  public CredentialsMatcher with(Converter<Object, byte[]> objectToByteArray) {
    setConverter(objectToByteArray);
    return this;
  }
}
