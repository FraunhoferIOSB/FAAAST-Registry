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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.GenericFilterBean;


/**
 * Custom filter for applying our access rules.
 */
public class AclFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(AclFilter.class);
    private static final String API_PREFIX = "/api/v3.0/";
    private static final String INVALID_ACL_FOLDER_MSG = "Invalid ACL folder path, AAS Security will not enforce rules.)";

    private final String aclFolder;
    private final Map<Path, AllAccessPermissionRulesRoot> aclList;

    public AclFilter(String aclFolder) {
        aclList = new HashMap<>();
        this.aclFolder = aclFolder;
        readAccessRules();
        monitorAclRules();
    }


    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain chain)
            throws IOException, ServletException
    {
        LOG.info("doFilter called: Request: {}", servletRequest);
        if (servletRequest instanceof HttpServletRequest request)
        {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Map<String, Object> claims = new HashMap<>();
            if ((authentication != null) && (authentication.getCredentials() instanceof Jwt jwt))
            {
                claims = jwt.getClaims();
            }
            boolean allowed;
            synchronized(aclList) {
                allowed = filterRules(aclList, claims, request);
            }
            LOG.info("doFilter called: Request: {}; authentication: {}; allowed: {}", request, authentication, allowed);
            if (!allowed)
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else
            {
                chain.doFilter(servletRequest, servletResponse);
            }
        }
        else
        {
            chain.doFilter(servletRequest, servletResponse);
        }
    }


    private void readAccessRules() {
        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOG.error(INVALID_ACL_FOLDER_MSG);
            return;
        }

        File folder = new File(aclFolder.trim());
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        ObjectMapper mapper = new ObjectMapper();
        if (jsonFiles != null) {
            synchronized (aclList) {
                for (File file: jsonFiles) {
                    Path filePath = file.toPath().toAbsolutePath();
                    String content = null;
                    try {
                        LOG.trace("readAccessRules: add rule {}", filePath);
                        content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                        aclList.put(filePath, mapper.readValue(
                                content, AllAccessPermissionRulesRoot.class));
                    }
                    catch (IOException e) {
                        LOG.error(INVALID_ACL_FOLDER_MSG, e);
                    }
                }
            }
        }
    }


    /**
     * Check all rules that explicitly allows the request. If a rule exists
     * after all filters, true is returned
     *
     * @param claims
     * @param request
     * @return
     */
    private static boolean filterRules(Map<Path, AllAccessPermissionRulesRoot> aclList, Map<String, Object> claims, HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String path = requestPath.startsWith(API_PREFIX) ? requestPath.substring(9) : requestPath;
        String method = request.getMethod();
        List<AllAccessPermissionRulesRoot> relevantRules = aclList.values().stream()
                .filter(a -> a.getAllAccessPermissionRules()
                        .getRules().stream()
                        .anyMatch(r -> r.getACL() != null
                                && r.getACL().getATTRIBUTES() != null
                                && r.getACL().getRIGHTS() != null
                                && r.getOBJECTS() != null
                                && r.getOBJECTS().stream().anyMatch(attr -> {
                                    if (attr.getROUTE() != null) {
                                        return "*".equals(attr.getROUTE()) || attr.getROUTE().contains(path);
                                    }
                                    else if (attr.getDESCRIPTOR() != null) {
                                        return checkDescriptor(path, attr.getDESCRIPTOR());
                                    }
                                    else {
                                        return false;
                                    }
                                })
                                && "ALLOW".equals(r.getACL().getACCESS())
                                && r.getACL().getRIGHTS().contains(getRequiredRight(method))
                                && verifyAllClaims(claims, r)))
                .toList();
        return !relevantRules.isEmpty();
    }


    private static boolean verifyAllClaims(Map<String, Object> claims, Rule rule) {
        if (rule.getACL().getATTRIBUTES().stream()
                .anyMatch(attr -> "ANONYMOUS".equals(attr.getGLOBAL())
                        && Boolean.TRUE.equals(rule.getFORMULA().get("$boolean")))) {
            return true;
        }
        if (claims == null) {
            return false;
        }
        List<String> claimValues = rule.getACL().getATTRIBUTES().stream()
                .filter(attr -> attr.getGLOBAL() == null)
                .map(Attribute::getCLAIM)
                .filter(Objects::nonNull)
                .toList();
        return !claimValues.isEmpty()
                && claimValues.stream()
                        .allMatch(value -> {
                            Object claim = claims.get(value);
                            return claim != null
                                    && evaluateSimpleEQFormula(rule.getFORMULA(), value, claim.toString());
                        });
    }


    private static boolean evaluateSimpleEQFormula(Map<String, Object> formula, String value, String claimValue) {
        if (formula.size() != 1 || !formula.containsKey("$eq")) {
            LOG.error("Unsupported ACL formula.");
            return false;
        }
        List<LinkedHashMap<?, ?>> eqList = (List<LinkedHashMap<?, ?>>) formula.get("$eq");
        LinkedHashMap<?, ?> attribute = (LinkedHashMap<?, ?>) eqList.get(0).get("$attribute");
        String strVal = (String) eqList.get(1).get("$strVal");
        return attribute.get("CLAIM").equals(value) && strVal.equals(claimValue);
    }


    private static String getRequiredRight(String method) {
        switch (method) {
            case "GET":
                return "READ";
            case "POST":
                return "WRITE";
            case "PUT":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }


    private static boolean checkDescriptor(String path, String descriptor) {
        if (descriptor.startsWith("(aasDesc)")) {
            if (!path.startsWith("/shell-descriptors")) {
                return false;
            }
            if (descriptor.equals("(aasDesc)*")) {
                return true;
            }
            else if (descriptor.startsWith("(aasDesc)")) {
                String id = descriptor.substring(9);
                return path.contains(EncodingHelper.base64UrlEncode(id));
            }
        }
        else if (descriptor.startsWith("(smDesc)")) {
            if (!path.startsWith("/submodel-descriptors")) {
                return false;
            }
            if (descriptor.equals("(smDesc)*")) {
                return true;
            }
            else if (descriptor.startsWith("(smDesc)")) {
                String id = descriptor.substring(8);
                return path.contains(EncodingHelper.base64UrlEncode(id));
            }
        }
        return false;
    }


    private void monitorAclRules() {
        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOG.error(INVALID_ACL_FOLDER_MSG);
            return;
        }
        Path folderToWatch = Paths.get(aclFolder);
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            // Register the folder with the WatchService for CREATE and DELETE events
            folderToWatch.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            monitorLoop(watchService, folderToWatch);
        }
        catch (IOException e) {
            LOG.error(INVALID_ACL_FOLDER_MSG);
        }

    }


    private void monitorLoop(WatchService watchService, Path folderToWatch) {
        ObjectMapper mapper = new ObjectMapper();
        Thread monitoringThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey watchKey = null;
                try {
                    watchKey = watchService.take();
                }
                catch (InterruptedException e) {
                    LOG.error(INVALID_ACL_FOLDER_MSG);
                }
                boolean valid;
                if (watchKey != null) {
                    for (var event: watchKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path filePath = (Path) event.context();
                        Path absolutePath = folderToWatch.resolve(filePath).toAbsolutePath();
                        // Check if the file is a JSON file
                        if (filePath.toString().toLowerCase().endsWith(".json")) {
                            if ((kind == StandardWatchEventKinds.ENTRY_CREATE) || (kind == StandardWatchEventKinds.ENTRY_MODIFY)) {
                                try {
                                    synchronized (aclList) {
                                        aclList.put(absolutePath, mapper.readValue(
                                                new String(Files.readAllBytes(absolutePath), StandardCharsets.UTF_8), AllAccessPermissionRulesRoot.class));
                                    }
                                }
                                catch (IOException e) {
                                    LOG.error(INVALID_ACL_FOLDER_MSG);
                                }
                                LOG.info("Added new ACL rule {}", filePath);
                            }
                            else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                synchronized (aclList) {
                                    if (aclList.remove(absolutePath) != null) {
                                        LOG.info("Removed ACL rule {}", filePath);
                                    }
                                    else {
                                        LOG.warn("ACL rule not found: {}", filePath);
                                    }
                                }
                            }
                        }
                    }
                    // Reset the key to receive further watch events
                    valid = watchKey.reset();
                }
                else {
                    valid = false;
                }
                if (!valid) {
                    LOG.warn("monitorLoop: WatchKey no longer valid; exiting.");
                    break;
                }
            }
        });
        monitoringThread.start();
    }
}
