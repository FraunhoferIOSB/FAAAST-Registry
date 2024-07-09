# In-Memory Persistence

## Introduction

The In-Memory Persistence keeps the AAS and Submodel Registry in the local memory. At startup, the Registry always starts empty. All registered AASs or Submodels will be stored in the local memory. When the application is stopped, all registered AASs and Submodels will be lost.

## Configuration

The In-Memory Persistence doesn't require specific settings in the `application.properties` (`service\src\main\resources\application.properties`) file.

The property `spring.profiles.active` must be empty or not set, as the default profile is In-Memory Persistence.
