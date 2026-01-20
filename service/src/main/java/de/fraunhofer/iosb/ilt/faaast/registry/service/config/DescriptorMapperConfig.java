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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.EnumDeserializer3;
import de.fraunhofer.iosb.ilt.faaast.registry.service.helper.EnumSerializer3;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.PageMixin;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.ServiceSpecificationProfileMixin;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceSpecificationProfile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.registry.ProtocolInformationMixin;
import de.fraunhofer.iosb.ilt.faaast.service.registry.SecurityAttributeObjectMixin;
import de.fraunhofer.iosb.ilt.faaast.service.registry.SpecificAssetIdMixin;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.internal.mixins.DataSpecificationIec61360Mixin;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.internal.mixins.EndpointMixin;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.internal.mixins.ExtensionMixin;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.internal.mixins.KeyMixin;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.internal.mixins.ReferenceMixin;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.LevelType;
import org.eclipse.digitaltwin.aas4j.v3.model.ProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SecurityAttributeObject;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueReferencePair;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultDataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultExtension;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringDefinitionTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringPreferredNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringShortNameTypeIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLevelType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSecurityAttributeObject;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultValueReferencePair;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.module.SimpleAbstractTypeResolver;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.util.StdDateFormat;


/**
 * Class for configuring the classes to use for the descriptor interfaces.
 */
@Configuration
public class DescriptorMapperConfig {

    /**
     * Register our Mappings in the ObjectMapper.
     *
     * @return The desired Jackson2ObjectMapperBuilder.
     */
    @Bean
    @Primary
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        SimpleModule module = new SimpleModule();

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AssetAdministrationShellDescriptor.class, DefaultAssetAdministrationShellDescriptor.class);
        resolver.addMapping(AdministrativeInformation.class, DefaultAdministrativeInformation.class);
        resolver.addMapping(Endpoint.class, DefaultEndpoint.class);
        resolver.addMapping(ProtocolInformation.class, DefaultProtocolInformation.class);
        resolver.addMapping(SpecificAssetId.class, DefaultSpecificAssetId.class);
        resolver.addMapping(Key.class, DefaultKey.class);
        resolver.addMapping(Reference.class, DefaultReference.class);
        resolver.addMapping(SubmodelDescriptor.class, DefaultSubmodelDescriptor.class);
        resolver.addMapping(LangStringTextType.class, DefaultLangStringTextType.class);
        resolver.addMapping(LangStringNameType.class, DefaultLangStringNameType.class);
        resolver.addMapping(Extension.class, DefaultExtension.class);
        resolver.addMapping(EmbeddedDataSpecification.class, DefaultEmbeddedDataSpecification.class);
        resolver.addMapping(DataSpecificationIec61360.class, DefaultDataSpecificationIec61360.class);
        resolver.addMapping(LangStringPreferredNameTypeIec61360.class, DefaultLangStringPreferredNameTypeIec61360.class);
        resolver.addMapping(LangStringShortNameTypeIec61360.class, DefaultLangStringShortNameTypeIec61360.class);
        resolver.addMapping(LangStringDefinitionTypeIec61360.class, DefaultLangStringDefinitionTypeIec61360.class);
        resolver.addMapping(ValueList.class, DefaultValueList.class);
        resolver.addMapping(ValueReferencePair.class, DefaultValueReferencePair.class);
        resolver.addMapping(LevelType.class, DefaultLevelType.class);
        resolver.addMapping(DataSpecificationContent.class, DataSpecificationIec61360.class);
        resolver.addMapping(SecurityAttributeObject.class, DefaultSecurityAttributeObject.class);

        ReflectionHelper.ENUMS.forEach(x -> module.addSerializer(x, new EnumSerializer3()));
        ReflectionHelper.ENUMS.forEach(x -> module.addDeserializer(x, new EnumDeserializer3(x)));

        module.setAbstractTypes(resolver);
        return builder -> builder
                .addModule(module)
                .addMixIn(Page.class, PageMixin.class)
                .addMixIn(Endpoint.class, EndpointMixin.class)
                .addMixIn(DataSpecificationIec61360.class, DataSpecificationIec61360Mixin.class)
                .addMixIn(Extension.class, ExtensionMixin.class)
                .addMixIn(Key.class, KeyMixin.class)
                .addMixIn(Reference.class, ReferenceMixin.class)
                .addMixIn(ServiceSpecificationProfile.class, ServiceSpecificationProfileMixin.class)
                .addMixIn(SecurityAttributeObject.class, SecurityAttributeObjectMixin.class)
                .addMixIn(Endpoint.class, EndpointMixin.class)
                .addMixIn(ProtocolInformation.class, ProtocolInformationMixin.class)
                .addMixIn(SpecificAssetId.class, SpecificAssetIdMixin.class)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
                .defaultDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY));
    }
}
