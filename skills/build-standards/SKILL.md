---
name: Shadowfang Build Standards
description: Build requirements and JVM guidelines for Shadowfang Folia projects.
---

# Shadowfang Build Standards

This document outlines the compile and runtime JVM requirements for the Shadowfang Folia project workspace.

## 1. Folia Server Version

The server runs **Folia 26.1.2** (custom fork). The PaperMC API dependency coordinates are:

```groovy
compileOnly "dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT"
```

In `plugin.yml`, the Bukkit API compatibility marker is `api-version: '1.21'` (this is the Bukkit standard — not the server version). Set `folia-supported: true` to declare Folia compatibility.

The Worlds plugin for v26.1.2 uses:
```
paperweight.foliaDevBundle("26.1.2.build.8-stable")
```

## 2. JDK 25 Requirement
The project ecosystem targets **Java 25** (class file version 69.0) due to downstream dependencies and server runtime environments:
- The `worlds` plugin dependency (`worlds-4.2.2-all.jar`) compiles targeting Java 25.
- The `foliaserver` runs under `jdk-25.0.1+8`.
- Any dependent plugins (such as `Shadowfang-Core-Folia`) must use the **Java 25 toolchain** in Gradle to avoid class version mismatch errors (`class file has wrong version 69.0, should be 65.0`).

### Gradle Toolchain Configuration
In `build.gradle`, use the Gradle Java Toolchain to enforce Java 25 compilation:
```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

## 3. Multi-project Compilations
When building the subprojects or compiling modules:
- Use the root/subproject Gradle wrapper (`gradlew.bat` or `./gradlew`) to build.
- For `shadowfang-ecosystem` modules, compile via the parent project or utilizing the main gradle wrapper.

## 4. Folia Scheduling & Thread Safety
When developing features:
- Never use synchronous teleportation; always use `player.teleportAsync(Location)`.
- Never use `Bukkit.getScheduler()`; use `Bukkit.getGlobalRegionScheduler()`, `Bukkit.getRegionScheduler()`, or `player.getScheduler()`.
