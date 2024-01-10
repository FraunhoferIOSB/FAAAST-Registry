# Getting Started

## Prerequisites

-   Java 17+

## Usage

### From precompiled JAR

<!--start:download-snapshot-->
[Download latest SNAPSHOT version (0.1.0-SNAPSHOT)](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&amp;g=de.fraunhofer.iosb.ilt.faaast.registry&amp;a=service&amp;v=0.1.0-SNAPSHOT)<!--end:download-snapshot-->

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

## Example

This example shows how to start a FA³ST Registry with in-memory persistence.

### Via Command-line Interface (CLI)

```sh
java -jar service-0.1.0-SNAPSHOT.jar
```

or

```sh
mvn spring-boot:run
```
