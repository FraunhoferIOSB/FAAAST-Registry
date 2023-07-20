# Configuration

## Use relational database persistence

The default configuration starts the Registry with in-memory persistence. If you want to use relational database persistence with a PostgreSQL database, you must make several changes.

### service/pom.xml

Remove the following dependency:

```xml
    <groupId>${project.groupId}</groupId>
    <artifactId>persistence-memory</artifactId>
```

and add the following dependency instead (just uncomment the corresponding dependency):

```xml
    <groupId>${project.groupId}</groupId>
    <artifactId>persistence-jpa</artifactId>
```

### service\src\main\resources\application.properties

Remove or comment the following block:

```properties
##### RDBS via JPA (in-memory H2) #####
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.datasource.driver=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
#######################################
```

uncomment the following block (except first and last line):

```properties
###### RDBS via JPA (external PostgresDB) #####
#spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
#spring.datasource.driver=org.postgresql.Driver
#spring.datasource.url=jdbc:postgresql://localhost:5432/Fa3stRegistry
#spring.datasource.username=postgres
#spring.datasource.password=admin
###############################################
```

### service\src\main\resources\applicationContext.xml

Remove or comment the following line:

```xml
    <bean id="aasRepository" class="de.fraunhofer.iosb.ilt.faaast.registry.memory.AasRepositoryMemory"/>
```

uncomment the following line (except first and last line):

```xml
    <!--<bean id="aasRepository" class="de.fraunhofer.iosb.ilt.faaast.registry.jpa.AasRepositoryJpa"/>-->
```
