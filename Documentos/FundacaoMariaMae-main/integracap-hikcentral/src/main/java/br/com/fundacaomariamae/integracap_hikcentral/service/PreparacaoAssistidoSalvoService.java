package br.com.fundacaomariamae.integracap_hikcentral.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoSalvoPreparado;
import br.com.fundacaomariamae.integracap_hikcentral.dto.CadastroEntrada;
import br.com.fundacaomariamae.integracap_hikcentral.dto.FinalidadePreparacao;

@Service
public class PreparacaoAssistidoSalvoService {
    private final ConsultaCadastroIntegracaoService assistidos;
    private final PreparacaoCadastroService preparacao;
    private final HistoricoPreparacaoService historico;

    public PreparacaoAssistidoSalvoService(ConsultaCadastroIntegracaoService assistidos,
                                          PreparacaoCadastroService preparacao,
                                          HistoricoPreparacaoService historico) {
        this.assistidos = assistidos;
        this.preparacao = preparacao;
        this.historico = historico;
    }

    public AssistidoSalvoPreparado preparar(Long assistidoId, String codigoOrganizacao, byte[] foto) {
        return preparar(assistidoId, codigoOrganizacao, foto, FinalidadePreparacao.CADASTRO);
    }

    public AssistidoSalvoPreparado preparar(Long assistidoId, String codigoOrganizacao, byte[] foto,
                                            FinalidadePreparacao finalidade) {
        if (assistidoId == null || assistidoId <= 0) {
            throw new IllegalArgumentException("Identificador do assistido deve ser positivo");
        }
        if (finalidade == null) throw new IllegalArgumentException("Finalidade da preparacao obrigatoria");
        String tentativaId = historico.iniciar(assistidoId, finalidade);
        try {
            return executar(assistidoId, codigoOrganizacao, foto, finalidade, tentativaId);
        } catch (ResponseStatusException erro) {
            int status = erro.getStatusCode().value();
            String motivo = erro.getReason() == null ? "Cadastro nao pode ser preparado" : erro.getReason();
            registrarFalha(tentativaId, status, "CADASTRO_INDISPONIVEL", motivo, erro);
            throw new PreparacaoFalhouException(tentativaId, status, motivo, erro);
        } catch (IllegalArgumentException erro) {
            registrarFalha(tentativaId, 400, "ENTRADA_INVALIDA", erro.getMessage(), erro);
            throw new PreparacaoFalhouException(tentativaId, 400, erro.getMessage(), erro);
        } catch (RuntimeException erro) {
            String motivo = "Falha interna na preparacao; consulte o responsavel pelo sistema";
            registrarFalha(tentativaId, 500, "FALHA_INTERNA", motivo, erro);
            throw new PreparacaoFalhouException(tentativaId, 500, motivo, erro);
        }
    }

    private void registrarFalha(String id, int status, String codigo, String mensagem, RuntimeException original) {
        try {
            historico.falhar(id, status, codigo, mensagem);
        } catch (RuntimeException erroRegistro) {
            original.addSuppressed(erroRegistro);
            // Nao informar que a falha foi registrada se a gravacao tambem falhou.
            throw new IllegalStateException("Nao foi possivel registrar o resultado da preparacao", original);
        }
    }

    private AssistidoSalvoPreparado executar(Long assistidoId, String codigoOrganizacao, byte[] foto,
                                              FinalidadePreparacao finalidade, String tentativaId) {
        var assistido = assistidos.buscarPorId(assistidoId);
        // Regra local da integracao: nao preparar credenciais para cadastro inativo.
        if (!"ATIVO".equals(assistido.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Preparacao indisponivel: o cadastro precisa estar ativo");
        }
        if (assistido.codigoAssistido() == null || assistido.codigoAssistido().isBlank()
                || assistido.nome() == null || assistido.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Cadastro salvo incompleto: confira codigo e nome");
        }
        var preparado = preparacao.preparar(new CadastroEntrada(
                assistido.codigoAssistido(), assistido.nome(), codigoOrganizacao), foto);
        historico.concluir(tentativaId, preparado.foto().sha256());
        return new AssistidoSalvoPreparado(
                preparado.estado(), preparado.contrato(), assistido.id(), preparado.codigoOrigem(),
                preparado.nome(), assistido.dataNascimento(), assistido.nomeMae(), assistido.cpf(),
                preparado.codigoOrganizacao(), preparado.foto(), finalidade, tentativaId);
    }
}
