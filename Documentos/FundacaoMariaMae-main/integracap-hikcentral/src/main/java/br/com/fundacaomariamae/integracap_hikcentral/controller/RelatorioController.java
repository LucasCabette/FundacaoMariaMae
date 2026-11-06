
package br.com.fundacaomariamae.integracap_hikcentral.controller;

import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;
import br.com.fundacaomariamae.integracap_hikcentral.dto.ResumoDiarioDTO;
import br.com.fundacaomariamae.integracap_hikcentral.service.RelatorioService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/resumo-demo")
    public ResumoDiarioDTO consultarResumoDemo() {

        LocalDate data = LocalDate.of(2026, 10, 12);

        List<PassagemDTO> passagens = List.of(

                new PassagemDTO(
                        1L, "2026000001", "Pessoa Teste 1",
                        LocalDateTime.of(2026, 10, 12, 8, 0),
                        "Terminal 1", "AUTORIZADO"
                ),

                new PassagemDTO(
                        2L, "2026000002", "Pessoa Teste 2",
                        LocalDateTime.of(2026, 10, 12, 9, 0),
                        "Terminal 1", "RECUSADO"
                ),

                new PassagemDTO(
                        3L, "2026000003", "Pessoa Teste 3",
                        LocalDateTime.of(2026, 10, 12, 10, 0),
                        "Terminal 1", "AUTORIZADO"
                )
        );

        return relatorioService.gerarResumoDiario(data, passagens);
    }
}
