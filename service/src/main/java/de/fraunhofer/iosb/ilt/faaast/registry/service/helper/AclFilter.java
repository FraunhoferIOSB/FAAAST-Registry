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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.registry.service.config.ControllerConfig;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.Defacl;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.Defattribute;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.Defformula;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.Defobject;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.registry.service.query.json.RightsEnum;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private final Map<Path, AllAccessPermissionRules> aclList;

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
                String jsonContent;
                try {
                    LOG.trace("readAccessRules: add rule {}", filePath);
                    jsonContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                    JsonNode rootNode = mapper.readTree(jsonContent);
                    AllAccessPermissionRules allRules;
                    if (rootNode.has("AllAccessPermissionRules")) {
                        allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
                    }
                    else {
                        allRules = mapper.readValue(jsonContent, AllAccessPermissionRules.class);
                    }
                    aclList.put(filePath, allRules);
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
    private static boolean filterRules(Map<Path, AllAccessPermissionRules> aclList, Map<String, Object> claims, HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String path = requestPath.startsWith(ControllerConfig.getApiPrefix()) ? requestPath.substring(9) : requestPath;
        String method = request.getMethod();
        List<AllAccessPermissionRules> relevantRules = aclList.values().stream()
                .filter(a -> a.getRules().stream()
                        .anyMatch(r -> evaluateRule(r, path, method, claims, a)))
                .toList();
        return !relevantRules.isEmpty();
    }


    private static boolean evaluateRule(AccessPermissionRule rule, String path, String method, Map<String, Object> claims, AllAccessPermissionRules allAccess) {
        Acl acl = getAcl(rule, allAccess);
        return acl != null
                && getAttributes(acl, allAccess) != null
                && acl.getRights() != null
                && getObjects(rule, allAccess) != null
                && getObjects(rule, allAccess).stream().anyMatch(attr -> {
                    if (attr.getRoute() != null) {
                        return "*".equals(attr.getRoute()) || attr.getRoute().contains(path);
                    }
                    else if (attr.getDescriptor() != null) {
                        return checkDescriptor(path, attr.getDescriptor());
                    }
                    else {
                        return false;
                    }
                })
                && Acl.Access.ALLOW.equals(acl.getAccess())
                && evaluateRights(acl.getRights(), method, path)
                && verifyAllClaims(claims, rule, allAccess);
    }


    private static Acl getAcl(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if (rule.getAcl() != null) {
            return rule.getAcl();
        }
        else if (rule.getUseacl() != null) {
            Optional<Defacl> acl = allAccess.getDefacls().stream()
                    .filter(a -> Objects.equals(a.getName(), rule.getUseacl()))
                    .findAny();
            if (acl.isPresent()) {
                return acl.get().getAcl();
            }
            else {
                throw new IllegalArgumentException("DEFACL not found: " + rule.getUseacl());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ACL or USEACL must be specified");
        }
    }


    private static List<AttributeItem> getAttributes(Acl acl, AllAccessPermissionRules allAccess) {
        if ((acl.getAttributes() != null) && (!acl.getAttributes().isEmpty())) {
            return acl.getAttributes();
        }
        else if (acl.getUseattributes() != null) {
            Optional<Defattribute> attribute = allAccess.getDefattributes().stream()
                    .filter(a -> Objects.equals(a.getName(), acl.getUseattributes()))
                    .findAny();
            if (attribute.isPresent()) {
                return attribute.get().getAttributes();
            }
            else {
                throw new IllegalArgumentException("DEFATTRIBUTES not found: " + acl.getUseattributes());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ATTRIBUTES or USEATTRIBUTES must be specified");
        }
    }


    //private static Map<String, Object> getFormula(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
    private static LogicalExpression getFormula(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if (rule.getFormula() != null) {
            return rule.getFormula();
        }
        else if (rule.getUseformula() != null) {
            Optional<Defformula> formula = allAccess.getDefformulas().stream()
                    .filter(a -> Objects.equals(a.getName(), rule.getUseformula()))
                    .findAny();
            if (formula.isPresent()) {
                return formula.get().getFormula();
            }
            else {
                throw new IllegalArgumentException("DEFFORMULA not found: " + rule.getUseformula());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: FORMULA or USEFORMULA must be specified");
        }
    }


    private static List<ObjectItem> getObjects(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if ((rule.getObjects() != null) && (!rule.getObjects().isEmpty())) {
            return rule.getObjects();
        }
        else if (rule.getUseobjects() != null) {
            // We must collect all Defobjects in all Useobjects
            List<Defobject> objectList = allAccess.getDefobjects().stream()
                    .filter(a -> rule.getUseobjects().contains(a.getName()))
                    .toList();
            if (objectList.isEmpty()) {
                throw new IllegalArgumentException("DEFOBJECTS not found: " + rule.getUseobjects());
            }
            else {
                Set<ObjectItem> retval = new HashSet<>();
                for (Defobject item: objectList) {
                    retval.addAll(item.getObjects());
                }
                return retval.stream().toList();
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: OBJECTS or USEOBJECTS must be specified");
        }
    }


    private static boolean verifyAllClaims(Map<String, Object> claims, AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        Acl acl = getAcl(rule, allAccess);
        if (getAttributes(acl, allAccess).stream()
                .anyMatch(attr -> AttributeItem.Global.ANONYMOUS.equals(attr.getGlobal())
                        && Boolean.TRUE.equals(getFormula(rule, allAccess).get$boolean()))) {
            return true;
        }
        if (claims == null) {
            return false;
        }

        //List<AttributeItem> attributes = getAttributes(getAcl(rule, allAccess), allAccess);
        //attributes = attributes.stream()
        //        .filter(attr -> attr.getGlobal() == null)
        //        .toList();
        //List<String> claimValues2 = attributes.stream()
        //        .map(AttributeItem::getClaim)
        //        .toList();

        List<String> claimValues = getAttributes(getAcl(rule, allAccess), allAccess).stream()
                .filter(attr -> attr.getGlobal() == null)
                .map(AttributeItem::getClaim)
                .filter(java.util.Objects::nonNull)
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
                            return evaluateFormula(getFormula(rule, allAccess), claimList);
                        });
    }


    private static boolean evaluateFormula(LogicalExpression formula,
                                           Map<String, String> claims) {
        Map<String, Object> ctx = new HashMap<>();
        for (var c: claims.entrySet()) {
            ctx.put("CLAIM:" + c.getKey(), c.getValue());
            LOG.trace("evaluateFormula: claimName: {}; claimValue: {}", c.getKey(), c.getValue());
        }
        ctx.put("UTCNOW", LocalTime.now(Clock.systemUTC())); // $GLOBAL → UTCNOW
        boolean retval = FormulaEvaluator.evaluate(formula, ctx);
        LOG.trace("evaluateFormula: CTX: {}: Ergebnis: {}", ctx.size(), retval);
        return retval;
    }


    private static boolean evaluateRights(List<RightsEnum> aclRights, String method, String path) {
        // We need the path to check if the request is an operation invocation (EXECUTE)
        String requiredRight = isOperationRequest(method, path) ? "EXECUTE" : getRequiredRight(method);

        return aclRights.contains(RightsEnum.ALL) || aclRights.contains(RightsEnum.valueOf(requiredRight));
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
            if ("(aasDesc)*".equals(descriptor)) {
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
            if ("(smDesc)*".equals(descriptor)) {
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
        WatchService watchService;
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
                WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupt status
                    LOG.warn("ACL monitoring thread interrupted", e);
                    break; // exit loop
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
                                    String jsonContent = new String(Files.readAllBytes(absolutePath), StandardCharsets.UTF_8);
                                    JsonNode rootNode = mapper.readTree(jsonContent);
                                    AllAccessPermissionRules allRules;
                                    if (rootNode.has("AllAccessPermissionRules")) {
                                        allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
                                    }
                                    else {
                                        allRules = mapper.readValue(jsonContent, AllAccessPermissionRules.class);
                                    }
                                    aclList.put(absolutePath, allRules);
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
                    LOG.info("monitorLoop: WatchKey no longer valid; exiting.");
                    break;
                }
            }
        });
        monitoringThread.start();
    }
}
