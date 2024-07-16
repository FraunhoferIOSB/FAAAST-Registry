# Configuration

## General

You can change configuration properties in the `application.properties` (`service\src\main\resources\application.properties`) file.

With `server.port` you can change the HTTPS Port of the API. The default value is `8090`.

```properties
server.port=8090
```

## User-defined configuration file

You can use your own configuration file to use your desired settings. You can use the file `service\src\main\resources\application.properties` as template.
Just modify the settings in your `application.properties` as desired.

In order to apply your changes, place your `application.properties` file in the same directory as the service JAR, or in a sub-directory called `config`.
This will override the default settings.

## Persistence: In-Memory vs. Database

The default configuration starts the Registry with in-memory persistence. To use relational database persistence with a PostgreSQL database, please refer to the `JPA Persistence` section for details.
