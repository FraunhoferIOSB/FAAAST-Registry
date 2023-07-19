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

import ch.qos.logback.classic.Level;
import de.fraunhofer.iosb.ilt.faaast.registry.service.logging.FaaastFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import java.io.PrintStream;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;


/**
 * Main application of the registry.
 */
@SpringBootApplication
@EntityScan(basePackages = {
        "de.fraunhofer.iosb.ilt.faaast.service.model.descriptor"
})
@ImportResource("classpath:applicationContext.xml")
public class App {
    // Reduces log output (ERROR for FA³ST packages, ERROR for all other packages). Default information about the starting process will still be printed.
    private static final String QUITE_OPTION = "-q";
    // Enables verbose logging (INFO for FA³ST packages, WARN for all other packages).
    private static final String VERBOSE_OPTION = "-v";
    // Enables very verbose logging (DEBUG for FA³ST packages, INFO for all other packages).
    private static final String VERY_VERBOSE_OPTION = "-vv";
    // Enables very very verbose logging (TRACE for FA³ST packages, DEBUG for all other packages).
    private static final String VERY_VERY_VERBOSE_OPTION = "-vvv";

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    /**
     * Entry point of the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        configureLogging(args);
        SpringApplication.run(App.class, args);
        new SpringApplicationBuilder(App.class)
                .bannerMode(Mode.CONSOLE)
                .banner(App::printBanner)
                .run(args);
    }


    private static void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println("            _____                                                       ");
        out.println("           |___ /                                                       ");
        out.println(" ______      |_ \\   _____ _______     _____            _     _              ");
        out.println("|  ____/\\   ___) | / ____|__   __|   |  __ \\          (_)   | |             ");
        out.println("| |__ /  \\ |____/ | (___    | |      | |__) |___  __ _ _ ___| |_ _ __ _   _ ");
        out.println("|  __/ /\\ \\        \\___ \\   | |      |  _  // _ \\/ _` | / __| __| '__| | | |");
        out.println("| | / ____ \\       ____) |  | |      | | \\ \\  __/ (_| | \\__ \\ |_| |  | |_| |");
        out.println("|_|/_/    \\_\\     |_____/   |_|      |_|  \\_\\___|\\__, |_|___/\\__|_|   \\__, |");
        out.println("                                                  __/ |                __/ |");
        out.println("                                                 |___/                |___/ ");
        out.println("----------------------------------------------------------------------------");
        out.println();
        out.println("FA³ST Registry is now running...");
    }


    private static void configureLogging(String[] args) {
        for (String arg: args) {
            if (arg.equals(VERY_VERY_VERBOSE_OPTION)) {
                FaaastFilter.setLevelFaaast(Level.TRACE);
                FaaastFilter.setLevelExternal(Level.DEBUG);
                break;
            }
            else if (arg.equals(VERY_VERBOSE_OPTION)) {
                FaaastFilter.setLevelFaaast(Level.DEBUG);
                FaaastFilter.setLevelExternal(Level.INFO);
                break;
            }
            else if (arg.equals(VERBOSE_OPTION)) {
                FaaastFilter.setLevelFaaast(Level.INFO);
                FaaastFilter.setLevelExternal(Level.WARN);
                break;
            }
            else if (arg.equals(QUITE_OPTION)) {
                FaaastFilter.setLevelFaaast(Level.ERROR);
                FaaastFilter.setLevelExternal(Level.ERROR);
                break;
            }
        }

        LOGGER.info("Using log level for FA³ST packages: {}", FaaastFilter.getLevelFaaast());
        LOGGER.info("Using log level for external packages: {}", FaaastFilter.getLevelExternal());
    }
}
