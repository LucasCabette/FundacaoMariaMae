package br.com.fundacaomariamae.integracap_hikcentral.dto;

import br.com.fundacaomariamae.integracap_hikcentral.model.Assistido;
import br.com.fundacaomariamae.integracap_hikcentral.model.Sexo;
import br.com.fundacaomariamae.integracap_hikcentral.model.StatusAssistido;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssistidoResponse(
    Long id,
    String codigoAssistido,
    String nome,
    LocalDate dataNascimento,
    String nomeMae,
    String nomePai,
    String cpf,
    String rg,
    String cidadeNascimento,
    Sexo sexo,
    StatusAssistido status,
    LocalDateTime dataCadastro,
    LocalDateTime dataAtualizacao
) {
    public static AssistidoResponse from(Assistido a) {
        return new AssistidoResponse(
            a.getId(), a.getCodigoAssistido(), a.getNome(),
            a.getDataNascimento(), a.getNomeMae(), a.getNomePai(),
            a.getCpf(), a.getRg(), a.getCidadeNascimento(),
            a.getSexo(), a.getStatus(), a.getDataCadastro(), a.getDataAtualizacao()
        );
    }
}
