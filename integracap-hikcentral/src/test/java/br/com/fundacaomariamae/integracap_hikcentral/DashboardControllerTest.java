
package br.com.fundacaomariamae.integracap_hikcentral;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class DashboardControllerTest {

    @LocalServerPort
    private int porta;

    @Test
    void deveRetornarResumoDoDashboard() {

        RestClient cliente = RestClient.create(
                "http://localhost:" + porta
        );

        String json = cliente.get()
                .uri("/api/dashboard/resumo-demo")
                .retrieve()
                .body(String.class);

        assertNotNull(json);

        assertTrue(json.contains("\"passagensHoje\":3"));
        assertTrue(json.contains("\"passagensAutorizadas\":2"));
        assertTrue(json.contains("\"passagensRecusadas\":1"));
    }
}
