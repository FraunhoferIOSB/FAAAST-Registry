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

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.springframework.core.convert.converter.Converter;


/**
 * Converts an incoming HTTP request parameter into a list of SpecificAssetIds.
 */
public class SpecificAssetIdListConverter implements Converter<String, List<SpecificAssetId>> {

    private final JsonDeserializer jsonDeserializer = new JsonDeserializer();

    @Override
    public @Nullable List<SpecificAssetId> convert(@Nonnull String source) {
        try {
            return jsonDeserializer.readList(EncodingHelper.base64UrlDecode(source), SpecificAssetId.class);
        }
        catch (DeserializationException | IllegalArgumentException e) {
            throw new BadRequestException(e);
        }
    }
}
