# Release Notes
<!--start:changelog-header-->
## 1.2.0-SNAPSHOT (current development version)<!--end:changelog-header-->

**New Features & Major Changes**
- HTTP
	- Add discovery API (v3.0.1)
	- Ignore trailing slashes in URLs
	- Support Async Bulk APIs

**Internal changes & bugfixes**
- General
	- Fix synchronization issues in In-Memory Persistence
	- Minor corrections in Logging
	- Improved paging mechanism
	- Major updates of libraries used: Spring Boot 4, Spring Framework 7 and Hibernate 7
- JPA Persistence
	- Fix error in AAS Registry when a Submodel was used in multiple AASs
	- Fix error when multiple requests arrived at the same time

## 1.1.0

**New Features & Major Changes**
- HTTP
	- Added option to turn off SSL
	- Added description endpoint

**Internal changes & bugfixes**
- General
	- Fix Docker image
	- Update JPA mapping
	- Use descriptors from AAS4J
	- Fix documentation formatting errors
	- Add log message if a constraint violation occurs when creating AAS or Submodel

## 1.0.0

Initial development
