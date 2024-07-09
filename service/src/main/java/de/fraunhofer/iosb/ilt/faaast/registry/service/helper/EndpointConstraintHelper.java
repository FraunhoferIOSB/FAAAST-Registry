/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.registry.service.helper;

import static de.fraunhofer.iosb.ilt.faaast.registry.service.helper.ConstraintHelper.MAX_IDSHORT_LENGTH;

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.SecurityAttributeObject;


/**
 * Helper class for validating endpoint constraints.
 */
public class EndpointConstraintHelper {

    private EndpointConstraintHelper() {}


    /**
     * Checks the constraints of the given list of endpoints.
     *
     * @param endpoints
     */
    public static void checkEndpoints(List<Endpoint> endpoints) {
        if (endpoints != null) {
            endpoints.stream().forEach(EndpointConstraintHelper::checkEndpoint);
        }
    }


    private static void checkEndpoint(Endpoint endpoint) {
        if (endpoint != null) {
            CommonConstraintHelper.checkText(endpoint.getInterface(), MAX_IDSHORT_LENGTH, true, "Interface");
            checkProtocolInformation(endpoint.getProtocolInformation());
        }
    }


    private static void checkProtocolInformation(ProtocolInformation protocolInformation) {
        if (protocolInformation != null) {
            CommonConstraintHelper.checkText(protocolInformation.getHref(), ConstraintHelper.MAX_STRING_2048_LENGTH, true, "href");
            CommonConstraintHelper.checkText(protocolInformation.getEndpointProtocol(), MAX_IDSHORT_LENGTH, false, "Endpoint Protocol");
            protocolInformation.getEndpointProtocolVersion().forEach(x -> CommonConstraintHelper.checkText(x, MAX_IDSHORT_LENGTH, false, "Endpoint Protocol Version"));
            CommonConstraintHelper.checkText(protocolInformation.getSubprotocol(), MAX_IDSHORT_LENGTH, false, "Subprotocol");
            CommonConstraintHelper.checkText(protocolInformation.getSubprotocolBody(), MAX_IDSHORT_LENGTH, false, "Subprotocol Body");
            CommonConstraintHelper.checkText(protocolInformation.getSubprotocolBodyEncoding(), MAX_IDSHORT_LENGTH, false, "Subprotocol Body Encoding");
            checkSecurityAttributes(protocolInformation.getSecurityAttributes());
        }
    }


    private static void checkSecurityAttributes(List<SecurityAttributeObject> securityAttributes) {
        if (securityAttributes != null) {
            securityAttributes.stream().forEach(EndpointConstraintHelper::checkSecurityAttribute);
        }
    }


    private static void checkSecurityAttribute(SecurityAttributeObject securityAttribute) {
        if (securityAttribute != null) {
            if (securityAttribute.getKey() == null) {
                CommonConstraintHelper.raiseConstraintViolatedException("Key is null");
            }
            else if (securityAttribute.getValue() == null) {
                CommonConstraintHelper.raiseConstraintViolatedException("Value is null");
            }
        }
    }

}
