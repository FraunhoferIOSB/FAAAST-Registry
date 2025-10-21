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
package de.fraunhofer.iosb.ilt.faaast.registry.core.util;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Descriptor;


/**
 * Helper class with methods to create deep copies.
 */
public class DeepCopyHelper {

    private DeepCopyHelper() {}


    /**
     * Create a deep copy of a list of {@link org.eclipse.digitaltwin.aas4j.v3.model.Descriptor} objects.
     *
     * @param <T> type of the desriptor.
     * @param descriptor which should be deep copied.
     * @param outputClass of the referable.
     * @return the deep copied descriptor.
     */
    public static <T extends Descriptor> T deepCopy(Descriptor descriptor, Class<T> outputClass) {
        if (outputClass == null) {
            throw new IllegalArgumentException("outputClass must be non-null");
        }
        if ((descriptor != null) && !outputClass.isAssignableFrom(descriptor.getClass())) {
            throw new IllegalArgumentException(
                    String.format("type mismatch - can not create deep copy of instance of type %s with target type %s", descriptor.getClass(), outputClass));
        }
        try {
            return new JsonDeserializer().read(new JsonSerializer().write(descriptor), outputClass);
        }
        catch (SerializationException | DeserializationException e) {
            throw new IllegalArgumentException("deep copy of descriptor failed", e);
        }
    }


    /**
     * Creates a backup of a map of {@link org.eclipse.digitaltwin.aas4j.v3.model.Descriptor} objects.
     *
     * @param <T> type of the desriptor.
     * @param descriptors the descriptor map.
     * @return a map with descriptor backups.
     */
    public static <T extends Descriptor> Map<String, String> createBackupMap(Map<String, ? extends T> descriptors) {
        HashMap<String, String> retval = new HashMap<>();
        for (var entry: descriptors.entrySet()) {
            retval.put(entry.getKey(), createBackup(entry.getValue()));
        }
        return retval;
    }


    /**
     * restores a map of {@link org.eclipse.digitaltwin.aas4j.v3.model.Descriptor} objects from a backup map..
     *
     * @param <T> type of the desriptor.
     * @param backupDescriptors the descriptor backup map.
     * @param outputClass of the descriptors
     * @return a map with the restored descriptors.
     */
    public static <T extends Descriptor> Map<String, T> restoreBackupMap(Map<String, String> backupDescriptors, Class<? extends T> outputClass) {
        HashMap<String, T> retval = new HashMap<>();
        for (var entry: backupDescriptors.entrySet()) {
            retval.put(entry.getKey(), restoreBackup(entry.getValue(), outputClass));
        }
        return retval;
    }


    private static String createBackup(Descriptor descriptor) {
        try {
            return new JsonSerializer().write(descriptor);
        }
        catch (SerializationException e) {
            throw new IllegalArgumentException("create backup of descriptor failed", e);
        }
    }


    private static <T extends Descriptor> T restoreBackup(String backupDescriptor, Class<T> outputClass) {
        try {
            return new JsonDeserializer().read(backupDescriptor, outputClass);
        }
        catch (DeserializationException e) {
            throw new IllegalArgumentException("restore backup of descriptor failed", e);
        }
    }
}
