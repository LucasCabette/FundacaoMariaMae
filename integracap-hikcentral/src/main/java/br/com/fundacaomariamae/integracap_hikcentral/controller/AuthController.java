
package br.com.fundacaomariamae.integracap_hikcentral.controller;

import br.com.fundacaomariamae.integracap_hikcentral.dto.LoginRequest;
import br.com.fundacaomariamae.integracap_hikcentral.dto.LoginResponse;
import br.com.fundacaomariamae.integracap_hikcentral.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(
        @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}
