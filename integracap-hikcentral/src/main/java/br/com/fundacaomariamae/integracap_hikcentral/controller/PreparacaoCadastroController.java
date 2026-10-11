package br.com.fundacaomariamae.integracap_hikcentral.controller;

import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import br.com.fundacaomariamae.integracap_hikcentral.dto.CadastroEntrada;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoCadastroService;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoCadastroService.Preparado;

@RestController
@RequestMapping("/api/sincronizacao")
public class PreparacaoCadastroController {
    private final PreparacaoCadastroService service;
    public PreparacaoCadastroController(PreparacaoCadastroService service) { this.service = service; }

    @PostMapping(value = "/preparar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Preparado preparar(@Valid @RequestPart("cadastro") CadastroEntrada cadastro,
                              @RequestPart("foto") MultipartFile foto) throws IOException {
        return service.preparar(cadastro, foto.getBytes());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail entradaInvalida(IllegalArgumentException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, erro.getMessage());
    }
}
