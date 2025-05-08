# AppsLandia Integrations

## Features

 - **Facebook RocksDB** Integration
   
## Installation

### Java Version

- Java 21+

### Maven
```XML
<dependency>
    <groupId>com.appslandia</groupId>
    <artifactId>appslandia-integrations</artifactId>
    <version>1.1</version>
</dependency>

<dependency>
    <groupId>com.appslandia</groupId>
    <artifactId>appslandia-common</artifactId>
    <version>19.39.2</version>
</dependency>
```

### Gradle
```
dependencies {
  compile 'com.appslandia:appslandia-integrations:1.1'
  compile 'com.appslandia:appslandia-common:19.39.2'
}
```

## Sample Usage

### RocksDB
``` java
List<ColumnFamilyDescriptor> descriptors = new ArrayList<>();

// DEFAULT_COLUMN_FAMILY is required at index 0
descriptors.add(RocksUtils.newCfDescriptor(RocksManager.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
descriptors.add(RocksUtils.newCfDescriptor("other_column_family", new ColumnFamilyOptions()));

var dbOptions = RocksUtils.newDBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
var readOnlyMode = false;
var rocksCloseable = RocksUtils.newRocksCloseable().add(descriptors).add(dbOptions);

try (var db = RocksManager.open("database_dir", dbOptions, readOnlyMode, descriptors, rocksCloseable)) {

	// db.put, db.get, db.delete, db.newRocksIterator, db.newRocksEntryIterator, etc.
	
} catch (RocksDBException ex) {
	ex.printStackTrace();
}
```

## License
This code is distributed under the terms and conditions of the [MIT license](LICENSE).