package br.com.fundacaomariamae.integracap_hikcentral.dto;

import br.com.fundacaomariamae.integracap_hikcentral.model.Sexo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;

public record AssistidoRequest(
    @NotBlank @Size(max = 150) String nome,
    @NotNull
@PastOrPresent(message = "Data de nascimento não pode ser futura.")
LocalDate dataNascimento,
    @Size(max = 150) String nomeMae,
    @Size(max = 150) String nomePai,
    @Size(min = 11, max = 11) String cpf,
    @Size(max = 30) String rg,
    @Size(max = 100) String cidadeNascimento,
    Sexo sexo,
    @NotBlank String codigoAssistido,
    @Size(max = 2800000) String fotoBase64
) {}
