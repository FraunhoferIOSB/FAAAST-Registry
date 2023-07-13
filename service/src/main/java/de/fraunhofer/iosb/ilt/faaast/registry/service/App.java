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

    /**
     * Entry point of the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
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
}
