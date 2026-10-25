
package br.com.fundacaomariamae.integracap_hikcentral;

import br.com.fundacaomariamae.integracap_hikcentral.dto.DashboardResumoDTO;
import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;
import br.com.fundacaomariamae.integracap_hikcentral.service.DashboardService;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardServiceTest {

    private final DashboardService service = new DashboardService();

    private PassagemDTO criarPassagem(Long id, String resultado) {
        return new PassagemDTO(
                id,
                "202600000" + id,
                "Pessoa Teste " + id,
                LocalDateTime.of(2026, 10, 27, 10, 0),
                "Terminal 1",
                resultado
        );
    }

    @Test
    void deveCalcularIndicadoresCorretamente() {

        List<PassagemDTO> passagens = List.of(
                criarPassagem(1L, "AUTORIZADO"),
                criarPassagem(2L, "RECUSADO"),
                criarPassagem(3L, "AUTORIZADO")
        );

        DashboardResumoDTO resumo =
                service.gerarResumo(passagens, 150, 5);

        assertEquals(3, resumo.getPassagensHoje());
        assertEquals(2, resumo.getPassagensAutorizadas());
        assertEquals(1, resumo.getPassagensRecusadas());
        assertEquals(0, resumo.getErros());
    }

    @Test
    void deveRetornarZeroSemPassagens() {

        DashboardResumoDTO resumo =
                service.gerarResumo(List.of(), 150, 0);

        assertEquals(0, resumo.getPassagensHoje());
        assertEquals(0, resumo.getPassagensAutorizadas());
        assertEquals(0, resumo.getPassagensRecusadas());
        assertEquals(0, resumo.getErros());
    }

    @Test
    void deveContabilizarErros() {

        List<PassagemDTO> passagens = List.of(
                criarPassagem(1L, "ERRO"),
                criarPassagem(2L, "AUTORIZADO")
        );

        DashboardResumoDTO resumo =
                service.gerarResumo(passagens, 150, 0);

        assertEquals(2, resumo.getPassagensHoje());
        assertEquals(1, resumo.getPassagensAutorizadas());
        assertEquals(1, resumo.getErros());
    }

    @Test
    void deveContabilizarTodasRecusadas() {

        List<PassagemDTO> passagens = List.of(
                criarPassagem(1L, "RECUSADO"),
                criarPassagem(2L, "RECUSADO")
        );

        DashboardResumoDTO resumo =
                service.gerarResumo(passagens, 150, 0);

        assertEquals(2, resumo.getPassagensRecusadas());
        assertEquals(0, resumo.getPassagensAutorizadas());
    }
}
