package br.com.fundacaomariamae.integracap_hikcentral.service;

import org.springframework.stereotype.Service;

import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;

@Service
public class PassagemService {

    public PassagemDTO processarPassagem(PassagemDTO passagem) {

        System.out.println("=== PASSAGEM RECEBIDA ===");
        System.out.println("Código: " + passagem.getCodigoAssistido());
        System.out.println("Nome: " + passagem.getNomeAssistido());
        System.out.println("Data/Hora: " + passagem.getDataHora());
        System.out.println("Terminal: " + passagem.getTerminal());
        System.out.println("Resultado: " + passagem.getResultado());

        return passagem;
    }
}