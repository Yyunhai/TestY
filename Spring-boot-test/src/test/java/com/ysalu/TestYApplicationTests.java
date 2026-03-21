package com.ysalu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysalu.web.auth.SessionUser;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
    void authEndpointsSupportMultiTableRegistrationSessionLoginAndAuditing() throws Exception {
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
                .andExpect(jsonPath("$.displayName").value("Tester Two"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.permissions[0]").value("overview:read"));

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
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.permissions[0]").value("overview:read"))
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
                .andExpect(jsonPath("$.username").value("tester02"))
                .andExpect(jsonPath("$.displayName").value("Tester Two"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        mockMvc.perform(get("/api/overview").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationName").value("TestY"));

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied: admin:users:read"));

        Map<String, String> createDocumentPayload = new HashMap<String, String>();
        createDocumentPayload.put("title", "第一篇文档");
        createDocumentPayload.put("content", "# 标题\n\n这里是第一版内容。");

        MvcResult documentCreateResult = mockMvc.perform(post("/api/docs").session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDocumentPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("第一篇文档"))
                .andExpect(jsonPath("$.content").value("# 标题\n\n这里是第一版内容。"))
                .andReturn();

        Map<?, ?> createdDocument = objectMapper.readValue(
                documentCreateResult.getResponse().getContentAsString(),
                Map.class
        );
        Number documentId = (Number) createdDocument.get("id");
        assertNotNull(documentId);

        mockMvc.perform(get("/api/docs").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("第一篇文档"))
                .andExpect(jsonPath("$[0].excerpt").exists());

        mockMvc.perform(get("/api/docs/" + documentId.longValue()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("第一篇文档"))
                .andExpect(jsonPath("$.content").value("# 标题\n\n这里是第一版内容。"));

        Map<String, String> updateDocumentPayload = new HashMap<String, String>();
        updateDocumentPayload.put("title", "第一篇文档（已更新）");
        updateDocumentPayload.put("content", "# 标题\n\n这里是更新后的内容。");

        mockMvc.perform(put("/api/docs/" + documentId.longValue()).session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDocumentPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("第一篇文档（已更新）"))
                .andExpect(jsonPath("$.content").value("# 标题\n\n这里是更新后的内容。"));

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
    void rootAdminOwnsAllPermissionsAndCanViewSecurityData() throws Exception {
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

        MockHttpSession rootSession = (MockHttpSession) rootLogin.getRequest().getSession(false);
        assertNotNull(rootSession);

        mockMvc.perform(get("/api/admin/users").session(rootSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].roles").isArray());

        mockMvc.perform(get("/api/admin/login-audits").session(rootSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].principal").exists())
                .andExpect(jsonPath("$[0].remoteIp").exists())
                .andExpect(jsonPath("$[0].permissions").isArray());
    }
}
