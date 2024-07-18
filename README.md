# FA³ST Registry [![Build Status](https://github.com/FraunhoferIOSB/FAAAST-Registry/workflows/Maven%20Build/badge.svg)](https://github.com/FraunhoferIOSB/FAAAST-Registry/actions) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/c6851106e76e4df782db1d30fe5d846f)](https://www.codacy.com/gh/FraunhoferIOSB/FAAAST-Registry/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FraunhoferIOSB/FAAAST-Registry&amp;utm_campaign=Badge_Grade) [![Documentation Status](https://readthedocs.org/projects/faaast-registry/badge/?version=latest)](https://faaast-registry.readthedocs.io/en/latest/?badge=latest) <a href="https://sonarcloud.io/summary/new_code?id=FraunhoferIOSB_FAAAST-Registry" ><img src="https://sonarcloud.io/images/project_badges/sonarcloud-white.svg" alt="SonarCloud badge" width="105"/></a>

![FA³ST Registry Logo Light](./docs/source/images/Fa3st-Registry_positiv.png/#gh-light-mode-only "FA³ST Registry Logo")
![FA³ST Registry Logo Dark](./docs/source/images/Fa3st-Registry_negativ.png/#gh-dark-mode-only "FA³ST Registry Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Registry.

For more details on FA³ST Registry see the full documentation :blue_book: [here](https://faaast-registry.readthedocs.io/).

| FA³ST Registry is still under development. Contributions in form of issues and pull requests are highly welcome. |
|-----------------------------|

## Prerequisites

-   Java 17+

## Getting Started

You can find a detailed documentation :blue_book: [here](https://faaast-registry.readthedocs.io/)

## Usage

### Download pre-compiled JAR

<!--start:download-release-->
[Download latest RELEASE version (1.0.0)](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/registry/service/1.0.0/service-1.0.0.jar)<!--end:download-release-->

<!--start:download-snapshot-->
[Download latest SNAPSHOT version (1.1.0-SNAPSHOT)](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.registry&a=service&v=1.1.0-SNAPSHOT)<!--end:download-snapshot-->

### As Maven Dependency

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.registry</groupId>
	<artifactId>service</artifactId>
	<version>1.0.0</version>
</dependency>
```

### As Gradle Dependency

```text
implementation 'de.fraunhofer.iosb.ilt.faaast.registry:service:1.0.0'
```

## Building from Source

### Prerequisites

-   Maven

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Registry
cd FAAAST-Registry
mvn clean install
```

## Changelog

You can find the detailed changelog [here](docs/source/changelog/changelog.md).

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.
You can find our contribution guidelines [here](CONTRIBUTING.md)

## Contributors

| Name | Github Account |
|:--| -- |
| Michael Jacoby | [mjacoby](https://github.com/mjacoby) |
| Tino Bischoff | [tbischoff2](https://github.com/tbischoff2) |

## Contact

faaast@iosb.fraunhofer.de

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.
