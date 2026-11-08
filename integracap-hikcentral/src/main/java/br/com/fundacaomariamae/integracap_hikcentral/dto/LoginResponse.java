
package br.com.fundacaomariamae.integracap_hikcentral.dto;

public record LoginResponse(
    String token,
    String tipo,
    Long usuarioId,
    String nome,
    String email,
    String perfil
) {}
