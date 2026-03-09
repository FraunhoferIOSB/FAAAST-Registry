# Security

## General

The Registry supports AAS Security as defined in ([Specification of the Asset Administration Shell - Part 4: Security](https://industrialdigitaltwin.org/wp-content/uploads/2025/08/IDTA-01004-3-0-1_AAS-Specification_Part4_Security.pdf)).

By default, Security is disabled. To enable Security, you must connect the Registry to an Identity Provider, who takes care of the user identities. He verifies the JWT, and handles authentication requests. 

Additionally, you must add Access Rules according to the AAS Specification mentioned above. 

:::{caution}
You must be aware, that, if Security is enabled, everything is forbidden by default! So, if you don't specify any Access Rules, no access to the Registry will be possible.
:::

## Configuration

### General

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

You must specify a valid URL and make sure, that the Identity Provider is running. Otherwise the Registry won't be able to start.

With `service.security.aclFolder` (mandatory) you specify the folder where your Access Rules are stored. The Rules must be in JSON format, as specified by the AAS Specification mentioned above.

```properties
service.security.aclFolder=./acl
```

### Token Exchange

Fa³st Registry allows to execute a Token Exchange with a dedicated server, before validating the JWT witht the specified Identity Provider. The URL of the Token Exchange server is configured with `service.security.tokenExchange.url`. 

```properties
service.security.tokenExchange.url=https://example.com/sts/token
```

If this Token Exchange URL is empty, the Token Exchange is disabled. If a valif URL is specified, the Registry sends a Token Exchange request to this server. Tha Token Exchange server processes the request and returns the exchanged JWT as response. Afterwards, the Registry continues with the JWT in the response.

### Usage

If you want to grant anonymous READ access for AAS descriptors, you must define an Access rule and place it in the `service.security.aclFolder`.

```{code-block} json
{
    "AllAccessPermissionRules": {
        "rules": [
            {
                "ACL": {
                    "ATTRIBUTES": [
                        {
                            "GLOBAL": "ANONYMOUS"
                        }
                    ],
                    "RIGHTS": [
                        "READ"
                    ],
                    "ACCESS": "ALLOW"
                },
                "OBJECTS": [
                    {
                        "DESCRIPTOR": "(aasDesc)*"
                    }
                ],
                "FORMULA": {
                    "$boolean": true
                }
            }
        ]
    }
}
```

If you want to grant anonymous READ access for Submodel descriptors in the Submodel Registry, just use the rule as above and replace `(aasDesc)` with `(smDesc)`.

If you want to grant access to a specific user, you can refer to claims from the JWT.

E.g., if you want to grant READ, UPDATE and CREATE access for AAS descriptors to a user with the E-Mail address `bob@example.com`, you can use a rule like this: 

```{code-block} json
{
    "AllAccessPermissionRules": {
        "rules": [
            {
                "ACL": {
                    "ATTRIBUTES": [
                        {
                            "CLAIM": "email"
                        }
                    ],
                    "RIGHTS": [
                        "READ",
                        "UPDATE",
                        "CREATE"
                    ],
                    "ACCESS": "ALLOW"
                },
                "OBJECTS": [
                    {
                        "DESCRIPTOR": "(aasDesc)*"
                    }
                ],
                "FORMULA": {
                    "$or": [
                        {
                            "$eq": [
                                {
                                    "$attribute": {
                                        "CLAIM": "email"
                                    }
                                },
                                {
                                    "$strVal": "bob@example.com"
                                }
                            ]
                        }
                    ]
                }
            }
        ]
    }
}
```
