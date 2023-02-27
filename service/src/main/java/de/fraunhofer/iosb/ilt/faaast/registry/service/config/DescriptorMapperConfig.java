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
package de.fraunhofer.iosb.ilt.faaast.registry.service.config;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultAdministrativeInformation;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


/**
 * Class for configuring the classes to use for the descriptor interfaces.
 */
@Configuration
public class DescriptorMapperConfig {

    //    /**
    //     * Retrieves an ObjectMapper with the desired settings.
    //     *
    //     * <p>Two options will be configure by default:
    //     * disable MapperFeature.DEFAULT_VIEW_INCLUSION
    //     * disable DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES</p>
    //     *
    //     * @return The ObjectMapper.
    //     */
    //    @Bean
    //    @Primary
    //    public ObjectMapper objectMapper() {
    //        ObjectMapper mapper = new ObjectMapper();
    //
    //        SimpleModule module = new SimpleModule("AasModel", Version.unknownVersion());
    //
    //        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
    //        resolver.addMapping(AssetAdministrationShellDescriptor.class, DefaultAssetAdministrationShellDescriptor.class);
    //        resolver.addMapping(DescriptionDescriptor.class, DefaultDescriptionDescriptor.class);
    //        resolver.addMapping(IdentifierKeyValuePairDescriptor.class, DefaultIdentifierKeyValuePairDescriptor.class);
    //        resolver.addMapping(KeyDescriptor.class, DefaultKeyDescriptor.class);
    //        resolver.addMapping(ReferenceDescriptor.class, DefaultReferenceDescriptor.class);
    //        resolver.addMapping(SubmodelDescriptor.class, DefaultSubmodelDescriptor.class);
    //
    //        module.setAbstractTypes(resolver);
    //
    //        mapper.registerModule(module);
    //        return mapper;
    //    }

    /**
     * Register our Mappings in the ObjectMapper.
     *
     * @return The desired Jackson2ObjectMapperBuilder.
     */
    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        SimpleModule module = new SimpleModule("AasModel", Version.unknownVersion());

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AssetAdministrationShellDescriptor.class, DefaultAssetAdministrationShellDescriptor.class);
        resolver.addMapping(AdministrativeInformation.class, DefaultAdministrativeInformation.class);
        resolver.addMapping(Endpoint.class, DefaultEndpoint.class);
        resolver.addMapping(ProtocolInformation.class, DefaultProtocolInformation.class);
        resolver.addMapping(Identifier.class, DefaultIdentifier.class);
        resolver.addMapping(IdentifierKeyValuePair.class, DefaultIdentifierKeyValuePair.class);
        resolver.addMapping(Key.class, DefaultKey.class);
        resolver.addMapping(Reference.class, DefaultReference.class);
        resolver.addMapping(SubmodelDescriptor.class, DefaultSubmodelDescriptor.class);

        module.setAbstractTypes(resolver);
        return new Jackson2ObjectMapperBuilder()
                .modules(module);
    }
}
