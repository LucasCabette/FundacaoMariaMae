// A interface chama a preparacao existente; nao cria cadastros nem envia ao HikCentral.
let preparacaoAssistidoId = null;
let preparacaoOcupada = false;
let preparacaoVersao = 0;
let preparacaoPreviewUrl = null;
let preparacaoBotaoOrigem = null;

function mensagemPreparacao(texto, erro = false) {
    const elemento = document.getElementById("preparacao-resultado");
    elemento.textContent = texto;
    elemento.classList.toggle("resultado-erro", erro);
}

function limparPreviaPreparacao() {
    if (preparacaoPreviewUrl) URL.revokeObjectURL(preparacaoPreviewUrl);
    preparacaoPreviewUrl = null;
    const previa = document.getElementById("preparacao-previa");
    previa.removeAttribute("src");
    previa.hidden = true;
}

window.abrirPreparacaoFoto = async function(id) {
    if (preparacaoOcupada) return;
    if (window.location.protocol === "file:") {
        alert("Para preparar fotos, abra http://localhost:8081/interface/pessoas.html com a aplicação em execução.");
        return;
    }
    const versao = ++preparacaoVersao;
    preparacaoAssistidoId = null;
    preparacaoBotaoOrigem = document.activeElement;
    document.getElementById("formPrepararFoto").reset();
    limparPreviaPreparacao();
    mensagemPreparacao("");
    document.getElementById("preparacao-assistido").textContent = "Carregando cadastro...";
    document.getElementById("preparacao-enviar").disabled = true;
    document.getElementById("preparacao-historico").replaceChildren();
    document.getElementById("modalPrepararFoto").style.display = "flex";
    document.getElementById("preparacao-fechar").focus();
    try {
        const response = await consultarApi(`${API_URL}/${id}`);
        const assistido = await response.json();
        if (versao !== preparacaoVersao) return;
        preparacaoAssistidoId = assistido.id;
        document.getElementById("preparacao-assistido").textContent = `${assistido.nome} · Código ${assistido.codigoAssistido}`;
        const ativo = assistido.status === "ATIVO";
        document.getElementById("preparacao-enviar").disabled = !ativo;
        if (!ativo) mensagemPreparacao("Este cadastro está inativo. A preparação não está disponível.", true);
        else document.getElementById("preparacao-organizacao").focus();
        await atualizarHistoricoPreparacao();
    } catch (erro) {
        if (versao === preparacaoVersao) mensagemPreparacao(`Não foi possível carregar o cadastro. ${erro.message}`, true);
    }
};

window.fecharPreparacaoFoto = function() {
    if (preparacaoOcupada) return;
    ++preparacaoVersao;
    preparacaoAssistidoId = null;
    limparPreviaPreparacao();
    document.getElementById("modalPrepararFoto").style.display = "none";
    preparacaoBotaoOrigem?.focus();
};

window.atualizarHistoricoPreparacao = async function() {
    if (!preparacaoAssistidoId) return;
    const id = preparacaoAssistidoId;
    const versao = preparacaoVersao;
    const lista = document.getElementById("preparacao-historico");
    const estados = { EM_PREPARACAO: "Em preparação", PREPARADO_LOCALMENTE: "Preparado localmente", FALHA_PREPARACAO: "Falha na preparação" };
    try {
        const response = await consultarApi(`${API_BASE}/sincronizacao/assistidos/${id}/preparacoes?limite=5`);
        const tentativas = await response.json();
        if (versao !== preparacaoVersao) return;
        lista.replaceChildren();
        if (!tentativas.length) {
            const item = document.createElement("li");
            item.textContent = "Nenhuma tentativa registrada para este cadastro.";
            lista.appendChild(item);
        }
        for (const tentativa of tentativas) {
            const item = document.createElement("li");
            const finalidade = tentativa.finalidade === "ATUALIZACAO_FOTO" ? "Nova foto" : "Cadastro";
            const horario = new Date(tentativa.criadoEm).toLocaleString("pt-BR");
            item.textContent = `${horario} · ${finalidade} · ${estados[tentativa.estado] || tentativa.estado}\nID: ${tentativa.id}${tentativa.mensagem ? "\n" + tentativa.mensagem : ""}`;
            lista.appendChild(item);
        }
    } catch (erro) {
        if (versao !== preparacaoVersao) return;
        lista.replaceChildren();
        const item = document.createElement("li");
        item.textContent = `Não foi possível consultar o histórico. ${erro.message}`;
        lista.appendChild(item);
    }
};

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("formPrepararFoto");
    if (!form) return;
    document.getElementById("preparacao-arquivo").addEventListener("change", () => {
        limparPreviaPreparacao();
        const foto = document.getElementById("preparacao-arquivo").files[0];
        if (foto && ["image/jpeg", "image/png"].includes(foto.type) && foto.size <= 1024 * 1024) {
            preparacaoPreviewUrl = URL.createObjectURL(foto);
            const previa = document.getElementById("preparacao-previa");
            previa.src = preparacaoPreviewUrl;
            previa.hidden = false;
        }
    });
    document.addEventListener("keydown", e => {
        if (e.key === "Escape" && document.getElementById("modalPrepararFoto").style.display === "flex") fecharPreparacaoFoto();
    });
    form.addEventListener("submit", async e => {
        e.preventDefault();
        if (preparacaoOcupada || !preparacaoAssistidoId) return;
        if (!form.reportValidity()) return;
        const foto = document.getElementById("preparacao-arquivo").files[0];
        const organizacao = document.getElementById("preparacao-organizacao").value.trim();
        if (!organizacao || !foto) return mensagemPreparacao("Informe a organização e selecione uma foto.", true);
        if (foto.size > 1024 * 1024) return mensagemPreparacao("A foto excede o limite de 1 MB. Selecione um arquivo menor.", true);
        const dados = new FormData();
        dados.append("foto", foto);
        dados.append("codigoOrganizacao", organizacao);
        dados.append("finalidade", document.getElementById("preparacao-finalidade").value);
        preparacaoOcupada = true;
        const enviar = document.getElementById("preparacao-enviar");
        enviar.disabled = true;
        document.getElementById("preparacao-fechar").disabled = true;
        mensagemPreparacao("Preparando a foto. Aguarde...");
        try {
            const response = await fetch(`${API_BASE}/sincronizacao/assistidos/${preparacaoAssistidoId}/preparar`, { method: "POST", body: dados });
            let resultado;
            try { resultado = await response.json(); } catch (_) { resultado = {}; }
            if (!response.ok) {
                const motivo = resultado.detail || resultado.message || `A preparação falhou (HTTP ${response.status}).`;
                mensagemPreparacao(`${motivo}${resultado.preparacaoId ? "\nID da tentativa: " + resultado.preparacaoId : ""}`, true);
            } else if (resultado.estado === "PREPARADO_LOCALMENTE" && resultado.preparacaoId) {
                mensagemPreparacao(`Foto preparada localmente.\nID da tentativa: ${resultado.preparacaoId}\nO envio ao leitor facial ainda não foi realizado.`);
            } else {
                mensagemPreparacao("A resposta não confirmou a preparação. Consulte o histórico antes de tentar novamente.", true);
            }
        } catch (_) {
            mensagemPreparacao("A conexão foi interrompida. Consulte o histórico antes de repetir: a preparação pode ter sido registrada.", true);
        } finally {
            preparacaoOcupada = false;
            enviar.disabled = false;
            document.getElementById("preparacao-fechar").disabled = false;
            await atualizarHistoricoPreparacao();
        }
    });
});
