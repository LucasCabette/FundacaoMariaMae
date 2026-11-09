
package br.com.fundacaomariamae.integracap_hikcentral.service;

import br.com.fundacaomariamae.integracap_hikcentral.dto.LoginRequest;
import br.com.fundacaomariamae.integracap_hikcentral.dto.LoginResponse;
import br.com.fundacaomariamae.integracap_hikcentral.model.Usuario;
import br.com.fundacaomariamae.integracap_hikcentral.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository
            .findByEmailAndAtivoTrue(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."
            ));

        if (!passwordEncoder.matches(
                request.senha(),
                usuario.getSenhaHash())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."
            );
        }

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
            token,
            "Bearer",
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil().name()
        );
    }
}
