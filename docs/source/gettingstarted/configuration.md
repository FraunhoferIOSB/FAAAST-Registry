# Configuration

## General

You can change configuration properties in the `application.properties` (`service\src\main\resources\application.properties`) file.

With `server.port` you can change the HTTPS Port of the API. The default value is `8090`.

```properties
server.port=8090
```

With `server.ssl.enabled` you can turn off SSL. The default value is `true`.

```properties
server.ssl.enabled=false
```

## CORS

A common issue when accessing FA³ST Registry is a cross-origin resource sharing block.
By default, the Registry does not enable CORS, but it typically is required when you want to access the REST interface from any machine other than the one running Registry.

With `cors.enabled` you can enable CORS. By default, CORS is disabled.

```properties
cors.enabled=true
```

With `cors.allowedOrigins` you can set the origins for which cross-origin requests are allowed from a browser. `*` allows all origins, which is also the default value.

```properties
cors.allowedOrigins=*
```

With `cors.allowedMethods` you can set the HTTP methods to allow, e.g. `GET`, `POST`, etc. The special value `*` allows all methods. By default, "simple" methods GET, HEAD, and POST are allowed.

```properties
cors.allowedMethods=GET, POST
```

With `cors.allowedHeaders` you can set the list of headers that a pre-flight request can list as allowed for use during an actual request. The special value `*` may be used to allow all headers. By default, all headers are allowed.

```properties
cors.allowedHeaders=*
```

With `cors.exposedHeaders` you can set the list of response headers that an actual response might have and can be exposed. The special value `*` allows all headers to be exposed. By default this is not set.

```properties
cors.exposedHeaders=header1
```

With `cors.maxAge` you can configure how long in seconds the response from a pre-flight request can be cached by clients. By default this is set to 1800 seconds (30 minutes).

```properties
cors.maxAge=3600
```

You can find some example settings prepared in comments in `application.properties`.

```properties
# settings to enable CORS
#cors.enabled=true
#cors.allowedOrigins=*
```

## User-defined configuration file

You can use your own configuration file to use your desired settings. You can use the file `service\src\main\resources\application.properties` as template.
Just modify the settings in your `application.properties` as desired.

In order to apply your changes, place your `application.properties` file in the same directory as the service JAR, or in a sub-directory called `config`.
This will override the default settings.

## Persistence: In-Memory vs. Database

The default configuration starts the Registry with in-memory persistence. To use relational database persistence with a PostgreSQL database, please refer to the `JPA Persistence` section for details.
