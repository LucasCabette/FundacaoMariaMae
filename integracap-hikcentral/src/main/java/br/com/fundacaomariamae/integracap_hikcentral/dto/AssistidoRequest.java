package br.com.fundacaomariamae.integracap_hikcentral.dto;

import br.com.fundacaomariamae.integracap_hikcentral.model.Sexo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import jakarta.validation.constraints.Pattern;

public record AssistidoRequest(
    @NotBlank @Size(max = 150) String nome,
    @NotNull LocalDate dataNascimento,
    @Size(max = 150) String nomeMae,
    @Size(max = 150) String nomePai,
    @Pattern(
    regexp = "^(|\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
    message = "CPF deve conter 11 dígitos, com ou sem pontuação."
)
String cpf,
    @Size(max = 30) String rg,
    @Size(max = 100) String cidadeNascimento,
    Sexo sexo,
    @NotBlank String codigoAssistido
) {}
