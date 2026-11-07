package br.com.fundacaomariamae.integracap_hikcentral.controller;

import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoRequest;
import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoResponse;
import br.com.fundacaomariamae.integracap_hikcentral.service.AssistidoService;
import br.com.fundacaomariamae.integracap_hikcentral.service.HikCentralSyncService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // <-- Libera a conexão com o FrontEnd no navegador
@RestController
@RequestMapping("/api/assistidos")
public class AssistidoController {
    private final AssistidoService service;
    private final HikCentralSyncService hikCentralSync;

    public AssistidoController(AssistidoService service, HikCentralSyncService hikCentralSync) {
        this.service = service;
        this.hikCentralSync = hikCentralSync;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssistidoResponse cadastrar(@Valid @RequestBody AssistidoRequest request) {
        AssistidoResponse salvo = service.cadastrar(request);
        return hikCentralSync.sincronizarNovoCadastro(salvo, request.fotoBase64());
    }

    @GetMapping
    public List<AssistidoResponse> listar(@RequestParam(required = false) String nome) {
        return service.listar(nome);
    }

    @GetMapping("/{id}")
    public AssistidoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public AssistidoResponse atualizar(@PathVariable Long id,
                                        @Valid @RequestBody AssistidoRequest request) {
        AssistidoResponse salvo = service.atualizar(id, request);
        return hikCentralSync.sincronizarEdicao(salvo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }
}
