# Security

## General

The Registry supports AAS Security as defined in ([Specification of the Asset Administration Shell - Part 4: Security](https://industrialdigitaltwin.org/wp-content/uploads/2025/08/IDTA-01004-3-0-1_AAS-Specification_Part4_Security.pdf)).

By default, Security is disabled. To enable Security, you must connect the Registry to an Identity Provider, who takes care of the user identities. He verifies the JWT, and handles authentication requests. 

Additionally, you must add Access Rules according to the AAS Specification mentioned above. 

:::{caution}
You must be aware, that, if Security is enabled, everything is forbidden by default! So, if you don't specify any Access Rules, no access to the Registry will be possible.
:::

## Configuration

With `service.security.enabled` you can turn Security on (true) or off (false). The default value is `false`.

```properties
service.security.enabled=false
```

:::{caution}
Please be aware, that with disabled Security, all requests will be allowed.
:::

When Security is enabled, you must set additional configuration properties.

With `spring.security.oauth2.resourceserver.jwt.issuer-uri` (mandatory) you specify your Identity Provider, like e.g. Keycloak. You must obtain the correct URL from your preferred Identity Provider.

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8088/realms/Test
```

With `service.security.aclFolder` (mandatory) you specify the folder where your Access Rules are stored. The Rules must be in JSON format.

```properties
service.security.aclFolder=./acl
```
