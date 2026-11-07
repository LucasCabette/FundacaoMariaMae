package br.com.fundacaomariamae.integracap_hikcentral.service;

import br.com.fundacaomariamae.integracap_hikcentral.config.HikCentralProperties;
import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoResponse;
import br.com.fundacaomariamae.integracap_hikcentral.dto.CadastroEntrada;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Sincroniza o cadastro recém-salvo e a foto recebida no mesmo formulário. */
@Service
public class HikCentralSyncService {
    private final HikCentralProperties properties;
    private final PreparacaoCadastroService preparacao;
    private final HikCentralOpenApiClient openApi;

    public HikCentralSyncService(HikCentralProperties properties, PreparacaoCadastroService preparacao,
                                 HikCentralOpenApiClient openApi) {
        this.properties = properties;
        this.preparacao = preparacao;
        this.openApi = openApi;
    }

    public AssistidoResponse sincronizarNovoCadastro(AssistidoResponse assistido, String fotoRecebida) {
        if (!properties.isEnabled()) {
            return assistido.comIntegracao("DESABILITADA", "A integração será ativada após configurar o acesso local.");
        }
        if (fotoRecebida == null || fotoRecebida.isBlank()) {
            return assistido.comIntegracao("PENDENTE_FOTO", "Cadastro salvo localmente; é necessária uma foto facial para sincronizar.");
        }
        if (properties.getOrgIndexCode().isBlank()) {
            return assistido.comIntegracao("FALHA_CONFIGURACAO", "Configure a organização de destino do HikCentral.");
        }

        try {
            String base64 = fotoRecebida.contains(",") ? fotoRecebida.substring(fotoRecebida.indexOf(',') + 1) : fotoRecebida;
            byte[] bytes = Base64.getDecoder().decode(base64);
            var foto = preparacao.preparar(new CadastroEntrada(
                    assistido.codigoAssistido(), assistido.nome(), properties.getOrgIndexCode()), bytes).foto();

            Map<String, Object> pessoa = mapearPessoa(assistido, properties.getOrgIndexCode());
            String personId = openApi.cadastrarOuAtualizarPessoa(assistido.codigoAssistido(), pessoa);
            openApi.atualizarFoto(assistido.codigoAssistido(), foto.base64());
            if (properties.getPrivilegeGroupId().isBlank()) {
                return assistido.comIntegracao("PESSOA_E_FOTO_ENVIADAS",
                        "Pessoa e foto enviados. Confirme se a permissão do leitor é atribuída automaticamente no HikCentral.");
            }
            openApi.atribuirNivelAcesso(personId, properties.getPrivilegeGroupId());
            return assistido.comIntegracao("CADASTRO_E_FOTO_ENVIADOS",
                    "Cadastro, foto e grupo de acesso enviados ao HikCentral. ID remoto: " + personId
                            + ". Confirme a aplicação da permissão ao leitor.");
        } catch (RuntimeException erro) {
            return assistido.comIntegracao("FALHA", mensagemSegura(erro));
        }
    }

    public AssistidoResponse sincronizarEdicao(AssistidoResponse assistido) {
        if (!properties.isEnabled()) {
            return assistido.comIntegracao("DESABILITADA", "A integração será ativada após configurar o acesso local.");
        }
        if (properties.getOrgIndexCode().isBlank()) {
            return assistido.comIntegracao("FALHA_CONFIGURACAO", "Configure a organização de destino do HikCentral.");
        }
        try {
            openApi.atualizarPessoaExistente(assistido.codigoAssistido(),
                    mapearPessoa(assistido, properties.getOrgIndexCode()));
            return assistido.comIntegracao("DADOS_ATUALIZADOS",
                    "Dados cadastrais atualizados no HikCentral. A foto facial existente foi mantida.");
        } catch (RuntimeException erro) {
            return assistido.comIntegracao("FALHA", mensagemSegura(erro));
        }
    }

    private static Map<String, Object> mapearPessoa(AssistidoResponse a, String organizacao) {
        String[] nomes = a.nome().trim().split("\\s+", 2);
        Map<String, Object> pessoa = new LinkedHashMap<>();
        pessoa.put("personCode", a.codigoAssistido());
        pessoa.put("personGivenName", nomes.length == 2 ? nomes[0] : a.nome());
        pessoa.put("personFamilyName", nomes.length == 2 ? nomes[1] : a.nome());
        pessoa.put("orgIndexCode", organizacao);
        pessoa.put("gender", switch (a.sexo() == null ? "" : a.sexo().name()) {
            case "MASCULINO" -> 1;
            case "FEMININO" -> 2;
            default -> 0;
        });
        return pessoa;
    }

    private static String mensagemSegura(RuntimeException erro) {
        String mensagem = erro.getMessage();
        return mensagem == null || mensagem.isBlank() ? "Falha ao enviar cadastro/foto ao HikCentral." : mensagem;
    }
}
