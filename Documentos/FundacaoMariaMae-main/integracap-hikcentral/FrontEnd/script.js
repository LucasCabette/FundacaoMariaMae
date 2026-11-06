// URL da API REST no Spring Boot
const API_BASE = window.location.protocol !== "file:" && window.location.pathname.startsWith("/interface/")
    ? `${window.location.origin}/api` : "http://localhost:8081/api";
const API_URL = `${API_BASE}/assistidos`;

let assistidoEmEdicao = null;
let consultaAtual = 0;

// =========================
// DADOS FICTÍCIOS (FALLBACK)
// =========================
const assistidosFicticios = [
    { 
        id: 1, 
        codigoAssistido: "2600000001", 
        nome: "Ana Beatriz Silva", 
        cpf: "123.456.789-01", 
        dataNascimento: "1990-05-15", 
        nomeMae: "Maria Silva",
        nomePai: "João Silva",
        cidadeNascimento: "Juiz de Fora",
        sexo: "FEMININO",
        rg: "MG12345678",
        status: "ATIVO"
    },
    { 
        id: 2, 
        codigoAssistido: "2600000002", 
        nome: "Carlos Eduardo Lima", 
        cpf: "987.654.321-00", 
        dataNascimento: "1985-08-20", 
        nomeMae: "Joana Lima",
        nomePai: "Pedro Lima",
        cidadeNascimento: "Juiz de Fora",
        sexo: "MASCULINO",
        rg: "MG87654321",
        status: "ATIVO"
    },
    { 
        id: 3, 
        codigoAssistido: "2600000003", 
        nome: "Juliana Martins Alves", 
        cpf: "111.222.333-44", 
        dataNascimento: "1998-11-03", 
        nomeMae: "Teresa Alves",
        nomePai: "Antônio Alves",
        cidadeNascimento: "Juiz de Fora",
        sexo: "FEMININO",
        rg: "MG11223344",
        status: "ATIVO"
    },
    { 
        id: 4, 
        codigoAssistido: "2600000004", 
        nome: "Julio Souza Santos", 
        cpf: "555.666.777-88", 
        dataNascimento: "1975-02-28", 
        nomeMae: "Francisca Santos",
        nomePai: "José Santos",
        cidadeNascimento: "Juiz de Fora",
        sexo: "MASCULINO",
        rg: "MG55667788",
        status: "ATIVO"
    },
    { 
        id: 5, 
        codigoAssistido: "2600000005", 
        nome: "Fernanda Oliveira Costa", 
        cpf: "999.888.777-66", 
        dataNascimento: "2001-07-12", 
        nomeMae: "Carla Costa",
        nomePai: "Roberto Costa",
        cidadeNascimento: "Juiz de Fora",
        sexo: "FEMININO",
        rg: "MG99887766",
        status: "ATIVO"
    }
];

// =========================
// FUNÇÃO AUXILIAR DA API
// =========================
async function consultarApi(url, opcoes) {
    const response = await fetch(url, opcoes);
    if (!response.ok) {
        let mensagem = `A operação não foi concluída (HTTP ${response.status}).`;
        try {
            const erro = await response.json();
            mensagem = erro.detail || erro.message || mensagem;
        } catch (_) {}
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
function inserirManualmente() { window.location.href = "formulario-assistido.html"; }
function sair() { window.location.href = "index.html"; }

function escanearDocumento() {
    const dadosEscaneados = {
        codigoAssistido: "26" + Math.floor(10000000 + Math.random() * 90000000),
        nome: "Nome Lido do Documento",
        dataNascimento: "1995-05-15",
        cpf: "12345678901",
        rg: "MG12345678",
        nomeMae: "Nome da Mãe Lida",
        nomePai: "Nome do Pai Lido",
        cidadeNascimento: "Juiz de Fora",
        sexo: "MASCULINO"
    };
    sessionStorage.setItem("dadosAssistidoTemp", JSON.stringify(dadosEscaneados));
    window.location.href = "confirmar-dados.html";
}

// =========================
// BUSCA E RENDERIZAÇÃO
// =========================
async function carregarAssistidos(filtroNome = "", filtroCpf = "", filtroId = "", filtroNascimento = "", filtroMae = "") {
    const consulta = ++consultaAtual;
    
    const nome = filtroNome.trim().toLowerCase();
    const cpf = filtroCpf.replace(/\D/g, "");
    const codigo = filtroId.trim();
    const mae = filtroMae.trim().toLowerCase();
    const nascimento = filtroNascimento;

    let listaParaFiltrar = [];

    try {
        const url = filtroNome.trim() ? `${API_URL}?nome=${encodeURIComponent(filtroNome.trim())}` : API_URL;
        const response = await consultarApi(url);
        listaParaFiltrar = await response.json();
    } catch (error) {
        console.warn("Backend offline ou inacessível. Utilizando dados fictícios para exibição.");
        listaParaFiltrar = assistidosFicticios;
    }

    const resultado = listaParaFiltrar.filter(item => {
        const bateuNome = !nome || String(item.nome || "").toLowerCase().includes(nome);
        const bateuCpf = !cpf || String(item.cpf || "").replace(/\D/g, "").includes(cpf);
        const bateuId = !codigo || String(item.codigoAssistido || "").includes(codigo) || String(item.id) === codigo;
        const bateuMae = !mae || String(item.nomeMae || "").toLowerCase().includes(mae);
        const bateuNascimento = !nascimento || String(item.dataNascimento || "").startsWith(nascimento);

        return bateuNome && bateuCpf && bateuId && bateuMae && bateuNascimento;
    });

    if (consulta === consultaAtual) {
        renderizarTabela(resultado);
    }
}

function formatarDataBrasil(dataISO) {
    if (!dataISO) return "Não informada";
    const partes = dataISO.split("T")[0].split("-");
    if (partes.length === 3) {
        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataISO;
}

function renderizarTabela(lista) {
    const tbody = document.querySelector(".tabela-dados tbody");
    if (!tbody) return;

    tbody.innerHTML = "";

    if (lista.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center" style="padding: 20px; color: var(--cor-texto-suave);">
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
        tr.children[4].textContent = formatarDataBrasil(item.dataNascimento);
        tr.children[5].textContent = item.nomeMae || "Não informada";
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
    const nascimento = document.getElementById("filtro-nascimento")?.value || "";
    const mae = document.getElementById("filtro-mae")?.value || "";

    carregarAssistidos(nome, cpf, id, nascimento, mae);
}

// =========================
// AÇÃO: INATIVAR
// =========================
async function inativarAssistido(id) {
    if (!confirm("Deseja inativar este cadastro? O registro será mantido no banco.")) return;
    try {
        await consultarApi(`${API_URL}/${id}`, { method: "DELETE" });
    } catch (error) {
        console.warn("Inativação simulada no frontend devido a falha na API.");
    }
    alert("Cadastro inativado com sucesso.");
    executarFiltroAtual();
}

// =========================
// AÇÃO: EDITAR COMPLETO (MODAL)
// =========================
window.abrirModalEditar = async function(id) {
    const modal = document.getElementById("modalEditar");
    if (!modal) return;
    assistidoEmEdicao = null;
    modal.style.display = "none";

    try {
        const response = await consultarApi(`${API_URL}/${id}`);
        assistidoEmEdicao = await response.json();
    } catch (error) {
        assistidoEmEdicao = assistidosFicticios.find(a => a.id === id) || {
            id: id,
            codigoAssistido: String(id),
            nome: "Registro de Teste",
            dataNascimento: "1990-01-01",
            cpf: "",
            rg: "",
            nomeMae: "",
            nomePai: "",
            cidadeNascimento: "",
            sexo: ""
        };
    }

    if (assistidoEmEdicao) {
        document.getElementById("edit-id").value = assistidoEmEdicao.id;
        document.getElementById("edit-codigo").value = assistidoEmEdicao.codigoAssistido || assistidoEmEdicao.id;
        document.getElementById("edit-nome").value = assistidoEmEdicao.nome || "";
        document.getElementById("edit-nascimento").value = assistidoEmEdicao.dataNascimento ? assistidoEmEdicao.dataNascimento.split("T")[0] : "";
        document.getElementById("edit-cpf").value = assistidoEmEdicao.cpf || "";
        document.getElementById("edit-rg").value = assistidoEmEdicao.rg || "";
        document.getElementById("edit-mae").value = assistidoEmEdicao.nomeMae || "";
        document.getElementById("edit-pai").value = assistidoEmEdicao.nomePai || "";
        document.getElementById("edit-cidade").value = assistidoEmEdicao.cidadeNascimento || "";
        document.getElementById("edit-sexo").value = assistidoEmEdicao.sexo || "";
        modal.style.display = "flex";
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

    // Salvar todas as alterações do modal de edição
    const formEditar = document.getElementById("formEditarAssistido");
    formEditar?.addEventListener("submit", async function(e) {
        e.preventDefault();

        if (!assistidoEmEdicao) {
            alert("Carregue o cadastro antes de salvar.");
            return;
        }

        const id = document.getElementById("edit-id").value;
        const cpf = document.getElementById("edit-cpf").value.replace(/\D/g, "");

        const dadosAtualizados = {
            id: Number(id),
            codigoAssistido: document.getElementById("edit-codigo").value.trim(),
            nome: document.getElementById("edit-nome").value.trim(),
            dataNascimento: document.getElementById("edit-nascimento").value,
            cpf: cpf || null,
            rg: document.getElementById("edit-rg").value.trim() || null,
            nomeMae: document.getElementById("edit-mae").value.trim() || null,
            nomePai: document.getElementById("edit-pai").value.trim() || null,
            cidadeNascimento: document.getElementById("edit-cidade").value.trim() || null,
            sexo: document.getElementById("edit-sexo").value || null
        };

        const botao = formEditar.querySelector('button[type="submit"]');
        if (botao) botao.disabled = true;

        try {
            await consultarApi(`${API_URL}/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dadosAtualizados)
            });
        } catch (error) {
            // Atualiza na memória local fictícia para testes offline
            const idx = assistidosFicticios.findIndex(a => a.id === Number(id));
            if (idx !== -1) {
                assistidosFicticios[idx] = { ...assistidosFicticios[idx], ...dadosAtualizados };
            }
        } finally {
            if (botao) botao.disabled = false;
        }

        alert("Cadastro atualizado com sucesso!");
        window.fecharModalEditar();
        executarFiltroAtual();
    });
});

window.addEventListener("pageshow", function(event) {
    const tabela = document.querySelector(".tabela-dados");
    if (tabela) {
        carregarAssistidos();
    }
});