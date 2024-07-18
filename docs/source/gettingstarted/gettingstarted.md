# Getting Started

## Prerequisites

-   Java 17+

## Usage

### From precompiled JAR

<!--start:download-release-->
<!--end:download-release-->

<!--start:download-snapshot-->
{download}`Latest SNAPSHOT version (0.1.0-SNAPSHOT) <https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.registry&a=service&v=0.1.0-SNAPSHOT>`<!--end:download-snapshot-->

### As Maven Dependency

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.registry</groupId>
	<artifactId>service</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

### As Gradle Dependency

```text
implementation 'de.fraunhofer.iosb.ilt.faaast.registry:service:0.1.0-SNAPSHOT'
```

## Building from Source

### Prerequisites

-   Maven

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Registry
cd FAAAST-Registry
mvn clean install
```

## Command-line Interface (CLI)

To start FA³ST Registry from command-line you need to run the `service` module by calling

```sh
> java -jar service-{version}.jar
```

:::{table} Supported CLI arguments and environment variables.
| CLI (short) | CLI (long)          | Environment variable                                           | Allowed<br>Values                       | Description                                                                                                                                              | Default<br>Value |
| ----------- | ------------------- | -------------------------------------------------------------- | --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- |
| -q          | --quite             |                                                                |                                         | Reduces log output (*ERROR* for FAST packages, *ERROR* for all other packages).<br>Default information about the starting process will still be printed. |                  |
| -v          | --verbose           |                                                                |                                         | Enables verbose logging (*INFO* for FAST packages, *WARN* for all other packages).                                                                       |                  |
| -vv         |                     |                                                                |                                         | Enables very verbose logging (*DEBUG* for FAST packages, *INFO* for all other packages).                                                                 |                  |
| -vvv        |                     |                                                                |                                         | Enables very very verbose logging (*TRACE* for FAST packages, *DEBUG* for all other packages).                                                           |                  |

## Example

This example shows how to start a FA³ST Registry with in-memory persistence.
If you have built the Registry from Source, you can find the file 'service-0.1.0-SNAPSHOT.jar' in the subdirectory 'service/target'.

### Via Command-line Interface (CLI)

```sh
java -jar service-0.1.0-SNAPSHOT.jar
```
