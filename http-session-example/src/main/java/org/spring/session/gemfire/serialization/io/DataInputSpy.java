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

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/**
 * The DataInputSpy class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DataInputSpy implements DataInput {

  public static DataInputSpy from(DataInput in) {
    return new DataInputSpy(in);
  }

  private final DataInput delegate;

  private final List<Object> reads = Collections.synchronizedList(new ArrayList<>());

  private DataInputSpy(DataInput in) {

    Assert.notNull(in, "DataInput is required");

    this.delegate = in;
  }

  protected DataInput getDelegate() {
    return this.delegate;
  }

  public List<Object> getReads() {
    return Collections.unmodifiableList(this.reads);
  }

  @Override
  public void readFully(byte[] bytes) throws IOException {
    getDelegate().readFully(bytes);
  }

  @Override
  public void readFully(byte[] bytes, int offset, int length) throws IOException {
    getDelegate().readFully(bytes, offset, length);
  }

  @Override
  public int skipBytes(int number) throws IOException {
    return getDelegate().skipBytes(number);
  }

  @Override
  public boolean readBoolean() throws IOException {
    return getDelegate().readBoolean();
  }

  @Override
  public byte readByte() throws IOException {
    return getDelegate().readByte();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return getDelegate().readUnsignedByte();
  }

  @Override
  public char readChar() throws IOException {
    return getDelegate().readChar();
  }

  @Override
  public short readShort() throws IOException {
    return getDelegate().readShort();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return getDelegate().readUnsignedShort();
  }

  @Override
  public int readInt() throws IOException {
    return getDelegate().readInt();
  }

  @Override
  public long readLong() throws IOException {

    Long value = getDelegate().readLong();

    this.reads.add(value);

    return value;
  }

  @Override
  public float readFloat() throws IOException {
    return getDelegate().readFloat();
  }

  @Override
  public double readDouble() throws IOException {
    return getDelegate().readDouble();
  }

  @Override
  public String readLine() throws IOException {
    return getDelegate().readLine();
  }

  @Override
  public String readUTF() throws IOException {

    String utf = getDelegate().readUTF();

    this.reads.add(utf);

    return utf;
  }
}
