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

import java.util.Iterator;
import java.util.function.Function;

import org.rocksdb.RocksIterator;

import com.appslandia.common.utils.Arguments;

/**
 *
 * @author Loc Ha
 *
 */
public class RocksEntryIterator<K, V> implements Iterator<RocksEntry<K, V>>, AutoCloseable {

  final RocksIterator rocksIterator;
  final RocksMarshaller<K> keyMarshaller;
  final RocksMarshaller<V> valueMarshaller;

  final K fromKey;
  final Function<K, Boolean> toKeyMatcher;
  final Function<K, Boolean> keyFilter;

  private boolean _firstSeek = true;
  private boolean _toKeyMatched;
  private K _key;

  public RocksEntryIterator(RocksIterator rocksIterator, RocksMarshaller<K> keyMarshaller,
      RocksMarshaller<V> valueMarshaller, K fromKey, Function<K, Boolean> toKeyMatcher,
      Function<K, Boolean> keyFilter) {
    this.rocksIterator = Arguments.notNull(rocksIterator);
    this.keyMarshaller = Arguments.notNull(keyMarshaller);
    this.valueMarshaller = valueMarshaller;

    this.fromKey = fromKey;
    this.toKeyMatcher = toKeyMatcher;
    this.keyFilter = keyFilter;
  }

  @Override
  public boolean hasNext() {
    if (this._toKeyMatched) {
      return false;
    }
    if (this._firstSeek) {
      if (this.fromKey == null) {
        this.rocksIterator.seekToFirst();
      } else {
        this.rocksIterator.seek(this.keyMarshaller.marshal(this.fromKey));
      }
      this._firstSeek = false;
    } else {
      this.rocksIterator.next();
    }

    if (!this.rocksIterator.isValid()) {
      return false;
    }

    if (this.keyFilter == null) {
      this._key = this.keyMarshaller.unmarshal(this.rocksIterator.key());
      return true;
    }

    while (true) {
      var key = this.keyMarshaller.unmarshal(this.rocksIterator.key());
      if (!this.keyFilter.apply(key)) {

        this.rocksIterator.next();
        if (!this.rocksIterator.isValid()) {
          return false;
        }
      } else {
        this._key = key;
        return true;
      }
    }
  }

  @Override
  public RocksEntry<K, V> next() {
    Arguments.isTrue(!this._toKeyMatched);
    Arguments.notNull(this._key);

    if (this.toKeyMatcher != null) {
      this._toKeyMatched = this.toKeyMatcher.apply(this._key);
    }

    if (this.valueMarshaller == null) {
      return new RocksEntry<>(this._key, null);
    } else {
      return new RocksEntry<>(this._key, this.valueMarshaller.unmarshal(this.rocksIterator.value()));
    }
  }

  @Override
  public void close() {
    this.rocksIterator.close();
  }
}
