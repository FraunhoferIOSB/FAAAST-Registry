# In-Memory Persistence

## Introduction

The In-Memory Persistence keeps the AAS and Submodel Registry in the local memory. At startup, the Registry always starts empty. All registered AASs or Submodels will be stored in the local memory. When the application is stopped, all registered AASs and Submodels will be lost.

## Configuration

The In-Memory Persistence requires some settings in the `application.properties` (`service\src\main\resources\application.properties`) file:

```properties
##### RDBS via JPA (in-memory H2) #####
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.datasource.driver=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
#######################################
```

The property `spring.profiles.active` must be empty or not set, as the default profile is In-Memory Persistence.
