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
package helper;

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class for common data for the constraint validation.
 */
public class CommonConstraintHelper {

    private CommonConstraintHelper() {}


    /**
     * Checks the constraints of the given Id.
     *
     * @param id The desired id.
     */
    public static void checkId(String id) {
        if ((id == null) || (id.length() == 0)) {
            raiseConstraintViolatedException("no Id provided");
        }
        else if (id.length() > ConstraintHelper.MAX_IDENTIFIER_LENGTH) {
            raiseConstraintViolatedException("ID too long.");
        }
    }


    /**
     * Checks the constraints of the given IdShort.
     *
     * @param idShort The desired idShort.
     */
    public static void checkIdShort(String idShort) {
        if ((idShort != null) && (idShort.length() > ConstraintHelper.MAX_IDSHORT_LENGTH)) {
            raiseConstraintViolatedException("IdShort too long.");
        }
    }


    /**
     * Checks the constraints of the given text.
     *
     * @param txt The desired text.
     * @param maxLength The maximum length of the text, 0 if unlimited.
     * @param notNull true if the text must not be null, false if null is allowed.
     * @param msg The message for the exception.
     */
    public static void checkText(String txt, int maxLength, boolean notNull, String msg) {
        if (notNull && (txt == null)) {
            raiseConstraintViolatedException(String.format("%s is null", msg));
        }
        if (txt != null) {
            if (txt.isEmpty()) {
                raiseConstraintViolatedException(String.format("%s is empty", msg));
            }
            else if ((maxLength > 0) && (txt.length() > maxLength)) {
                raiseConstraintViolatedException(String.format("%s too long", msg));
            }
            else if (!ConstraintHelper.TEXT_PATTERN.matcher(txt).matches()) {
                raiseConstraintViolatedException(String.format("%s doesn't match the pattern", msg));
            }
        }
    }


    /**
     * Checks the constraints of the given language text.
     *
     * @param language The deesired language text.
     * @param msg
     */
    public static void checkLanguage(String language, String msg) {
        if (language == null) {
            raiseConstraintViolatedException(String.format("no %s provided", msg));
        }
        else if (!ConstraintHelper.LANG_LANGUAGE_PATTERN.matcher(language).matches()) {
            raiseConstraintViolatedException(String.format("%s doesn't match the pattern", msg));
        }
    }


    /**
     * Checks the constraints of the given list of references.
     *
     * @param references The desired list of references.
     */
    public static void checkReferences(List<Reference> references) {
        if (references != null) {
            references.stream().forEach(CommonConstraintHelper::checkReference);
        }
    }


    /**
     * Checks the constraints of the given reference.
     *
     * @param reference The desired reference.
     */
    public static void checkReference(Reference reference) {
        if (reference != null) {
            checkReferenceParent(reference);
            checkReferenceParent(reference.getReferredSemanticId());
        }
    }


    /**
     * Raise an exception if a constraint is violated.
     *
     * @param txt The desired text for the exception.
     */
    public static void raiseConstraintViolatedException(String txt) {
        throw new BadRequestException(txt);
    }


    private static void checkKey(Key key) {
        if (key.getType() == null) {
            raiseConstraintViolatedException("no key type provided");
        }
        checkText(key.getValue(), ConstraintHelper.MAX_IDENTIFIER_LENGTH, true, "key value");
    }


    private static void checkReferenceParent(Reference reference) {
        if (reference != null) {
            if (reference.getType() == null) {
                raiseConstraintViolatedException("no reference type provided");
            }
            if ((reference.getKeys() == null) || reference.getKeys().isEmpty()) {
                raiseConstraintViolatedException("no keys provided");
            }

            reference.getKeys().stream().forEach(CommonConstraintHelper::checkKey);
        }
    }

}
