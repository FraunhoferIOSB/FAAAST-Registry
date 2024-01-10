# JPA (Java Persistenc API) Persistence

## Introduction

The JPA Persistence keeps the AAS and Submodel Registry in a relational database. When the Registry is restarted, all registered AASs and Submodels will still be availabe. So, when an AAS or Submodel is not available anymore, it's important to delete it from the Registry.

Currently, the JPA Persistence is configured and tested with PostgreSQL. But other relational databases supported by JPA should also work.
In that case, you must make sure, that the corresponding database driver dependency is included in the `pom.xml` of the service.

## Configuration

The JPA Persistence requires some settings in the `application.properties` (`service\src\main\resources\application.properties`) file:

```properties
##### RDBS via JPA (external PostgresDB) #####
spring.profiles.active=external
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.driver=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/Fa3stRegistry
spring.datasource.username=postgres
spring.datasource.password=
```

An example of these settings is already prepared in comments in the `application.properties`. Please make sure that the corresponding In-Memory settings are removed from the `application.properties`.

The property `spring.profiles.active` must be set to `external`.

Please make sure, that the database, referenced by `spring.datasource.url`, exists in your PostgreSQL database (in this example `Fa3stRegistry`).

Please make sure, that you add username and password of your PostgreSQL database in this section (properties `spring.datasource.username` and `spring.datasource.password`).
