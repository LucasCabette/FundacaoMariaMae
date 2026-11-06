package br.com.fundacaomariamae.integracap_hikcentral.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fundacaomariamae.integracap_hikcentral.dto.PassagemDTO;
import br.com.fundacaomariamae.integracap_hikcentral.service.PassagemService;

@RestController
@RequestMapping("/api/passagens")
public class PassagemController {

    private final PassagemService passagemService;

    public PassagemController(PassagemService passagemService) {
        this.passagemService = passagemService;
    }

    @PostMapping
    public ResponseEntity<PassagemDTO> receberPassagem(
            @RequestBody PassagemDTO passagem) {

        PassagemDTO passagemProcessada =
                passagemService.processarPassagem(passagem);

        return ResponseEntity.ok(passagemProcessada);
    }
}