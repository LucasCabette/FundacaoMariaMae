package br.com.fundacaomariamae.integracap_hikcentral.dto;

import java.time.LocalDate;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoCadastroService.Foto;

/** Resultado interno; ainda nao representa o contrato oficial do HikCentral. */
public record AssistidoSalvoPreparado(
        String estado, String contrato, Long assistidoId, String codigoOrigem,
        String nome, LocalDate dataNascimento, String nomeMae, String cpf,
        String codigoOrganizacao, Foto foto, FinalidadePreparacao finalidade, String preparacaoId) {}
