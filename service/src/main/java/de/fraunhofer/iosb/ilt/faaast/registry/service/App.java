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

import static de.fraunhofer.iosb.ilt.faaast.registry.service.App.APP_NAME;

import ch.qos.logback.classic.Level;
import de.fraunhofer.iosb.ilt.faaast.registry.service.logging.FaaastFilter;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 * Main application of the registry.
 */
@SpringBootApplication
@EnableAsync
@EntityScan(basePackages = {
        "de.fraunhofer.iosb.ilt.faaast.service.model.descriptor"
})
@ImportResource("classpath:applicationContext.xml")
@Command(name = APP_NAME, mixinStandardHelpOptions = true, description = "Starts a FA³ST Registry", usageHelpAutoWidth = true)
public class App implements Runnable {
    protected static final String APP_NAME = "FA³ST Registry Starter";
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Option(names = {
            "-q",
            "--quiet"
    }, description = "Reduces log output (ERROR for FA³ST packages, ERROR for all other packages). Default information about the starting process will still be printed.")
    public boolean quiet = false;

    @Option(names = {
            "-v",
            "--verbose"
    }, description = "Enables verbose logging (INFO for FA³ST packages, WARN for all other packages).")
    public boolean verbose = false;

    @Option(names = "-vv", description = "Enables very verbose logging (DEBUG for FA³ST packages, INFO for all other packages).")
    public boolean veryVerbose = false;

    @Option(names = "-vvv", description = "Enables very very verbose logging (TRACE for FA³ST packages, DEBUG for all other packages).")
    public boolean veryVeryVerbose = false;

    /**
     * Entry point of the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        new CommandLine(new App()).execute(args);

        new SpringApplicationBuilder(App.class)
                .bannerMode(Mode.CONSOLE)
                .banner(App::printBanner)
                .run(args);
    }


    @Override
    public void run() {
        configureLogging();
    }


    private static void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        LOGGER.info("            _____                                                       ");
        LOGGER.info("           |___ /                                                       ");
        LOGGER.info(" ______      |_ \\   _____ _______     _____            _     _              ");
        LOGGER.info("|  ____/\\   ___) | / ____|__   __|   |  __ \\          (_)   | |             ");
        LOGGER.info("| |__ /  \\ |____/ | (___    | |      | |__) |___  __ _ _ ___| |_ _ __ _   _ ");
        LOGGER.info("|  __/ /\\ \\        \\___ \\   | |      |  _  // _ \\/ _` | / __| __| '__| | | |");
        LOGGER.info("| | / ____ \\       ____) |  | |      | | \\ \\  __/ (_| | \\__ \\ |_| |  | |_| |");
        LOGGER.info("|_|/_/    \\_\\     |_____/   |_|      |_|  \\_\\___|\\__, |_|___/\\__|_|   \\__, |");
        LOGGER.info("                                                  __/ |                __/ |");
        LOGGER.info("                                                 |___/                |___/ ");
        LOGGER.info("----------------------------------------------------------------------------");
        LOGGER.info("");
        LOGGER.info("FA³ST Registry is now running...");
    }


    private void configureLogging() {
        if (veryVeryVerbose) {
            FaaastFilter.setLevelFaaast(Level.TRACE);
            FaaastFilter.setLevelExternal(Level.TRACE);
        }
        else if (veryVerbose) {
            FaaastFilter.setLevelFaaast(Level.TRACE);
            FaaastFilter.setLevelExternal(Level.DEBUG);
        }
        else if (verbose) {
            FaaastFilter.setLevelFaaast(Level.DEBUG);
            FaaastFilter.setLevelExternal(Level.INFO);
        }
        else if (quiet) {
            FaaastFilter.setLevelFaaast(Level.WARN);
            FaaastFilter.setLevelExternal(Level.ERROR);
        }

        LOGGER.info("Using log level for FA³ST packages: {}", FaaastFilter.getLevelFaaast());
        LOGGER.info("Using log level for external packages: {}", FaaastFilter.getLevelExternal());
    }
}
