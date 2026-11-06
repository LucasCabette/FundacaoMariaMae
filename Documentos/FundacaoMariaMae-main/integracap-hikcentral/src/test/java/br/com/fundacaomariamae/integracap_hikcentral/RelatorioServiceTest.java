
package br.com.fundacaomariamae.integracap_hikcentral;

import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;
import br.com.fundacaomariamae.integracap_hikcentral.dto.ResumoDiarioDTO;
import br.com.fundacaomariamae.integracap_hikcentral.service.RelatorioService;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelatorioServiceTest {

    private final RelatorioService service = new RelatorioService();

    private PassagemDTO criarPassagem(
            Long id,
            LocalDateTime dataHora,
            String resultado) {

        return new PassagemDTO(
                id,
                "202600000" + id,
                "Pessoa Teste " + id,
                dataHora,
                "Terminal 1",
                resultado
        );
    }

    @Test
    void deveGerarResumoCorretamente() {

        LocalDate data = LocalDate.of(2026, 10, 27);

        List<PassagemDTO> passagens = List.of(
                criarPassagem(1L, data.atTime(8, 0), "AUTORIZADO"),
                criarPassagem(2L, data.atTime(9, 0), "RECUSADO"),
                criarPassagem(3L, data.atTime(10, 0), "INCONCLUSIVO")
        );

        ResumoDiarioDTO resumo =
                service.gerarResumoDiario(data, passagens);

        assertEquals(3, resumo.getTotalPassagens());
        assertEquals(1, resumo.getPassagensAutorizadas());
        assertEquals(1, resumo.getPassagensRecusadas());
        assertEquals(1, resumo.getPassagensInconclusivas());
    }

    @Test
    void deveRetornarZeroSemPassagens() {

        LocalDate data = LocalDate.of(2026, 10, 27);

        ResumoDiarioDTO resumo =
                service.gerarResumoDiario(data, List.of());

        assertEquals(0, resumo.getTotalPassagens());
        assertEquals(0, resumo.getPassagensAutorizadas());
        assertEquals(0, resumo.getPassagensRecusadas());
        assertEquals(0, resumo.getPassagensInconclusivas());
    }

    @Test
    void deveContabilizarTodasAutorizadas() {

        LocalDate data = LocalDate.of(2026, 10, 27);

        List<PassagemDTO> passagens = List.of(
                criarPassagem(1L, data.atTime(8, 0), "AUTORIZADO"),
                criarPassagem(2L, data.atTime(9, 0), "AUTORIZADO")
        );

        ResumoDiarioDTO resumo =
                service.gerarResumoDiario(data, passagens);

        assertEquals(2, resumo.getTotalPassagens());
        assertEquals(2, resumo.getPassagensAutorizadas());
        assertEquals(0, resumo.getPassagensRecusadas());
    }

    @Test
    void deveContabilizarTodasRecusadas() {

        LocalDate data = LocalDate.of(2026, 10, 27);

        List<PassagemDTO> passagens = List.of(
                criarPassagem(1L, data.atTime(8, 0), "RECUSADO"),
                criarPassagem(2L, data.atTime(9, 0), "RECUSADO")
        );

        ResumoDiarioDTO resumo =
                service.gerarResumoDiario(data, passagens);

        assertEquals(2, resumo.getPassagensRecusadas());
        assertEquals(0, resumo.getPassagensAutorizadas());
    }
}
