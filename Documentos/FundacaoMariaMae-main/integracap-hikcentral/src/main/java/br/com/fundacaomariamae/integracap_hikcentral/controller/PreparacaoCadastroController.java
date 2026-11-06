package br.com.fundacaomariamae.integracap_hikcentral.controller;

import java.io.IOException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import br.com.fundacaomariamae.integracap_hikcentral.dto.CadastroEntrada;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoCadastroService;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoCadastroService.Preparado;
import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoSalvoPreparado;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoAssistidoSalvoService;
import br.com.fundacaomariamae.integracap_hikcentral.dto.FinalidadePreparacao;
import br.com.fundacaomariamae.integracap_hikcentral.dto.ResultadoPreparacao;
import br.com.fundacaomariamae.integracap_hikcentral.service.HistoricoPreparacaoService;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoFalhouException;

@RestController
@RequestMapping("/api/sincronizacao")
public class PreparacaoCadastroController {
    private final PreparacaoCadastroService service;
    private final PreparacaoAssistidoSalvoService assistidoSalvoService;
    private final HistoricoPreparacaoService historico;

    public PreparacaoCadastroController(PreparacaoCadastroService service,
                                        PreparacaoAssistidoSalvoService assistidoSalvoService,
                                        HistoricoPreparacaoService historico) {
        this.service = service;
        this.assistidoSalvoService = assistidoSalvoService;
        this.historico = historico;
    }

    @PostMapping(value = "/preparar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Preparado preparar(@Valid @RequestPart("cadastro") CadastroEntrada cadastro,
                              @RequestPart("foto") MultipartFile foto) throws IOException {
        return service.preparar(cadastro, foto.getBytes());
    }

    @PostMapping(value = "/assistidos/{assistidoId}/preparar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssistidoSalvoPreparado prepararAssistidoSalvo(
            @PathVariable Long assistidoId,
            @RequestParam String codigoOrganizacao,
            @RequestParam(defaultValue = "CADASTRO") FinalidadePreparacao finalidade,
            @RequestPart("foto") MultipartFile foto) throws IOException {
        return assistidoSalvoService.preparar(assistidoId, codigoOrganizacao, foto.getBytes(), finalidade);
    }

    @GetMapping("/preparacoes/{preparacaoId}")
    public ResultadoPreparacao consultarPreparacao(@PathVariable String preparacaoId) {
        return historico.buscar(preparacaoId);
    }

    @GetMapping("/assistidos/{assistidoId}/preparacoes")
    public java.util.List<ResultadoPreparacao> listarPreparacoes(
            @PathVariable Long assistidoId, @RequestParam(defaultValue = "50") int limite) {
        return historico.listar(assistidoId, limite);
    }

    @ExceptionHandler(PreparacaoFalhouException.class)
    public ResponseEntity<ProblemDetail> preparacaoFalhou(PreparacaoFalhouException erro) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(erro.getStatus()), erro.getMessage());
        problema.setProperty("preparacaoId", erro.getPreparacaoId());
        return ResponseEntity.status(erro.getStatus()).body(problema);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail entradaInvalida(IllegalArgumentException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, erro.getMessage());
    }
}
