# API

The Registry allows accessing the data via REST-API.

Please be aware, that HTTPS is used.

The Registry uses a configurable API prefix for all API calls. By default, the prefix is "/api/v3.0". The prefix can be changed in the Configuration.

## Supported API calls

- Asset Administration Shell Registry Interface
  - /shell-descriptors ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
  - /shell-descriptors/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
  - /shell-descriptors/{aasIdentifier}/submodel-descriptors ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
  - /shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
  - /query/shell-descriptors ![POST](https://img.shields.io/badge/POST-brightgreen)

- Submodel Registry Interface
  - /submodel-descriptors ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
  - /submodel-descriptors/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
  - /query/submodel-descriptors ![POST](https://img.shields.io/badge/POST-brightgreen)

- Description Interface
  - /description ![GET](https://img.shields.io/badge/GET-blue)

- Discovery Interface
  - /lookup/shells ![GET](https://img.shields.io/badge/GET-blue)
  - /lookup/shells/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

- Async Bulk Asset Administration Shell Registry API
  - /bulk/shell-descriptors ![POST](https://img.shields.io/badge/POST-brightgreen) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

- Async Bulk Submodel Registry API
  - /bulk/submodel-descriptors ![POST](https://img.shields.io/badge/POST-brightgreen) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

- Async Bulk Status API
  - /bulk/status/{handleId} ![GET](https://img.shields.io/badge/GET-blue)

- Async Bulk Result API
  - /bulk/result/{handleId} ![GET](https://img.shields.io/badge/GET-blue)

## Example

In the default configuration, the base URL for the API is e.g.:

https://localhost:8090/api/v3.0/shell-descriptors

## Query Language

Fa³st Registry supports AAS Query Language as defined in ([Specification of the Asset Administration Shell - Part 2: Application Programming Interfaces](https://industrialdigitaltwin.org/wp-content/uploads/2025/08/IDTA-01002-3-1-1_AAS-Specification_Part2_API.pdf)).

With the Query Language a user can query the Registry to search for specific AAS or Submodel descriptors, using several criteria. The Query Language offers a wide range of possibilities for the query.

E.g., if you want to query for all AAS descriptors with a specific idShort, you can use the following query:

```{code-block} json
{
  "Query": {
    "$condition": {
      "$eq": [
        {
          "$field": "$aasdesc#idShort"
        },
        {
          "$strVal": "MyAAS3"
        }
      ]
    }
  }
}
```

Or if you want to query for all AAS descriptors with an assetType that starts with "My asset", you can use the follwing query: 

```{code-block} json
{
  "Query": {
    "$condition": {
      "$starts-with": [
        {
          "$field": "$aasdesc#assetType"
        },
        {
          "$strVal": "My asset"
        }
      ]
    }
  }
}
```

It works the same way for Submodel descriptors. E.g., if you want to query for all Submodel descriptors with a http interface, you can use the following query: 

```{code-block} json
{
  "$condition": {
    "$eq": [
      {
        "$field": "$smdesc#endpoints[].interface"
      },
      {
        "$strVal": "http"
      }
    ]
  }
}
```
