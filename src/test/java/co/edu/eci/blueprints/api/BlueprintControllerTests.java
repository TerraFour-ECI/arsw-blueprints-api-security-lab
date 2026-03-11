package co.edu.eci.blueprints.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlueprintControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // ─── Helper post-processor factories ────────────────────────────────────

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor readerJwt() {
        return jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_blueprints.read"));
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor writerJwt() {
        return jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_blueprints.read"),
                new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_blueprints.write"));
    }

    // ─── GET /api/blueprints ─────────────────────────────────────────────────

    @Test
    void getAll_withReadScope_returns200() throws Exception {
        mockMvc.perform(get("/api/blueprints").with(readerJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAll_withNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/blueprints"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/blueprints/{author} ────────────────────────────────────────

    @Test
    void byAuthor_existingAuthor_returns200() throws Exception {
        mockMvc.perform(get("/api/blueprints/john").with(readerJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void byAuthor_nonExistingAuthor_returns404() throws Exception {
        mockMvc.perform(get("/api/blueprints/nobody").with(readerJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ─── GET /api/blueprints/{author}/{bpname} ───────────────────────────────

    @Test
    void byAuthorAndName_existingBlueprint_returns200() throws Exception {
        mockMvc.perform(get("/api/blueprints/john/house").with(readerJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.author").value("john"))
                .andExpect(jsonPath("$.data.name").value("house"));
    }

    @Test
    void byAuthorAndName_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/blueprints/john/missing").with(readerJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ─── POST /api/blueprints ────────────────────────────────────────────────

    @Test
    void create_withWriteScope_returns201() throws Exception {
        String body = """
                {"author":"testuser","name":"testblueprint","points":[{"x":1,"y":1}]}
                """;
        mockMvc.perform(post("/api/blueprints")
                .with(writerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("blueprint created"));
    }

    @Test
    void create_withReadScopeOnly_returns403() throws Exception {
        String body = """
                {"author":"testuser2","name":"testblueprint2","points":[]}
                """;
        mockMvc.perform(post("/api/blueprints")
                .with(readerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_duplicateBlueprint_returns409() throws Exception {
        String body = """
                {"author":"john","name":"house","points":[]}
                """;
        mockMvc.perform(post("/api/blueprints")
                .with(writerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void create_missingAuthor_returns400() throws Exception {
        String body = """
                {"name":"test","points":[]}
                """;
        mockMvc.perform(post("/api/blueprints")
                .with(writerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /api/blueprints/{author}/{bpname}/points ────────────────────────

    @Test
    void addPoint_existingBlueprint_returns202() throws Exception {
        mockMvc.perform(put("/api/blueprints/john/garage/points")
                .with(writerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"x":99,"y":99}
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202));
    }

    @Test
    void addPoint_notFoundBlueprint_returns404() throws Exception {
        mockMvc.perform(put("/api/blueprints/nobody/missing/points")
                .with(writerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"x":1,"y":1}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void addPoint_withNoToken_returns401() throws Exception {
        mockMvc.perform(put("/api/blueprints/john/house/points")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"x":1,"y":1}
                        """))
                .andExpect(status().isUnauthorized());
    }

    // ─── Full auth flow integration test ─────────────────────────────────────

    @Test
    void fullAuthFlow_loginThenGetBlueprints() throws Exception {
        // 1. Login to get a real JWT
        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"student","password":"student123"}
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from JSON response
        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(loginResponse)
                .get("access_token")
                .asText();

        // 2. Use that token to access protected endpoint
        mockMvc.perform(get("/api/blueprints")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
