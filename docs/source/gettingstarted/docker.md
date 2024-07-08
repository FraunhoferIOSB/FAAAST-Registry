# Usage with Docker

This section describes the usage with docker and docker compose.

## Docker-Compose

Clone this repository, navigate to `/misc/docker/` and run this command inside it.

```sh
cd misc/docker
docker-compose up
```

To modify the configuration edit the file `/misc/docker/docker-compose.yml`.
You can change the following values using environment variables:
    

### Configuration

| Name                                    | Example Value                                                             | Description                                                  |
|:----------------------------------------|---------------------------------------------------------------------------|--------------------------------------------------------------|
| spring.profiles.active                  | default / jpa                                                             | in-memory or jpa database connection                    |
| spring.jpa.properties.hibernate.dialect | org.hibernate.dialect.H2Dialect / org.hibernate.dialect.PostgreSQLDialect | the hibernate dialect to be used for the database connection |
| spring.datasource.driver                | org.h2.Driver / org.postgresql.Driver                                     | the JDBC driver to be used for the database connection       |
| spring.datasource.url                   | jdbc:postgresql://db:5432/postgres                                        | url of the internal or external database                     |
| spring.datasource.username              | postgres                                                                  | username for the database                                    |
| spring.datasource.password              | admin                                                                     | password for the database                                    |
| server.port                             | 8090                                                                      | port of the Registry                                         |

## Docker CLI

To start the FA³ST Registry with default values execute this command.
A FA³ST Registry with in-memory database on port 8090 will be started.

```sh
docker run --rm -P fraunhoferiosb/faaast-registry
```

To start the FA³ST Registry with your own configuration, override the environment variables.

```sh
docker run --rm -P -e "server.port=8091" fraunhoferiosb/faaast-registry
```

Similarly to the above examples you can pass more arguments to the FA³ST Registry by using the CLI or an environment file
