# LibManager

[![Wiki](https://img.shields.io/badge/docs-wiki-blue?style=flat-square)](https://github.com/GhostAndry/LibManager/wiki/wiki)

A universal runtime library loader and isolator for Java applications. Automatically download Maven dependencies and instantiate core modules in isolated classloaders.

## Features

✨ **Auto-download** libraries from Maven repositories  
🔒 **Isolation** via custom classloaders (parent-first/child-first)  
🎯 **Dynamic instantiation** of bootstrap classes  
🔄 **Flexible constructor** support  
📦 **Version control** and dependency management  

## Quick Start

### Installation

**Gradle:**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.GhostAndry:LibManager:v1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.github.GhostAndry</groupId>
    <artifactId>LibManager</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Basic Usage

```java
LibraryAdapter adapter = new MyAdapter();
LibraryLoader loader = new LibraryLoader(dataFolder, logger);

Object core = loader.bootstrap(
    adapter,
    Arrays.asList("org.yaml:snakeyaml:2.2"),
    "com.example.CoreBootstrap",
    Collections.emptyList(),
    Arrays.asList("com.example")
);
```

## 📖 [See Wiki](https://github.com/GhostAndry/LibManager/wiki)

Full documentation, API reference, and advanced examples available in the **[Wiki](https://github.com/GhostAndry/LibManager/wiki/wiki)**.

## Contributing

Pull requests and issues welcome! 🚀
