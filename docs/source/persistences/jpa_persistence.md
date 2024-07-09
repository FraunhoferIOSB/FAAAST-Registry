# JPA (Java Persistenc API) Persistence

## Introduction

The JPA Persistence keeps the AAS and Submodel Registry in a relational database. When the Registry is restarted, all registered AASs and Submodels will still be availabe, unless you use an In-Memory database like H2. So, when an AAS or Submodel is not available anymore, it's important to delete it from the Registry.

Currently, the JPA Persistence is configured and tested with PostgreSQL and H2. But other relational databases supported by JPA should also work.
In that case, you must make sure, that the corresponding database driver dependency is included in the `pom.xml` of the service.

## Configuration

The JPA Persistence requires some settings in the `application.properties` (`service\src\main\resources\application.properties`) file:

Here an example of the settings required for PostgreSQL:

```properties
##### RDBS via JPA (external PostgresDB) #####
spring.profiles.active=jpa
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.driver=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/fa3st-registry
spring.datasource.username=fa3st-registry
spring.datasource.password=ChangeMe
```

Here an example of the settings required for H2:

```properties
##### RDBS via JPA (in-memory H2) #####
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.profiles.active=jpa
spring.datasource.driver=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
```

An example of these settings is already prepared in comments in the `application.properties`.

The property `spring.profiles.active` must be set to `jpa`.

Please make sure, that the database, referenced by `spring.datasource.url`, exists in your PostgreSQL database (in this example `fa3st-registry`).

Please make sure, that you add username and password of your PostgreSQL database in this section (properties `spring.datasource.username` and `spring.datasource.password`).
