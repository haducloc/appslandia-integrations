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
import java.util.List;

import org.rocksdb.ColumnFamilyDescriptor;

import com.appslandia.common.utils.Arguments;

/**
 *
 * @author Loc Ha
 *
 */
public class RocksCloseable implements AutoCloseable {

  final List<AutoCloseable> closeables = new ArrayList<>();

  public RocksCloseable add(AutoCloseable closeable) {
    this.closeables.add(closeable);
    return this;
  }

  public RocksCloseable add(ColumnFamilyDescriptor descriptor) {
    Arguments.notNull(descriptor);

    this.closeables.add(descriptor.getOptions());
    return this;
  }

  public RocksCloseable add(List<ColumnFamilyDescriptor> descriptors) {
    Arguments.notNull(descriptors);

    for (ColumnFamilyDescriptor descriptor : descriptors) {
      this.closeables.add(descriptor.getOptions());
    }
    return this;
  }

  @Override
  public void close() {
    for (var idx = this.closeables.size() - 1; idx >= 0; idx--) {

      RocksUtils.closeQuietly(this.closeables.get(idx));
    }
  }
}
