# FA³ST Registry
![FA³ST Logo Light](./images/Fa3st-Registry_positiv.png "FA³ST Registry Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Registry implements the Registry for the [Asset Administration Shell (AAS) specification by Plattform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and provides an easy-to-use Registry for AAS.

The FA³ST Registry contains two separate Registry instances: An AAS Registry for Asset Administration Shell (AAS) Descriptors, and a Submodel Registry for Submodel Descriptors. These Registry instances are strictly separated, i.e. when an AAS with Submodels is registered in the AAS Registry, the Submodels of this AAS won't be registered in the Submodel Registry. If you want these Submodels to be registered in the Submodel Registry, you must register them in the Submodel Registry separately.

## Implemented AAS Specification
| Specification | Version |
|:--| -- |
| Details of the Asset Administration Shell - Part 2<br />Interoperability at Runtime – Exchanging Information via Application Programming Interfaces | Version 1.0RC02<br />([specification](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.pdf))<br />([swagger](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V1.0RC02)) |

## Features

-   supports several persistence implementations: `memory, jpa`
-   supports separated Registries
	-   AAS Registry
    -   Submodel Registry

```{toctree}
:hidden:
:caption: Getting Started
:maxdepth: 3
gettingstarted/gettingstarted.md
gettingstarted/configuration.md
```

```{toctree}
:hidden:
:caption: API
:maxdepth: 2
api/api.md
```

```{toctree}
:hidden:
:caption: Persistence
:maxdepth: 3
General <persistences/persistence.md>
In-Memory <persistences/inmemory_persistence.md>
JPA <persistences/jpa_persistence.md>
```

```{toctree}
:hidden:
:caption: About
:maxdepth: 2
about/about.md
about/contributing.md
about/recommended.md
```

```{toctree}
:hidden:
:caption: Changelog
:maxdepth: 2
changelog/changelog.md
```
