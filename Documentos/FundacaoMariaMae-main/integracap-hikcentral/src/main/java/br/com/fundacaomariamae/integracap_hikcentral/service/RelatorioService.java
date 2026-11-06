package br.com.fundacaomariamae.integracap_hikcentral.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;
import br.com.fundacaomariamae.integracap_hikcentral.dto.ResumoDiarioDTO;

@Service
public class RelatorioService {

    public ResumoDiarioDTO gerarResumoDiario(
            LocalDate data,
            List<PassagemDTO> passagens) {

        int total = passagens.size();

        int autorizadas = 0;
        int recusadas = 0;
        int inconclusivas = 0;

        for (PassagemDTO passagem : passagens) {

            if ("AUTORIZADO".equalsIgnoreCase(passagem.getResultado())) {
                autorizadas++;

            } else if ("RECUSADO".equalsIgnoreCase(passagem.getResultado())) {
                recusadas++;

            } else {
                inconclusivas++;
            }
        }

        return new ResumoDiarioDTO(
                data,
                total,
                autorizadas,
                recusadas,
                inconclusivas
        );
    }
}