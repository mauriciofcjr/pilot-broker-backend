package com.pilotbroker.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PilotBrokerBackendApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe sem erros no perfil de teste
    }
}
