// URL da API REST no Spring Boot
const API_BASE = window.location.protocol !== "file:" && window.location.pathname.startsWith("/interface/")
    ? `${window.location.origin}/api` : "http://localhost:8081/api";
const API_URL = `${API_BASE}/assistidos`;

// Mantém os dados completos carregados para editar sem apagar outros campos.
let assistidoEmEdicao = null;
let consultaAtual = 0;

async function consultarApi(url, opcoes) {
    const response = await fetch(url, opcoes);
    if (!response.ok) {
        let mensagem = `A operação não foi concluída (HTTP ${response.status}).`;
        try {
            const erro = await response.json();
            mensagem = erro.detail || erro.message || mensagem;
        } catch (_) { /* Nem toda resposta de erro contém JSON. */ }
        throw new Error(mensagem);
    }
    return response;
}

// =========================
// NAVEGAÇÃO ENTRE TELAS
// =========================
document.getElementById("loginForm")?.addEventListener("submit", function(event) {
    event.preventDefault();
    window.location.href = "menu.html";
});

function consultarCadastros() { window.location.href = "pessoas.html"; }
function novoCadastro() { window.location.href = "novo-cadastro.html"; }
function voltarMenu() { window.location.href = "menu.html"; }
function escanearDocumento() { alert("Funcionalidade de escanear documento em desenvolvimento."); }
function inserirManualmente() { alert("Tela de formulário manual em desenvolvimento."); }
function sair() { window.location.href = "index.html"; }

// =========================
// BUSCA E RENDERIZAÇÃO
// =========================
async function carregarAssistidos(filtroNome = "", filtroCpf = "", filtroId = "") {
    const consulta = ++consultaAtual;
    try {
        const nome = filtroNome.trim();
        const cpf = filtroCpf.replace(/\D/g, "");
        const codigo = filtroId.trim();
        const url = nome ? `${API_URL}?nome=${encodeURIComponent(nome)}` : API_URL;
        const response = await consultarApi(url);
        const assistidos = await response.json();
        // CPF e código são filtrados na lista real, sem alterar a API existente.
        const resultado = assistidos.filter(item => {
            const bateuCpf = !cpf || String(item.cpf || "").replace(/\D/g, "").includes(cpf);
            const bateuId = !codigo || String(item.codigoAssistido || "").includes(codigo)
                || String(item.id) === codigo;
            return bateuCpf && bateuId;
        });
        if (consulta === consultaAtual) renderizarTabela(resultado);
    } catch (error) {
        if (consulta !== consultaAtual) return;
        renderizarTabela([]);
        const celula = document.querySelector(".tabela-dados tbody td");
        if (celula) celula.textContent = "Não foi possível consultar os cadastros. Verifique a conexão com o sistema.";
        console.error("Falha ao consultar cadastros:", error);
    }
}

function renderizarTabela(lista) {
    const tbody = document.querySelector(".tabela-dados tbody");
    if (!tbody) return;

    tbody.innerHTML = "";

    if (lista.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center" style="padding: 20px; color: var(--cor-texto-suave);">
                    Nenhum registro encontrado.
                </td>
            </tr>`;
        atualizarContador(0);
        return;
    }

    lista.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><div class="avatar-circle">👤</div></td>
            <td></td>
            <td></td>
            <td></td>
            <td class="text-center">
                <button class="btn-acao btn-preparar" title="Preparar foto" onclick="abrirPreparacaoFoto(${item.id})">Preparar foto</button>
                <button class="btn-acao btn-editar" title="Editar" onclick="abrirModalEditar(${item.id})">✏️</button>
                <button class="btn-acao btn-excluir" title="Inativar" onclick="inativarAssistido(${item.id})">🗑️</button>
            </td>
        `;
        tr.children[1].textContent = item.nome || "";
        tr.children[2].textContent = item.cpf || "Não informado";
        tr.children[3].textContent = item.codigoAssistido || item.id;
        tbody.appendChild(tr);
    });

    atualizarContador(lista.length);
}

function atualizarContador(total) {
    const contador = document.querySelector(".paginacao-container span");
    if (contador) {
        contador.textContent = `Mostrando ${total} registro(s)`;
    }
}

function executarFiltroAtual() {
    const nome = document.getElementById("filtro-nome")?.value || "";
    const cpf = document.getElementById("filtro-cpf")?.value || "";
    const id = document.getElementById("filtro-id")?.value || "";
    carregarAssistidos(nome, cpf, id);
}

// =========================
// AÇÃO: APAGAR / INATIVAR
// =========================
async function inativarAssistido(id) {
    if (!confirm("Deseja inativar este cadastro? O registro será mantido no banco.")) return;
    try {
        await consultarApi(`${API_URL}/${id}`, { method: "DELETE" });
    } catch (error) {
        alert(`Não foi possível inativar o cadastro. ${error.message}`);
        return;
    }
    alert("Cadastro inativado com sucesso. O registro permanece disponível para consulta.");
    executarFiltroAtual();
}

// =========================
// AÇÃO: EDITAR (MODAL)
// =========================
window.abrirModalEditar = async function(id) {
    const modal = document.getElementById("modalEditar");
    if (!modal) return;
    assistidoEmEdicao = null;
    modal.style.display = "none";
    try {
        const response = await consultarApi(`${API_URL}/${id}`);
        const assistido = await response.json();
        assistidoEmEdicao = assistido;
        document.getElementById("edit-id").value = assistido.id;
        document.getElementById("edit-nome").value = assistido.nome;
        document.getElementById("edit-cpf").value = assistido.cpf || "";
        document.getElementById("edit-codigo").value = assistido.codigoAssistido;
        modal.style.display = "flex";
    } catch (error) {
        alert(`Não foi possível carregar o cadastro para edição. ${error.message}`);
    }
};

window.fecharModalEditar = function() {
    const modal = document.getElementById("modalEditar");
    if (modal) modal.style.display = "none";
    assistidoEmEdicao = null;
};

// =========================
// INICIALIZAÇÃO E EVENTOS
// =========================
document.addEventListener("DOMContentLoaded", () => {
    const tabela = document.querySelector(".tabela-dados");

    if (tabela) {
        carregarAssistidos();

        const formFiltro = document.querySelector(".filtro-grid");
        formFiltro?.addEventListener("submit", (e) => {
            e.preventDefault();
            executarFiltroAtual();
        });
    }

    // Configuração do formulário de salvar alterações no Modal
    const formEditar = document.getElementById("formEditarAssistido");
    formEditar?.addEventListener("submit", async function(e) {
        e.preventDefault();

        if (!assistidoEmEdicao) {
            alert("Carregue o cadastro antes de salvar.");
            return;
        }
        const original = assistidoEmEdicao;
        const cpf = document.getElementById("edit-cpf").value.replace(/\D/g, "");
        // O PUT recebe todos os campos: conserva os que não existem neste modal.
        const dados = {
            nome: document.getElementById("edit-nome").value.trim(),
            cpf: cpf || null,
            codigoAssistido: document.getElementById("edit-codigo").value.trim(),
            dataNascimento: original.dataNascimento,
            nomeMae: original.nomeMae,
            nomePai: original.nomePai,
            rg: original.rg,
            cidadeNascimento: original.cidadeNascimento,
            sexo: original.sexo
        };
        const botao = formEditar.querySelector('button[type="submit"]');
        if (botao) botao.disabled = true;
        try {
            await consultarApi(`${API_URL}/${original.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dados)
            });
        } catch (error) {
            alert(`Não foi possível atualizar o cadastro. ${error.message}`);
            return;
        } finally {
            if (botao) botao.disabled = false;
        }
        alert("Cadastro atualizado com sucesso!");
        window.fecharModalEditar();
        executarFiltroAtual();
    });
});
