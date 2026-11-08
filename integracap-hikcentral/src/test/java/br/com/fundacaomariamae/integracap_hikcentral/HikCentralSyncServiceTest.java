
package br.com.fundacaomariamae.integracap_hikcentral;

import br.com.fundacaomariamae.integracap_hikcentral.config.HikCentralProperties;
import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoResponse;
import br.com.fundacaomariamae.integracap_hikcentral.service.HikCentralOpenApiClient;
import br.com.fundacaomariamae.integracap_hikcentral.service.HikCentralSyncService;
import br.com.fundacaomariamae.integracap_hikcentral.service.PreparacaoCadastroService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

class HikCentralSyncServiceTest {

    private final HikCentralProperties configuracao =
            new HikCentralProperties();

    private final PreparacaoCadastroService preparacao =
            mock(PreparacaoCadastroService.class);

    private final HikCentralOpenApiClient openApi =
            mock(HikCentralOpenApiClient.class);

    private final HikCentralSyncService service =
            new HikCentralSyncService(configuracao, preparacao, openApi);

    private AssistidoResponse criarAssistido() {
        return new AssistidoResponse(
                1L,
                "TESTE-001",
                "Pessoa Teste",
                null, null, null, null, null,
                null, null, null, null, null,
                null, null
        );
    }

    @Test
    void deveInformarIntegracaoDesabilitada() {
        configuracao.setEnabled(false);

        AssistidoResponse resposta =
                service.sincronizarNovoCadastro(criarAssistido(), null);

        assertEquals("DESABILITADA", resposta.hikCentralSyncStatus());
        verifyNoInteractions(preparacao, openApi);
    }

    @Test
    void deveSolicitarFotoQuandoNaoInformada() {
        configuracao.setEnabled(true);

        AssistidoResponse resposta =
                service.sincronizarNovoCadastro(criarAssistido(), null);

        assertEquals("PENDENTE_FOTO", resposta.hikCentralSyncStatus());
        verifyNoInteractions(preparacao, openApi);
    }

    @Test
    void deveInformarFaltaDeConfiguracaoDaOrganizacao() {
        configuracao.setEnabled(true);

        AssistidoResponse resposta =
                service.sincronizarNovoCadastro(
                        criarAssistido(), "Zm90bw=="
                );

        assertEquals("FALHA_CONFIGURACAO",
                resposta.hikCentralSyncStatus());

        verifyNoInteractions(preparacao, openApi);
    }

    @Test
    void deveInformarIntegracaoDesabilitadaNaEdicao() {
        configuracao.setEnabled(false);

        AssistidoResponse resposta =
                service.sincronizarEdicao(criarAssistido());

        assertEquals("DESABILITADA", resposta.hikCentralSyncStatus());
        verifyNoInteractions(preparacao, openApi);
    }

    @Test
    void deveRetornarFalhaQuandoFotoForInvalida() {
        configuracao.setEnabled(true);
        configuracao.setOrgIndexCode("ORG-TESTE");

        AssistidoResponse resposta =
                service.sincronizarNovoCadastro(
                        criarAssistido(), "FOTO-INVALIDA%%%");

        assertEquals("FALHA", resposta.hikCentralSyncStatus());
        verifyNoInteractions(preparacao, openApi);
    }

    @Test
    void deveInformarFaltaDeOrganizacaoNaEdicao() {
        configuracao.setEnabled(true);

        AssistidoResponse resposta =
                service.sincronizarEdicao(criarAssistido());

        assertEquals(
                "FALHA_CONFIGURACAO",
                resposta.hikCentralSyncStatus()
        );

        verifyNoInteractions(preparacao, openApi);
    }

    @Test
    void deveTratarFalhaDaApiDuranteEdicao() {
        configuracao.setEnabled(true);
        configuracao.setOrgIndexCode("ORG-TESTE");

        doThrow(new IllegalStateException("Falha simulada na API"))
                .when(openApi)
                .atualizarPessoaExistente(
                        eq("TESTE-001"),
                        anyMap()
                );

        AssistidoResponse resposta =
                service.sincronizarEdicao(criarAssistido());

        assertEquals("FALHA", resposta.hikCentralSyncStatus());
        verifyNoInteractions(preparacao);
    }

}
