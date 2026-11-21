package cl.duoc.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Semana 5: comprueba la configuracion de Spring Security (SecurityConfig).
 * jwt() simula un token ya validado, sin llamar a Azure.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTests {

    @Autowired
    MockMvc mvc;

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/eventos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenValidoResponde200() throws Exception {
        mvc.perform(get("/api/eventos").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void healthCheckNoExigeToken() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
