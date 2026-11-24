package com.teamflow.team;

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
class TeamApiIntegrationTest {
    private static final String ORGANIZATION_A = "11111111-1111-1111-1111-111111111111";
    private static final String ORGANIZATION_B = "22222222-2222-2222-2222-222222222222";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("team_db")
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
    void createsListsReadsAndUpdatesTeamWithinOrganization() throws Exception {
        String response = mockMvc.perform(post("/teams")
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "identification": "Equipe Norte",
                                  "totalMembers": 8,
                                  "composition": [{"role": "Técnico", "quantity": 4}],
                                  "competencies": ["Instalação"],
                                  "qualifications": ["NR10"],
                                  "availability": {"note": "Cadastro agregado"},
                                  "status": "ATIVA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_A))
                .andExpect(jsonPath("$.identification").value("Equipe Norte"))
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/teams").header("X-Organization-Id", ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].identification", hasItem("Equipe Norte")));

        mockMvc.perform(get("/teams/{id}", id).header("X-Organization-Id", ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("Equipe Norte"));

        mockMvc.perform(put("/teams/{id}", id)
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"identification":"Equipe Norte Atualizada","totalMembers":9,"status":"INATIVA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("Equipe Norte Atualizada"))
                .andExpect(jsonPath("$.totalMembers").value(9));
    }

    @Test
    void doesNotExposeTeamsFromAnotherOrganization() throws Exception {
        mockMvc.perform(post("/teams")
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("{\"identification\":\"Equipe A\",\"totalMembers\":2}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/teams")
                        .header("X-Organization-Id", ORGANIZATION_B)
                        .contentType(APPLICATION_JSON)
                        .content("{\"identification\":\"Equipe Sul\",\"totalMembers\":3}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/teams").header("X-Organization-Id", ORGANIZATION_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].identification", hasItem("Equipe A")));

        mockMvc.perform(get("/teams").header("X-Organization-Id", ORGANIZATION_B))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].identification").value("Equipe Sul"));
    }

    @Test
    void rejectsInvalidRequiredFields() throws Exception {
        mockMvc.perform(post("/teams")
                        .header("X-Organization-Id", ORGANIZATION_A)
                        .contentType(APPLICATION_JSON)
                        .content("{\"identification\":\"\",\"totalMembers\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.identification").exists())
                .andExpect(jsonPath("$.fields.totalMembers").exists());
    }
}
