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
package de.fraunhofer.iosb.ilt.faaast.registry.service;

import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.registry.core.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


/**
 * Class with our error handling.
 */
@ControllerAdvice
public class RegistryControllerAdvice {

    /**
     * Handles the ResourceNotFoundException.
     *
     * @param e The desired exception.
     * @return The corresponding response.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result> handleResourceNotFoundException(Exception e) {
        Message msg = Message.builder().messageType(MessageType.ERROR).text(e.getMessage()).build();
        return new ResponseEntity<>(Result.builder().message(msg).build(), HttpStatus.NOT_FOUND);
    }


    /**
     * Handles ResourceAlreadyExistsException and BadRequestException.
     *
     * @param e The desired exception.
     * @return The corresponding response.
     */
    @ExceptionHandler(value = {
            ResourceAlreadyExistsException.class,
            BadRequestException.class
    })
    public ResponseEntity<Result> handleResourceAlreadyExistsException(Exception e) {
        Message msg = Message.builder().messageType(MessageType.ERROR).text(e.getMessage()).build();
        return new ResponseEntity<>(Result.builder().message(msg).build(), HttpStatus.BAD_REQUEST);
    }


    /**
     * Fallback method. Handles all other exceptions.
     *
     * @param e The desired exception.
     * @return The corresponding response.
     */
    public ResponseEntity<Result> handleExceptions(Exception e) {
        Message msg = Message.builder().messageType(MessageType.ERROR).text(e.getMessage()).build();
        return new ResponseEntity<>(Result.builder().message(msg).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
