package com.ysalu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysalu.web.auth.SessionUser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TestYApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void overviewEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required."));
    }

    @Test
    void overviewEndpointStillReturnsCorsHeadersForFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/overview")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void documentLifecycleSupportsVersionsRestoreAndDelete() throws Exception {
        MockHttpSession session = registerAndLogin("writer01", "writer01@example.com", "secret01");

        Map<String, String> createDocumentPayload = new HashMap<String, String>();
        createDocumentPayload.put("title", "测试文档");
        createDocumentPayload.put("content", "# 初始版本\n用于测试文档创建。");

        MvcResult createResult = mockMvc.perform(post("/api/docs?mode=MANUAL").session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDocumentPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("测试文档"))
                .andReturn();

        Map<String, Object> createdDocument = readMap(createResult);
        Long documentId = ((Number) createdDocument.get("id")).longValue();

        Map<String, String> autosavePayload = new HashMap<String, String>();
        autosavePayload.put("title", "测试文档");
        autosavePayload.put("content", "# 自动保存版本\n用于测试版本列表。");

        mockMvc.perform(put("/api/docs/" + documentId + "?mode=AUTOSAVE").session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autosavePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("# 自动保存版本\n用于测试版本列表。"));

        MvcResult versionsResult = mockMvc.perform(get("/api/docs/" + documentId + "/versions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionNumber").value(2))
                .andExpect(jsonPath("$[0].sourceType").value("AUTOSAVE"))
                .andExpect(jsonPath("$[1].sourceType").value("MANUAL"))
                .andReturn();

        List<Map<String, Object>> versions = readList(versionsResult);
        Long firstVersionId = ((Number) versions.get(1).get("id")).longValue();

        mockMvc.perform(post("/api/docs/" + documentId + "/versions/" + firstVersionId + "/restore").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("# 初始版本\n用于测试文档创建。"));

        mockMvc.perform(delete("/api/docs/" + documentId).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/docs").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void documentsAreVisibleAcrossUsersButRemainOwnerManaged() throws Exception {
        MockHttpSession authorSession = registerAndLogin("writer02", "writer02@example.com", "secret01");
        MockHttpSession readerSession = registerAndLogin("reader02", "reader02@example.com", "secret01");

        Map<String, String> createDocumentPayload = new HashMap<String, String>();
        createDocumentPayload.put("title", "Shared Doc");
        createDocumentPayload.put("content", "# Shared Content");

        MvcResult createResult = mockMvc.perform(post("/api/docs?mode=MANUAL").session(authorSession)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDocumentPayload)))
                .andExpect(status().isOk())
                .andReturn();

        Long documentId = ((Number) readMap(createResult).get("id")).longValue();

        MvcResult listResult = mockMvc.perform(get("/api/docs").session(readerSession))
                .andExpect(status().isOk())
                .andReturn();
        List<Map<String, Object>> documents = readList(listResult);
        boolean foundSharedDocument = false;
        for (Map<String, Object> document : documents) {
            if (((Number) document.get("id")).longValue() == documentId.longValue()
                    && "Shared Doc".equals(document.get("title"))) {
                foundSharedDocument = true;
                break;
            }
        }
        assertTrue(foundSharedDocument);

        mockMvc.perform(get("/api/docs/" + documentId).session(readerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Shared Doc"))
                .andExpect(jsonPath("$.content").value("# Shared Content"));

        mockMvc.perform(get("/api/docs/" + documentId + "/versions").session(readerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceType").value("MANUAL"));

        Map<String, String> updateDocumentPayload = new HashMap<String, String>();
        updateDocumentPayload.put("title", "Shared Doc Updated");
        updateDocumentPayload.put("content", "# Updated By Reader");

        mockMvc.perform(put("/api/docs/" + documentId + "?mode=MANUAL").session(readerSession)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDocumentPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Document does not exist."));
    }

    @Test
    void authEndpointsSupportRegistrationSessionLoginResetAndAuditing() throws Exception {
        Map<String, String> registerPayload = new HashMap<String, String>();
        registerPayload.put("username", "tester02");
        registerPayload.put("email", "tester02@example.com");
        registerPayload.put("password", "secret01");
        registerPayload.put("displayName", "Tester Two");
        registerPayload.put("phoneNumber", "13800000000");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("tester02"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        Map<String, String> loginPayload = new HashMap<String, String>();
        loginPayload.put("usernameOrEmail", "tester02");
        loginPayload.put("password", "secret01");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.10")
                        .header(HttpHeaders.USER_AGENT, "JUnit-Test")
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("tester02@example.com"))
                .andExpect(jsonPath("$.lastLoginIp").value("203.0.113.10"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionUser.SESSION_USER_ATTRIBUTE);
        assertNotNull(sessionUser);
        assertNotNull(sessionUser.getPermissions());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("tester02"));

        mockMvc.perform(get("/api/overview").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationName").value("TestY"));

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied: admin:users:read"));

        Map<String, String> resetPayload = new HashMap<String, String>();
        resetPayload.put("username", "tester02");
        resetPayload.put("email", "tester02@example.com");
        resetPayload.put("newPassword", "secret02");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful."));

        loginPayload.put("password", "secret01");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.11")
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        loginPayload.put("password", "secret02");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.12")
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.lastLoginIp").value("203.0.113.12"));

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful."));

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void operationLogsCaptureAuthAndDocumentActions() throws Exception {
        MockHttpSession session = registerAndLogin("logger01", "logger01@example.com", "secret01");

        Map<String, String> createDocumentPayload = new HashMap<String, String>();
        createDocumentPayload.put("title", "Log Demo");
        createDocumentPayload.put("content", "# First Version");

        MvcResult createResult = mockMvc.perform(post("/api/docs?mode=MANUAL").session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDocumentPayload)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> createdDocument = readMap(createResult);
        Long documentId = ((Number) createdDocument.get("id")).longValue();

        Map<String, String> updateDocumentPayload = new HashMap<String, String>();
        updateDocumentPayload.put("title", "Log Demo Updated");
        updateDocumentPayload.put("content", "# Second Version");

        mockMvc.perform(put("/api/docs/" + documentId + "?mode=AUTOSAVE").session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDocumentPayload)))
                .andExpect(status().isOk());

        MockHttpSession rootSession = loginAsRoot();
        MvcResult operationLogsResult = mockMvc.perform(get("/api/admin/operation-logs").session(rootSession)
                        .param("operatorUsername", "logger01")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> page = readMap(operationLogsResult);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> logs = (List<Map<String, Object>>) page.get("content");
        boolean hasRegisterLog = false;
        boolean hasCreateLog = false;
        boolean hasUpdateLog = false;
        for (Map<String, Object> log : logs) {
            if ("AUTH".equals(log.get("module")) && "USER_REGISTERED".equals(log.get("action"))) {
                hasRegisterLog = true;
            }
            if ("DOCS".equals(log.get("module")) && "DOCUMENT_CREATED".equals(log.get("action"))) {
                hasCreateLog = true;
            }
            if ("DOCS".equals(log.get("module")) && "DOCUMENT_UPDATED".equals(log.get("action"))) {
                hasUpdateLog = true;
            }
        }
        assertTrue(hasRegisterLog);
        assertTrue(hasCreateLog);
        assertTrue(hasUpdateLog);
    }

    @Test
    void rootAdminCanManageRolesUsersAndAudits() throws Exception {
        registerUser("audituser", "audituser@example.com", "secret01");

        Map<String, String> failedLoginPayload = new HashMap<String, String>();
        failedLoginPayload.put("usernameOrEmail", "audituser");
        failedLoginPayload.put("password", "wrongpass");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .header("X-Forwarded-For", "198.51.100.50")
                            .content(objectMapper.writeValueAsString(failedLoginPayload)))
                    .andExpect(status().isBadRequest());
        }

        MockHttpSession rootSession = loginAsRoot();

        MvcResult permissionsResult = mockMvc.perform(get("/api/admin/permissions").session(rootSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").exists())
                .andReturn();
        List<Map<String, Object>> permissions = readList(permissionsResult);
        Long overviewPermissionId = findPermissionId(permissions, "overview:read");
        Long docsReadPermissionId = findPermissionId(permissions, "docs:read");
        Long docsWritePermissionId = findPermissionId(permissions, "docs:write");

        Map<String, Object> createRolePayload = new HashMap<String, Object>();
        createRolePayload.put("code", "EDITOR");
        createRolePayload.put("name", "Editor");
        createRolePayload.put("description", "Can read shared docs and manage personal docs");
        createRolePayload.put(
                "permissionIds",
                java.util.Arrays.asList(overviewPermissionId, docsReadPermissionId, docsWritePermissionId)
        );

        MvcResult roleResult = mockMvc.perform(post("/api/admin/roles").session(rootSession)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRolePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EDITOR"))
                .andExpect(jsonPath("$.permissions[0]").exists())
                .andReturn();
        Map<String, Object> createdRole = readMap(roleResult);
        Long roleId = ((Number) createdRole.get("id")).longValue();

        registerUser("editor01", "editor01@example.com", "secret01");

        MvcResult usersResult = mockMvc.perform(get("/api/admin/users").session(rootSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists())
                .andReturn();
        List<Map<String, Object>> users = readList(usersResult);
        Long editorUserId = findUserId(users, "editor01");

        Map<String, Object> updateUserRolesPayload = new HashMap<String, Object>();
        updateUserRolesPayload.put("roleIds", java.util.Arrays.asList(roleId));

        mockMvc.perform(put("/api/admin/users/" + editorUserId + "/roles").session(rootSession)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRolesPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("EDITOR"));

        Map<String, Object> updateUserStatusPayload = new HashMap<String, Object>();
        updateUserStatusPayload.put("accountStatus", "LOCKED");

        mockMvc.perform(put("/api/admin/users/" + editorUserId + "/status").session(rootSession)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserStatusPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("LOCKED"));

        Map<String, String> loginPayload = new HashMap<String, String>();
        loginPayload.put("usernameOrEmail", "editor01");
        loginPayload.put("password", "secret01");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is locked."));

        mockMvc.perform(get("/api/admin/login-audits").session(rootSession)
                        .param("success", "false")
                        .param("principal", "audituser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].principal").value("audituser"))
                .andExpect(jsonPath("$.content[0].success").value(false))
                .andExpect(jsonPath("$.totalElements").value(3));

        MvcResult alertResult = mockMvc.perform(get("/api/admin/login-audits/alerts").session(rootSession))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> alerts = readMap(alertResult);
        Number failedAttemptsLast24Hours = (Number) alerts.get("failedAttemptsLast24Hours");
        assertNotNull(failedAttemptsLast24Hours);
        assertTrue(failedAttemptsLast24Hours.longValue() >= 3L);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> suspiciousPrincipals = (List<Map<String, Object>>) alerts.get("suspiciousPrincipals");
        boolean containsAuditUser = false;
        for (Map<String, Object> principalAlert : suspiciousPrincipals) {
            if ("audituser".equals(principalAlert.get("principal"))
                    && ((Number) principalAlert.get("failureCount")).longValue() >= 3L) {
                containsAuditUser = true;
                break;
            }
        }
        assertTrue(containsAuditUser);

        MvcResult operationLogsResult = mockMvc.perform(get("/api/admin/operation-logs").session(rootSession)
                        .param("module", "ADMIN")
                        .param("operatorUsername", "rootadmin")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> page = readMap(operationLogsResult);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> logs = (List<Map<String, Object>>) page.get("content");
        boolean hasRoleCreateLog = false;
        boolean hasRoleUpdateLog = false;
        boolean hasStatusUpdateLog = false;
        for (Map<String, Object> log : logs) {
            if ("ROLE_CREATED".equals(log.get("action"))) {
                hasRoleCreateLog = true;
            }
            if ("USER_ROLES_UPDATED".equals(log.get("action"))) {
                hasRoleUpdateLog = true;
            }
            if ("USER_STATUS_UPDATED".equals(log.get("action"))) {
                hasStatusUpdateLog = true;
            }
        }
        assertTrue(hasRoleCreateLog);
        assertTrue(hasRoleUpdateLog);
        assertTrue(hasStatusUpdateLog);
    }

    @Test
    void adminUsersEndpointToleratesInvalidAccountStatusValues() throws Exception {
        registerUser("broken1", "broken1@example.com", "secret01");
        jdbcTemplate.update("update user_account set account_status = ? where username = ?", "BROKEN_STATUS", "broken1");

        MockHttpSession rootSession = loginAsRoot();

        MvcResult usersResult = mockMvc.perform(get("/api/admin/users").session(rootSession))
                .andExpect(status().isOk())
                .andReturn();

        List<Map<String, Object>> users = readList(usersResult);
        boolean foundBrokenUser = false;
        for (Map<String, Object> user : users) {
            if ("broken1".equals(user.get("username")) && "UNKNOWN".equals(user.get("accountStatus"))) {
                foundBrokenUser = true;
                break;
            }
        }
        assertTrue(foundBrokenUser);
    }

    private MockHttpSession registerAndLogin(String username, String email, String password) throws Exception {
        registerUser(username, email, password);
        Map<String, String> loginPayload = new HashMap<String, String>();
        loginPayload.put("usernameOrEmail", username);
        loginPayload.put("password", password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    private void registerUser(String username, String email, String password) throws Exception {
        Map<String, String> registerPayload = new HashMap<String, String>();
        registerPayload.put("username", username);
        registerPayload.put("email", email);
        registerPayload.put("password", password);
        registerPayload.put("displayName", username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private MockHttpSession loginAsRoot() throws Exception {
        Map<String, String> loginPayload = new HashMap<String, String>();
        loginPayload.put("usernameOrEmail", "rootadmin");
        loginPayload.put("password", "Root@123456");

        MvcResult rootLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .header("X-Forwarded-For", "198.51.100.20")
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.rootAdmin").value(true))
                .andExpect(jsonPath("$.roles[0]").value("ROOT"))
                .andReturn();
        return (MockHttpSession) rootLogin.getRequest().getSession(false);
    }

    private Map<String, Object> readMap(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
    }

    private List<Map<String, Object>> readList(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private Long findPermissionId(List<Map<String, Object>> permissions, String code) {
        for (Map<String, Object> permission : permissions) {
            if (code.equals(permission.get("code"))) {
                return ((Number) permission.get("id")).longValue();
            }
        }
        throw new AssertionError("Permission not found: " + code);
    }

    private Long findUserId(List<Map<String, Object>> users, String username) {
        for (Map<String, Object> user : users) {
            if (username.equals(user.get("username"))) {
                return ((Number) user.get("id")).longValue();
            }
        }
        throw new AssertionError("User not found: " + username);
    }
}
