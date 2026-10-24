
package br.com.fundacaomariamae.integracap_hikcentral.controller;

import br.com.fundacaomariamae.integracap_hikcentral.dto.DashboardResumoDTO;
import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;
import br.com.fundacaomariamae.integracap_hikcentral.service.DashboardService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo-demo")
    public DashboardResumoDTO consultarResumoDemo() {

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

        return dashboardService.gerarResumo(
                passagens,
                150,
                5
        );
    }
}
