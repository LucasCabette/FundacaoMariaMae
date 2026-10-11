package br.com.fundacaomariamae.integracap_hikcentral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroEntrada(
        @NotBlank @Size(max = 128) String codigoOrigem,
        @NotBlank @Size(max = 256) String nome,
        @NotBlank @Size(max = 128) String codigoOrganizacao) {}
