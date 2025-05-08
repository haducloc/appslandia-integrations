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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.DBOptions;
import org.rocksdb.FlushOptions;
import org.rocksdb.Holder;
import org.rocksdb.OptimisticTransactionDB;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.TransactionDB;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.TtlDB;
import org.rocksdb.WriteOptions;

import com.appslandia.common.utils.Arguments;
import com.appslandia.common.utils.ValueUtils;

/**
 *
 * @author Loc Ha
 *
 */
public class RocksManager implements AutoCloseable {

  static {
    RocksDB.loadLibrary();
  }

  public final static String DEFAULT_COLUMN_FAMILY = RocksUtils.unmarshal(RocksDB.DEFAULT_COLUMN_FAMILY);

  final RocksDB rocksDB;
  final Map<String, ColumnFamilyHandle> handleMap;
  final RocksCloseable rocksCloseable;

  private RocksManager(RocksDB rocksDB, List<ColumnFamilyHandle> handles, RocksCloseable rocksCloseable) {
    this.rocksDB = Arguments.notNull(rocksDB);
    Arguments.hasElements(handles);

    this.handleMap = Collections
        .unmodifiableMap(handles.stream().collect(Collectors.toMap(RocksUtils::getCfHandleName, h -> h)));
    this.rocksCloseable = Arguments.notNull(rocksCloseable);
  }

  public RocksDB getRocksDB() {
    return this.rocksDB;
  }

  // -------------------- RocksDB -------------------- //

  public void put(String key, byte[] value) throws RocksDBException {
    put(key, value, null, null);
  }

  public void put(String key, byte[] value, String columnFamilyName) throws RocksDBException {
    put(key, value, columnFamilyName, null);
  }

  public void put(String key, byte[] value, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);
    Arguments.notNull(value);

    put(RocksUtils.marshal(key), value, columnFamilyName, options);
  }

  public void put(byte[] key, byte[] value, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);
    Arguments.notNull(value);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      this.rocksDB.put(getHandle(columnFamilyName), key, value);
    } else {
      this.rocksDB.put(getHandle(columnFamilyName), options, key, value);
    }
  }

  public byte[] get(String key) throws RocksDBException {
    return get(key, null, null);
  }

  public byte[] get(String key, String columnFamilyName) throws RocksDBException {
    return get(key, columnFamilyName, null);
  }

  public byte[] get(String key, String columnFamilyName, ReadOptions options) throws RocksDBException {
    Arguments.notNull(key);

    return get(RocksUtils.marshal(key), columnFamilyName, options);
  }

  public byte[] get(byte[] key, String columnFamilyName, ReadOptions options) throws RocksDBException {
    Arguments.notNull(key);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      return this.rocksDB.get(getHandle(columnFamilyName), key);
    } else {
      return this.rocksDB.get(getHandle(columnFamilyName), options, key);
    }
  }

  public boolean keyExists(String key) throws RocksDBException {
    return keyExists(key, null, null);
  }

  public boolean keyExists(String key, String columnFamilyName) throws RocksDBException {
    return keyExists(key, columnFamilyName, null);
  }

  public boolean keyExists(String key, String columnFamilyName, ReadOptions options) throws RocksDBException {
    Arguments.notNull(key);

    return keyExists(RocksUtils.marshal(key), columnFamilyName, options);
  }

  public boolean keyExists(byte[] key, String columnFamilyName, ReadOptions options) throws RocksDBException {
    Arguments.notNull(key);

    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      return this.rocksDB.keyExists(getHandle(columnFamilyName), key);
    } else {
      return this.rocksDB.keyExists(getHandle(columnFamilyName), options, key);
    }
  }

  public boolean keyMayExist(String key) throws RocksDBException {
    return keyMayExist(key, null, null, null);
  }

  public boolean keyMayExist(String key, Holder<byte[]> valueHolder) throws RocksDBException {
    return keyMayExist(key, valueHolder, null, null);
  }

  public boolean keyMayExist(String key, Holder<byte[]> valueHolder, String columnFamilyName) throws RocksDBException {
    return keyMayExist(key, valueHolder, columnFamilyName, null);
  }

  public boolean keyMayExist(String key, Holder<byte[]> valueHolder, String columnFamilyName, ReadOptions options)
      throws RocksDBException {
    Arguments.notNull(key);

    return keyMayExist(RocksUtils.marshal(key), valueHolder, columnFamilyName, options);
  }

  public boolean keyMayExist(byte[] key, Holder<byte[]> valueHolder, String columnFamilyName, ReadOptions options)
      throws RocksDBException {
    Arguments.notNull(key);

    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      return this.rocksDB.keyMayExist(getHandle(columnFamilyName), key, valueHolder);
    } else {
      return this.rocksDB.keyMayExist(getHandle(columnFamilyName), options, key, valueHolder);
    }
  }

  public void merge(String key, byte[] value) throws RocksDBException {
    merge(key, value, null, null);
  }

  public void merge(String key, byte[] value, String columnFamilyName) throws RocksDBException {
    merge(key, value, columnFamilyName, null);
  }

  public void merge(String key, byte[] value, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);

    merge(RocksUtils.marshal(key), value, columnFamilyName, options);
  }

  public void merge(byte[] key, byte[] value, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);
    Arguments.notNull(value);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      this.rocksDB.merge(getHandle(columnFamilyName), key, value);
    } else {
      this.rocksDB.merge(getHandle(columnFamilyName), options, key, value);
    }
  }

  public void delete(String key) throws RocksDBException {
    delete(key, null, null);
  }

  public void delete(String key, String columnFamilyName) throws RocksDBException {
    delete(key, columnFamilyName, null);
  }

  public void delete(String key, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);

    delete(RocksUtils.marshal(key), columnFamilyName, options);
  }

  public void delete(byte[] key, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      this.rocksDB.delete(getHandle(columnFamilyName), key);
    } else {
      this.rocksDB.delete(getHandle(columnFamilyName), options, key);
    }
  }

  public void deleteRange(String fromKey, String toKey) throws RocksDBException {
    deleteRange(fromKey, toKey, null, null);
  }

  public void deleteRange(String fromKey, String toKey, String columnFamilyName) throws RocksDBException {
    deleteRange(fromKey, toKey, columnFamilyName, null);
  }

  public void deleteRange(String fromKey, String toKey, String columnFamilyName, WriteOptions options)
      throws RocksDBException {
    Arguments.notNull(fromKey);
    Arguments.notNull(toKey);

    deleteRange(RocksUtils.marshal(fromKey), RocksUtils.marshal(toKey), columnFamilyName, options);
  }

  public void deleteRange(byte[] fromKey, byte[] toKey, String columnFamilyName, WriteOptions options)
      throws RocksDBException {
    Arguments.notNull(fromKey);
    Arguments.notNull(toKey);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      this.rocksDB.deleteRange(getHandle(columnFamilyName), fromKey, toKey);
    } else {
      this.rocksDB.deleteRange(getHandle(columnFamilyName), options, fromKey, toKey);
    }
  }

  public void singleDelete(String key) throws RocksDBException {
    singleDelete(key, null, null);
  }

  public void singleDelete(String key, String columnFamilyName) throws RocksDBException {
    Arguments.notNull(key);

    singleDelete(key, columnFamilyName, null);
  }

  public void singleDelete(String key, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);

    singleDelete(RocksUtils.marshal(key), columnFamilyName, options);
  }

  public void singleDelete(byte[] key, String columnFamilyName, WriteOptions options) throws RocksDBException {
    Arguments.notNull(key);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    if (options == null) {
      this.rocksDB.singleDelete(getHandle(columnFamilyName), key);
    } else {
      this.rocksDB.singleDelete(getHandle(columnFamilyName), options, key);
    }
  }

  public <K, V> RocksEntryIterator<K, V> newRocksKeyIterator(RocksMarshaller<K> keyMarshaller) {
    Arguments.notNull(keyMarshaller);

    return newRocksEntryIterator(keyMarshaller, null, null, null, null, null, null);
  }

  public <K, V> RocksEntryIterator<K, V> newRocksEntryIterator(RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller) {
    Arguments.notNull(keyMarshaller);

    return newRocksEntryIterator(keyMarshaller, valueMarshaller, null, null, null, null, null);
  }

  public <K, V> RocksEntryIterator<K, V> newRocksEntryIterator(RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller, K fromKey) {
    Arguments.notNull(keyMarshaller);

    return newRocksEntryIterator(keyMarshaller, valueMarshaller, fromKey, null, null, null, null);
  }

  public <K, V> RocksEntryIterator<K, V> newRocksEntryIterator(RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller, K fromKey, Function<K, Boolean> toKeyMatcher) {
    Arguments.notNull(keyMarshaller);

    return newRocksEntryIterator(keyMarshaller, valueMarshaller, fromKey, toKeyMatcher, null, null, null);
  }

  public <K, V> RocksEntryIterator<K, V> newRocksEntryIterator(RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller, K fromKey, Function<K, Boolean> toKeyMatcher,
      Function<K, Boolean> keyFilter) {
    Arguments.notNull(keyMarshaller);

    return newRocksEntryIterator(keyMarshaller, valueMarshaller, fromKey, toKeyMatcher, keyFilter, null, null);
  }

  public <K, V> RocksEntryIterator<K, V> newRocksEntryIterator(RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller, K fromKey, Function<K, Boolean> toKeyMatcher, Function<K, Boolean> keyFilter,
      String columnFamilyName) {
    Arguments.notNull(keyMarshaller);

    return newRocksEntryIterator(keyMarshaller, valueMarshaller, fromKey, toKeyMatcher, keyFilter, columnFamilyName,
        null);
  }

  public <K, V> RocksEntryIterator<K, V> newRocksEntryIterator(RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller, K fromKey, Function<K, Boolean> toKeyMatcher, Function<K, Boolean> keyFilter,
      String columnFamilyName, ReadOptions options) {
    Arguments.notNull(keyMarshaller);

    return new RocksEntryIterator<>(newRocksIterator(columnFamilyName, options), keyMarshaller, valueMarshaller,
        fromKey, toKeyMatcher, keyFilter);
  }

  public RocksIterator newRocksIterator() {
    return newRocksIterator(null, null);
  }

  public RocksIterator newRocksIterator(String columnFamilyName) {
    return newRocksIterator(columnFamilyName, null);
  }

  public RocksIterator newRocksIterator(String columnFamilyName, ReadOptions options) {
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    return (options != null) ? this.rocksDB.newIterator(getHandle(columnFamilyName), options)
        : this.rocksDB.newIterator(getHandle(columnFamilyName));
  }

  public void compactRange() throws RocksDBException {
    compactRange(null);
  }

  public void compactRange(String columnFamilyName) throws RocksDBException {
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    this.rocksDB.compactRange(getHandle(columnFamilyName));
  }

  public void flush(FlushOptions options) throws RocksDBException {
    flush(options, null);
  }

  public void flush(FlushOptions options, String columnFamilyName) throws RocksDBException {
    Arguments.notNull(options);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    this.rocksDB.flush(options, getHandle(columnFamilyName));
  }

  public String getProperty(String property) throws RocksDBException {
    return getProperty(property, null);
  }

  public String getProperty(String property, String columnFamilyName) throws RocksDBException {
    Arguments.notNull(property);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    return this.rocksDB.getProperty(getHandle(columnFamilyName), property);
  }

  public long getLongProperty(String property) throws RocksDBException {
    return getLongProperty(property, null);
  }

  public long getLongProperty(String property, String columnFamilyName) throws RocksDBException {
    Arguments.notNull(property);
    columnFamilyName = ValueUtils.valueOrAlt(columnFamilyName, DEFAULT_COLUMN_FAMILY);

    return this.rocksDB.getLongProperty(getHandle(columnFamilyName), property);
  }

  // -------------------- Extended DB -------------------- //

  public TtlDB getTtlDB() {
    if (!(this.rocksDB instanceof TtlDB)) {
      throw new IllegalStateException("rocksDB is not an instance of TtlDB.");
    }
    return (TtlDB) this.rocksDB;
  }

  public TransactionDB getTranDB() {
    if (!(this.rocksDB instanceof TransactionDB)) {
      throw new IllegalStateException("rocksDB is not an instance of TransactionDB.");
    }
    return (TransactionDB) this.rocksDB;
  }

  public OptimisticTransactionDB getOptimisticTranDB() {
    if (!(this.rocksDB instanceof OptimisticTransactionDB)) {
      throw new IllegalStateException("rocksDB is not an instance of OptimisticTransactionDB.");
    }
    return (OptimisticTransactionDB) this.rocksDB;
  }

  // -------------------- Others -------------------- //

  private ColumnFamilyHandle getHandle(String name) {
    var handle = this.handleMap.get(name);
    if (handle == null) {
      throw new IllegalArgumentException("handle is not found: " + name);
    }
    return handle;
  }

  @Override
  public void close() {
    for (ColumnFamilyHandle handle : this.handleMap.values()) {
      RocksUtils.closeQuietly(handle);
    }
    // this.rocksDB.close();
    RocksUtils.closeQuietly(this.rocksDB);
    this.rocksCloseable.close();
  }

  // -------------------- Create RocksManager -------------------- //

  public static RocksManager open(String rocksDbDir, DBOptions options, boolean readOnly,
      List<ColumnFamilyDescriptor> descriptors, RocksCloseable rocksCloseable) throws RocksDBException {
    Arguments.notNull(rocksDbDir);
    Arguments.notNull(options);
    Arguments.notNull(rocksCloseable);

    Arguments.hasElements(descriptors);
    Arguments.isTrue(DEFAULT_COLUMN_FAMILY.equals(RocksUtils.getCfDescriptorName(descriptors.get(0))),
        "default ColumnFamilyDescriptor is required.");

    List<ColumnFamilyHandle> handles = new ArrayList<>(descriptors.size());
    RocksDB rocksDB = null;

    if (readOnly) {
      rocksDB = RocksDB.openReadOnly(options, rocksDbDir, descriptors, handles);
    } else {
      rocksDB = RocksDB.open(options, rocksDbDir, descriptors, handles);
    }
    return new RocksManager(rocksDB, handles, rocksCloseable);
  }

  public static RocksManager openTtl(String rocksDbDir, DBOptions options, boolean readOnly,
      List<ColumnFamilyDescriptor> descriptors, List<Integer> ttlValues, RocksCloseable rocksCloseable)
      throws RocksDBException {

    Arguments.notNull(rocksDbDir);
    Arguments.notNull(options);
    Arguments.notNull(ttlValues);
    Arguments.notNull(rocksCloseable);

    Arguments.hasElements(descriptors);
    Arguments.isTrue(DEFAULT_COLUMN_FAMILY.equals(RocksUtils.getCfDescriptorName(descriptors.get(0))),
        "default ColumnFamilyDescriptor is required.");

    List<ColumnFamilyHandle> handles = new ArrayList<>(descriptors.size());
    RocksDB rocksDB = TtlDB.open(options, rocksDbDir, descriptors, handles, ttlValues, readOnly);

    return new RocksManager(rocksDB, handles, rocksCloseable);
  }

  public static RocksManager openTran(String rocksDbDir, DBOptions options, TransactionDBOptions tranDbOptions,
      List<ColumnFamilyDescriptor> descriptors, RocksCloseable rocksCloseable) throws RocksDBException {

    Arguments.notNull(rocksDbDir);
    Arguments.notNull(options);
    Arguments.notNull(tranDbOptions);
    Arguments.notNull(rocksCloseable);

    Arguments.hasElements(descriptors);
    Arguments.isTrue(DEFAULT_COLUMN_FAMILY.equals(RocksUtils.getCfDescriptorName(descriptors.get(0))),
        "default ColumnFamilyDescriptor is required.");

    List<ColumnFamilyHandle> handles = new ArrayList<>(descriptors.size());
    RocksDB rocksDB = TransactionDB.open(options, tranDbOptions, rocksDbDir, descriptors, handles);

    return new RocksManager(rocksDB, handles, rocksCloseable);
  }

  public static RocksManager openOptimisticTran(String rocksDbDir, DBOptions options,
      List<ColumnFamilyDescriptor> descriptors, RocksCloseable rocksCloseable) throws RocksDBException {
    Arguments.notNull(rocksDbDir);
    Arguments.notNull(options);
    Arguments.notNull(rocksCloseable);

    Arguments.hasElements(descriptors);
    Arguments.isTrue(DEFAULT_COLUMN_FAMILY.equals(RocksUtils.getCfDescriptorName(descriptors.get(0))),
        "default ColumnFamilyDescriptor is required.");

    List<ColumnFamilyHandle> handles = new ArrayList<>(descriptors.size());
    RocksDB rocksDB = OptimisticTransactionDB.open(options, rocksDbDir, descriptors, handles);

    return new RocksManager(rocksDB, handles, rocksCloseable);
  }
}
