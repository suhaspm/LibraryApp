# 0005: Spring Boot 4.1 on Java 21

**Status:** Accepted

## Context
The original plan said "Spring Boot 3". As of October 2026, Spring Boot 3.5's open-source
support has ended. Spring Boot 4.x is current (4.1.2), built on Spring Framework 7 and Jakarta EE 11.

## Decision
Use Spring Boot 4.1.x with Java 21 (LTS) and virtual threads enabled.

## Consequences
- Some tutorials you'll find are for Boot 3. The main differences we've hit:
  - Boot 4 has modular starters: `spring-boot-starter-webmvc`, and per-feature test starters such as `spring-boot-starter-webmvc-test`.
  - MongoDB properties moved to `spring.mongodb.*`.
  - Testcontainers 2.x renamed its modules (`testcontainers-mongodb`, class `org.testcontainers.mongodb.MongoDBContainer`).
- Java 25 (the next LTS) is a later upgrade, done by changing one line in the convention plugin.
