package com.teamflow.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProjectApiIntegrationTest {
    private static final String ORGANIZATION_A = "11111111-1111-1111-1111-111111111111";
    private static final String ORGANIZATION_B = "22222222-2222-2222-2222-222222222222";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("project_db")
            .withUsername("teamflow")
            .withPassword("teamflow_test");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void createsListsReadsAndUpdatesProjectWithinOrganization() throws Exception {
        String response = mockMvc.perform(post("/projects")
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "identification": "Projeto Norte",
                                  "client": "Cliente A",
                                  "description": "Serviço operacional",
                                  "plannedStartDate": "2026-12-01",
                                  "plannedEndDate": "2026-12-05",
                                  "estimatedDurationDays": 5,
                                  "competencies": ["Instalação"],
                                  "qualifications": ["NR10"],
                                  "localities": [{"identification": "São Paulo"}],
                                  "services": [{"identification": "Montagem", "serviceDays": 5, "estimatedDurationDays": 5}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_A))
                .andExpect(jsonPath("$.identification").value("Projeto Norte"))
                .andExpect(jsonPath("$.localities[0].identification").value("São Paulo"))
                .andExpect(jsonPath("$.services[0].serviceDays").value(5))
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/projects").header("X-Organization-Id", ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].identification", hasItem("Projeto Norte")));

        mockMvc.perform(get("/projects/{id}", id).header("X-Organization-Id", ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("Projeto Norte"));

        mockMvc.perform(put("/projects/{id}", id)
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"identification":"Projeto Norte Atualizado","estimatedDurationDays":7}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("Projeto Norte Atualizado"))
                .andExpect(jsonPath("$.estimatedDurationDays").value(7));
    }

    @Test
    void doesNotExposeProjectsFromAnotherOrganization() throws Exception {
        mockMvc.perform(post("/projects")
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("{\"identification\":\"Projeto A\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/projects")
                        .header("X-Organization-Id", ORGANIZATION_B)
                        .contentType(APPLICATION_JSON)
                        .content("{\"identification\":\"Projeto B\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects").header("X-Organization-Id", ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].identification", hasItem("Projeto A")));

        mockMvc.perform(get("/projects").header("X-Organization-Id", ORGANIZATION_B))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].identification").value("Projeto B"));
    }

    @Test
    void rejectsMissingIdentificationAndNegativeDuration() throws Exception {
        mockMvc.perform(post("/projects")
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("{\"identification\":\"\",\"estimatedDurationDays\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.identification").exists())
                .andExpect(jsonPath("$.fields.estimatedDurationDays").exists());
    }
}
