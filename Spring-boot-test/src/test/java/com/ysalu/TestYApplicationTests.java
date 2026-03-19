package com.ysalu;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
/**
 * 核心接口回归测试，确保概览接口和认证流程可用。
 */
class TestYApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    /**
     * 校验项目概览接口返回了新的模块名称。
     */
    void overviewEndpointReturnsModuleSummary() throws Exception {
        mockMvc.perform(get("/api/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationName").value("TestY"))
                .andExpect(jsonPath("$.message").value("Spring Boot multi-module application is running."))
                .andExpect(jsonPath("$.modules[0]").value("testy-repository"))
                .andExpect(jsonPath("$.modules[1]").value("testy-service"))
                .andExpect(jsonPath("$.modules[2]").value("Spring-boot-test"));
    }

    @Test
    /**
     * 校验本地前端开发服务仍然可以跨域访问后端接口。
     */
    void overviewEndpointAllowsFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/overview")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    @Test
    /**
     * 校验注册、登录和重置密码整条流程可以正常工作。
     */
    void authEndpointsSupportRegisterLoginAndResetPassword() throws Exception {
        Map<String, String> registerPayload = new HashMap<String, String>();
        registerPayload.put("username", "tester01");
        registerPayload.put("email", "tester01@example.com");
        registerPayload.put("password", "secret01");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("tester01"));

        Map<String, String> loginPayload = new HashMap<String, String>();
        loginPayload.put("usernameOrEmail", "tester01");
        loginPayload.put("password", "secret01");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value("tester01@example.com"));

        Map<String, String> resetPayload = new HashMap<String, String>();
        resetPayload.put("username", "tester01");
        resetPayload.put("email", "tester01@example.com");
        resetPayload.put("newPassword", "secret02");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful."));

        loginPayload.put("password", "secret02");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("tester01"));
    }
}
