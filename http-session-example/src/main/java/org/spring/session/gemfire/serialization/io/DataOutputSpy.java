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

package org.spring.session.gemfire.serialization.io;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/**
 * The DataOutputSpy class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DataOutputSpy implements DataOutput {

  public static DataOutputSpy from(DataOutput out) {
    return new DataOutputSpy(out);
  }

  private final DataOutput delegate;

  private final List<Object> writes = Collections.synchronizedList(new ArrayList<>());

  private DataOutputSpy(DataOutput out) {

    Assert.notNull(out, "DataOutput is required");

    this.delegate = out;
  }

  protected DataOutput getDelegate() {
    return this.delegate;
  }

  public List<Object> getWrites() {
    return Collections.unmodifiableList(this.writes);
  }

  @Override
  public void write(byte[] bytes) throws IOException {
    getDelegate().write(bytes);

  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    getDelegate().write(bytes, offset, length);
  }

  @Override
  public void write(int bytes) throws IOException {
    getDelegate().write(bytes);
  }

  @Override
  public void writeBoolean(boolean value) throws IOException {
    getDelegate().writeBoolean(value);
  }

  @Override
  public void writeChar(int value) throws IOException {
    getDelegate().writeChar(value);
  }

  @Override
  public void writeByte(int value) throws IOException {
    getDelegate().writeByte(value);
  }

  @Override
  public void writeShort(int value) throws IOException {
    getDelegate().writeShort(value);
  }

  @Override
  public void writeInt(int value) throws IOException {
    getDelegate().writeInt(value);
  }

  @Override
  public void writeLong(long value) throws IOException {
    this.writes.add(value);
    getDelegate().writeLong(value);
  }

  @Override
  public void writeFloat(float value) throws IOException {
    getDelegate().writeFloat(value);
  }

  @Override
  public void writeDouble(double value) throws IOException {
    getDelegate().writeDouble(value);
  }

  @Override
  public void writeBytes(String value) throws IOException {
    getDelegate().writeBytes(value);
  }

  @Override
  public void writeChars(String value) throws IOException {
    getDelegate().writeChars(value);
  }

  @Override
  public void writeUTF(String value) throws IOException {
    this.writes.add(value);
    getDelegate().writeUTF(value);
  }
}
