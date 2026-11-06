
package br.com.fundacaomariamae.integracap_hikcentral.service;

import br.com.fundacaomariamae.integracap_hikcentral.dto.DashboardResumoDTO;
import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    public DashboardResumoDTO gerarResumo(
            List<PassagemDTO> passagens,
            int totalCadastros,
            int novosCadastros) {

        int autorizadas = 0;
        int recusadas = 0;
        int erros = 0;

        for (PassagemDTO passagem : passagens) {

            String resultado = passagem.getResultado();

            if ("AUTORIZADO".equalsIgnoreCase(resultado)) {
                autorizadas++;

            } else if ("RECUSADO".equalsIgnoreCase(resultado)) {
                recusadas++;

            } else if ("ERRO".equalsIgnoreCase(resultado)) {
                erros++;
            }
        }

        return new DashboardResumoDTO(
                totalCadastros,
                passagens.size(),
                autorizadas,
                recusadas,
                novosCadastros,
                erros
        );
    }
}
