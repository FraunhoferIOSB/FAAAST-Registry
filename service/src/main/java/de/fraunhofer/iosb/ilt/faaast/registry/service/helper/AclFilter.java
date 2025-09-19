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
import de.fraunhofer.iosb.ilt.faaast.registry.service.config.ControllerConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.ACL;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.DefACL;
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
import java.time.Clock;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.GenericFilterBean;


/**
 * Custom filter for applying our access rules.
 */
public class AclFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(AclFilter.class);
    private static final String INVALID_ACL_FOLDER_MSG = "Invalid ACL folder path, AAS Security will not enforce rules.)";

    private final String aclFolder;
    private final Map<Path, AllAccessPermissionRulesRoot> aclList;

    public AclFilter(String aclFolder) {
        aclList = new ConcurrentHashMap<>();
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
            allowed = filterRules(aclList, claims, request);
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
        String path = requestPath.startsWith(ControllerConfig.getApiPrefix()) ? requestPath.substring(9) : requestPath;
        String method = request.getMethod();
        List<AllAccessPermissionRulesRoot> relevantRules = aclList.values().stream()
                .filter(a -> a.getAllAccessPermissionRules()
                        .getRules().stream()
                        .anyMatch(r -> evaluateRule(r, path, method, claims, a.getAllAccessPermissionRules())))
                .toList();
        return !relevantRules.isEmpty();
    }


    private static boolean evaluateRule(Rule rule, String path, String method, Map<String, Object> claims, AllAccessPermissionRules allAccess) {
        ACL acl = getAcl(rule, allAccess);
        return acl != null
                && acl.getATTRIBUTES() != null
                && acl.getRIGHTS() != null
                && rule.getOBJECTS() != null
                && rule.getOBJECTS().stream().anyMatch(attr -> {
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
                && "ALLOW".equals(acl.getACCESS())
                && evaluateRights(acl.getRIGHTS(), method, path)
                && verifyAllClaims(claims, rule, allAccess);
    }


    private static ACL getAcl(Rule rule, AllAccessPermissionRules allAccess) {
        if (rule.getACL() != null) {
            return rule.getACL();
        }
        else if (rule.getUSEACL() != null) {
            Optional<DefACL> acl = allAccess.getDEFACLS().stream().filter(a -> (a.getName() == null ? a.getName() == null : a.getName().equals(a.getName()))).findAny();
            if (acl.isPresent()) {
                return acl.get().getAcl();
            }
            else {
                throw new IllegalArgumentException("DEFACL not found: " + rule.getUSEACL());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ACL or USEACL must be specified");
        }
    }


    private static boolean verifyAllClaims(Map<String, Object> claims, Rule rule, AllAccessPermissionRules allAccess) {
        ACL acl = getAcl(rule, allAccess);
        if (acl.getATTRIBUTES().stream()
                .anyMatch(attr -> "ANONYMOUS".equals(attr.getGLOBAL())
                        && Boolean.TRUE.equals(rule.getFORMULA().get("$boolean")))) {
            return true;
        }
        if (claims == null) {
            return false;
        }
        List<String> claimValues = getAcl(rule, allAccess).getATTRIBUTES().stream()
                .filter(attr -> attr.getGLOBAL() == null)
                .map(Attribute::getCLAIM)
                .filter(Objects::nonNull)
                .toList();
        LOG.trace("verifyAllClaims: Anz {}", claimValues.size());
        Map<String, String> claimList = new HashMap<>();
        for (String val: claimValues) {
            Object claim = claims.get(val);
            if (claim != null) {
                claimList.put(val, claim.toString());
            }
        }
        return !claimValues.isEmpty()
                && claimValues.stream()
                        .allMatch(value -> {
                            //Object claim = claims.get(value);
                            //return claim != null
                            return evaluateFormula(rule.getFORMULA(), claimList);
                        });
    }


    private static boolean evaluateFormula(Map<String, Object> formula,
                                           Map<String, String> claims) {
        Map<String, Object> ctx = new HashMap<>();
        for (var c: claims.entrySet()) {
            ctx.put("CLAIM:" + c.getKey(), c.getValue());
            LOG.trace("evaluateFormula: claimName: {}; claimValue: {}", c.getKey(), c.getValue());
        }
        //ctx.put("CLAIM:" + claimName, claimValue);
        ctx.put("UTCNOW", LocalTime.now(Clock.systemUTC())); // $GLOBAL → UTCNOW
        boolean retval = FormulaEvaluator.evaluate(formula, ctx);
        LOG.trace("evaluateFormula: CTX: {}: Ergebnis: {}", ctx.size(), retval);
        return retval;
    }


    private static boolean evaluateRights(List<String> aclRights, String method, String path) {
        // We need the path to check if the request is an operation invocation (EXECUTE)
        String requiredRight = isOperationRequest(method, path) ? "EXECUTE" : getRequiredRight(method);

        return aclRights.contains("ALL") || aclRights.contains(requiredRight);
    }


    private static boolean isOperationRequest(String method, String path) {
        // Requirements for an operation request according to FAAAST docs:
        // Method: POST, URL suffix: /invoke, /invoke-async, /invoke/$value, /invoke-async/$value
        String cleanPath;
        String[] pathParts = path.split("/");

        if (pathParts.length > 1 && "$value".equals(pathParts[pathParts.length - 1])) {
            cleanPath = pathParts[pathParts.length - 2];
        }
        else {
            cleanPath = pathParts[pathParts.length - 1];
        }

        return HttpMethod.POST.name().equals(method) && ("/invoke".equals(cleanPath) || "invoke-async".equals(path));
    }


    private static String getRequiredRight(String method) {
        return switch (method) {
            case "GET" -> "READ";
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
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
                                    aclList.put(absolutePath, mapper.readValue(
                                            new String(Files.readAllBytes(absolutePath), StandardCharsets.UTF_8), AllAccessPermissionRulesRoot.class));
                                }
                                catch (IOException e) {
                                    LOG.error(INVALID_ACL_FOLDER_MSG);
                                }
                                LOG.info("Added new ACL rule {}", filePath);
                            }
                            else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                if (aclList.remove(absolutePath) != null) {
                                    LOG.info("Removed ACL rule {}", filePath);
                                }
                                else {
                                    LOG.warn("ACL rule not found: {}", filePath);
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
