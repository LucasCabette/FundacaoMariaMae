package br.com.fundacaomariamae.integracap_hikcentral.dto;

/** Historico local; nao contem foto, Base64, nome, CPF ou dados do HikCentral. */
public record ResultadoPreparacao(
        String id, Long assistidoId, FinalidadePreparacao finalidade, String estado,
        Integer httpStatus, String codigoErro, String mensagem, String fotoSha256,
        String criadoEm, String atualizadoEm) {}
