# Getting Started

## Prerequisites

-   Java 17+

## Usage

### From precompiled JAR

<!--start:download-snapshot-->
[Download latest SNAPSHOT version (0.1.0-SNAPSHOT)](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.registry&a=service&v=0.1.0-SNAPSHOT)<!--end:download-snapshot-->

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
mvn spring-boot:run
```

## Introduction

The FA³ST Registry contains two separate Registry instances: An AAS Registry for Asset Administration Shell (AAS) Descriptors, and a Submodel Registry for Submodel Descriptors. These Registry instances are strictly separated, i.e. when an AAS with Submodels is registered in the AAS Registry, the Submodels of this AAS won't be registered in the Submodel Registry. If you want these Submodels to be registered in the Submodel Registry, you must register them in the Submodel Registry separately.

## API

The Registry allows accessing the data via REST-API.
