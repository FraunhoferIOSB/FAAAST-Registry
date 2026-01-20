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

- Submodel Registry Interface
  - /submodel-descriptors ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
  - /submodel-descriptors/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

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
