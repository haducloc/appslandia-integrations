// The MIT License (MIT)
// Copyright © 2015 Loc Ha

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.appslandia.integrations.rocksdb;

import java.nio.charset.StandardCharsets;

import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteOptions;

/**
 *
 * @author Loc Ha
 *
 */
public class RocksUtils {

  // Utilities to get rid of Eclipse's warnings when using Builder pattern on
  // AutoClose objects
  // Resource leak: '<unassigned Closeable value>' is never closed

  public static RocksCloseable newRocksCloseable() {
    return new RocksCloseable();
  }

  public static Options newOptions() {
    return new Options();
  }

  public static DBOptions newDBOptions() {
    return new DBOptions();
  }

  public static TransactionDBOptions newTranDBOptions() {
    return new TransactionDBOptions();
  }

  public static ColumnFamilyOptions newCfOptions() {
    return new ColumnFamilyOptions();
  }

  public static ReadOptions newReadOptions() {
    return new ReadOptions();
  }

  public static WriteOptions newWriteOptions() {
    return new WriteOptions();
  }

  public static FlushOptions newFlushOptions() {
    return new FlushOptions();
  }

  public static byte[] marshal(String keyOrName) {
    return keyOrName.getBytes(StandardCharsets.UTF_8);
  }

  public static String unmarshal(byte[] keyOrName) {
    return new String(keyOrName, StandardCharsets.UTF_8);
  }

  public static String getCfDescriptorName(ColumnFamilyDescriptor descriptor) {
    return unmarshal(descriptor.getName());
  }

  public static String getCfHandleName(ColumnFamilyHandle handle) {
    try {
      return unmarshal(handle.getName());

    } catch (RocksDBException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static ColumnFamilyDescriptor newCfDescriptor(String cfName, ColumnFamilyOptions options) {
    return (options != null) ? new ColumnFamilyDescriptor(marshal(cfName), options)
        : new ColumnFamilyDescriptor(marshal(cfName));
  }

  public static void closeQuietly(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception ignore) {
      }
    }
  }
}
