# Configuration

## General

You can change configuration properties in the `application.properties` (`service\src\main\resources\application.properties`) file.

With `server.port` you can change the HTTPS Port of the API. The default value is `8090`.

```properties
server.port=8090
```

## Persistence: In-Memory vs. Database

The default configuration starts the Registry with in-memory persistence. To use relational database persistence with a PostgreSQL database, please refer to the `JPA Persistence` section for details.
