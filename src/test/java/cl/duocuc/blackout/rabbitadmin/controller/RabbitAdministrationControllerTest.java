package cl.duocuc.blackout.rabbitadmin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duocuc.blackout.rabbitadmin.dto.OperationResponse;
import cl.duocuc.blackout.rabbitadmin.dto.QueueRequest;
import cl.duocuc.blackout.rabbitadmin.service.RabbitAdministrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.security.entra-issuer=https://login.microsoftonline.com/test/v2.0",
    "app.security.entra-audience=test-audience",
    "app.security.entra-scope=access_as_user",
    "spring.rabbitmq.password=test-password"
})
@AutoConfigureMockMvc
class RabbitAdministrationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RabbitAdministrationService service;

    @Test
    void rejectsAnonymousRequest() throws Exception {
        mvc.perform(post("/api/rabbit/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test.queue\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsFanRole() throws Exception {
        mvc.perform(post("/api/rabbit/queues")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Fan")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test.queue\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesValidatedQueue() throws Exception {
        when(service.createQueue(any())).thenReturn(new OperationResponse("queue", "test.queue", "created"));

        mvc.perform(post("/api/rabbit/queues")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test.queue\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("created"));

        verify(service).createQueue(any(QueueRequest.class));
    }

    @Test
    void invalidNameNeverReachesService() throws Exception {
        mvc.perform(post("/api/rabbit/queues")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"nombre con espacios\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists());

        verify(service, never()).createQueue(any());
    }
}

