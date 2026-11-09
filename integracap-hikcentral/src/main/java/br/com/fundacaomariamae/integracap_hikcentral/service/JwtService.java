
package br.com.fundacaomariamae.integracap_hikcentral.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.fundacaomariamae.integracap_hikcentral.model.Usuario;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long validade;

    public JwtService(
        @Value("${app.jwt.secret}") String segredo,
        @Value("${app.jwt.expiration-ms}") long validade
    ) {
        byte[] bytes = Base64.getDecoder().decode(segredo);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                "A chave JWT deve possuir pelo menos 256 bits."
            );
        }

        this.chave = Keys.hmacShaKeyFor(bytes);
        this.validade = validade;
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + validade);

        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("usuarioId", usuario.getId())
            .claim("perfil", usuario.getPerfil().name())
            .issuedAt(agora)
            .expiration(expiracao)
            .signWith(chave)
            .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
            .verifyWith(chave)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token);

            return true;
        } catch (io.jsonwebtoken.JwtException |
                 IllegalArgumentException e) {
            return false;
        }
    }
}
