
package br.com.fundacaomariamae.integracap_hikcentral;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RelatorioControllerTest {

    @LocalServerPort
    private int porta;

    @Test
    void deveRetornarResumoDiario() {

        RestClient cliente = RestClient.create(
                "http://localhost:" + porta
        );

        String json = cliente.get()
                .uri("/api/relatorios/resumo-demo")
                .retrieve()
                .body(String.class);

        assertNotNull(json);

        assertTrue(json.contains("\"totalPassagens\":3"));
        assertTrue(json.contains("\"passagensAutorizadas\":2"));
        assertTrue(json.contains("\"passagensRecusadas\":1"));
    }
}
