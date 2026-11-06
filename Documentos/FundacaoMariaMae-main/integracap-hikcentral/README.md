# Fundação Maria Mãe

Aplicação de apoio ao cadastro de assistidos e à preparação de dados e fotos para futura integração com o HikCentral. Reúne backend Java/Spring Boot, banco SQLite e interface web.

## Executar

Requisito: JDK 21 ou posterior. A versão atual foi verificada com JDK 25. O Maven Wrapper acompanha o projeto; a primeira execução pode precisar de internet para baixar o Maven e as dependências.

Abra o PowerShell **na pasta que contém `pom.xml`, `mvnw.cmd`, `FrontEnd` e `data`**:

```powershell
.\mvnw.cmd spring-boot:run
```

Abra no navegador:

- Menu inicial: [localhost:8081/interface/index.html](http://localhost:8081/interface/index.html)
- Cadastros: [localhost:8081/interface/pessoas.html](http://localhost:8081/interface/pessoas.html)

Mantenha o terminal aberto. Para encerrar, pressione **Ctrl+C**. Após alterar o código Java, reinicie a aplicação; após alterar a interface, atualize a página com **Ctrl+F5**.

Use o endereço web para preparar fotos. As telas são servidas pelo Spring na mesma origem da API, preservando os arquivos na pasta `FrontEnd`.

## Funcionalidades disponíveis

- Cadastro, consulta, edição e inativação de assistidos pela API.
- Consulta, filtros de nome/CPF/código, edição e inativação pela interface.
- Preparação de cadastro e foto a partir de um assistido salvo, sem redigitar seus dados pessoais.
- Seleção de finalidade: preparar cadastro ou preparar uma nova foto.
- Validação de campos, imagens JPEG/PNG, tamanho e dimensões; geração de Base64 e hash SHA-256.
- Histórico persistente das tentativas, com resultado, finalidade, horários e motivo de falha.
- Estruturas de recebimento de passagens e consultas demonstrativas de relatórios/dashboard.

## Preparar uma foto pela interface

1. Abra **Consultar cadastros** e localize um assistido salvo.
2. Clique em **Preparar foto**.
3. Confira nome e código e escolha a finalidade.
4. Informe a organização de destino e selecione JPEG ou PNG de até 1 MB.
5. Clique em **Preparar foto** e confira o resultado e o histórico.

O serviço busca código, nome, nascimento, nome da mãe e CPF no SQLite. O CPF permanece opcional. Não foi acrescentada obrigatoriedade ao nome da mãe. A integração exige cadastro ativo, código e nome preenchidos, nascimento legível, foto válida e organização informada.

O código da organização é metadado provisório de destino, separado da ficha do assistido. `ORG-TESTE` pode ser usado com dados fictícios para verificação local; o valor e a exigência reais dependem da confirmação técnica com a Fundação.

**Preparar não significa enviar.** A resposta `PREPARADO_LOCALMENTE`, com contrato `INTERNO_PROVISORIO`, confirma apenas o processamento local. A foto não é salva nem substituída no banco, e nada é enviado ao HikCentral ou ao leitor facial. A coluna Foto ainda mostra o ícone do protótipo. A prévia do arquivo selecionado é local e descartada ao fechar a janela.

## API

Base: `http://localhost:8081`.

| Método | Rota | Finalidade |
| --- | --- | --- |
| POST | `/api/assistidos` | Cadastrar assistido |
| GET | `/api/assistidos?nome=...` | Listar ou pesquisar por nome |
| GET | `/api/assistidos/{id}` | Consultar cadastro |
| PUT | `/api/assistidos/{id}` | Atualizar cadastro completo |
| DELETE | `/api/assistidos/{id}` | Inativar, mantendo o registro |
| POST | `/api/sincronizacao/assistidos/{id}/preparar` | Preparar cadastro salvo e foto |
| GET | `/api/sincronizacao/preparacoes/{preparacaoId}` | Consultar uma tentativa |
| GET | `/api/sincronizacao/assistidos/{id}/preparacoes` | Listar tentativas do assistido |
| POST | `/api/sincronizacao/preparar` | Preparação independente, com cadastro JSON e foto |
| POST | `/api/passagens` | Receber dados de passagem |

Na preparação de cadastro salvo, envie multipart com `foto`, `codigoOrganizacao` e `finalidade` (`CADASTRO`, padrão, ou `ATUALIZACAO_FOTO`). O `{id}` é o identificador interno do assistido, e não seu código de cadastro.

```powershell
# Substitua o ID e o caminho por dados fictícios existentes no seu ambiente.
$idAssistido = 1
$resposta = curl.exe --fail-with-body "http://localhost:8081/api/sincronizacao/assistidos/$idAssistido/preparar" -F "finalidade=CADASTRO" -F "codigoOrganizacao=ORG-TESTE" -F "foto=@C:/caminho/foto-teste.jpg"
$dados = $resposta | ConvertFrom-Json
$dados | Select-Object preparacaoId,estado,finalidade,assistidoId,nome | Format-List

Invoke-RestMethod "http://localhost:8081/api/sincronizacao/preparacoes/$($dados.preparacaoId)" | Format-List
```

O endpoint independente mantém o contrato anterior e não participa do histórico de assistidos salvos.

## Resultados e falhas

| Estado | Significado |
| --- | --- |
| `EM_PREPARACAO` | Tentativa iniciada, ainda sem resultado final |
| `PREPARADO_LOCALMENTE` | Cadastro e foto preparados localmente |
| `FALHA_PREPARACAO` | Preparação rejeitada ou interrompida por erro registrado |

Cada chamada ao serviço de preparação de cadastro salvo ganha um UUID. Uma nova chamada cria outra tentativa sem apagar a anterior. O histórico guarda ID do assistido, finalidade, estado, código HTTP do resultado, motivo de falha, hash da foto no sucesso e horários UTC. Não guarda nome, CPF, foto ou Base64. Os metadados continuam associados ao cadastro pelo ID.

- `400`: entrada, foto ou finalidade inválida.
- `404`: cadastro ou tentativa não encontrados.
- `409`: cadastro inativo ou conflito cadastral, conforme a operação.
- `413`: upload acima do limite HTTP.
- `422`: dados salvos incompletos ou nascimento não reconhecido na preparação.

Erros de preparação que chegaram ao serviço devolvem `preparacaoId`. Requisições rejeitadas antes do serviço, como foto ausente, finalidade inválida ou upload excessivo, não geram tentativa. A consulta do histórico retorna HTTP 200 quando funciona; `httpStatus` informa o resultado original da tentativa.

A lista mostra até 50 tentativas, das mais recentes para as mais antigas. O parâmetro `?limite=100` amplia até 100. A interface mostra as últimas cinco. Sem histórico, a API retorna lista vazia, inclusive para um ID positivo inexistente.

Não existe reenvio automático nem fila. Para tentar outra vez, selecione/envie a foto novamente. Uma interrupção do processo pode deixar a tentativa em `EM_PREPARACAO`. Se o banco estiver indisponível, não é possível garantir o registro da falha.

## Organização do código

```text
FrontEnd/                 Telas, estilos e chamadas à API
data/                     Banco SQLite de trabalho
src/main/java/.../
  config/                 Publicação da interface pelo Spring
  controller/             Endpoints
  dto/                    Entradas e respostas
  model/                  Entidades e conversores de datas
  repository/             Acesso JPA aos cadastros
  service/                Cadastro, preparação, histórico e demais serviços
src/main/resources/       Configuração da aplicação
src/test/java/            Testes automatizados
```

## Banco e configuração

O banco fica em `data/fundacao_maria_mae.sqlite`, relativo à pasta de execução. Inicie sempre o projeto na pasta correta. É necessário acesso de escrita para cadastrar e registrar tentativas.

O JPA está configurado com `ddl-auto=none`: não cria nem altera automaticamente as tabelas de cadastro. A integração cria somente sua tabela `integracao_preparacao` e seu índice no primeiro uso. Não substitua o arquivo do banco ao atualizar apenas o código de um ambiente já utilizado.

Conversores explícitos permitem ler datas ISO e datas antigas em milissegundos no SQLite. Novos nascimentos são gravados em texto ISO; datas numéricas são interpretadas em UTC. A consulta de duplicidade compara nome e nascimento nos formatos antigos e novos, mantendo a regra existente. Não houve migração dos registros.

As propriedades estão em `src/main/resources/application.properties`. O serviço de imagens aceita até 2 MiB e 16 milhões de pixels por padrão, configuráveis em `hikcentral.preparacao.max-foto-bytes` e `hikcentral.preparacao.max-pixels`. O limite HTTP atual é menor; a interface usa 1 MB para acompanhá-lo. Esses são limites locais, não requisitos confirmados da Hikvision.

## Testes

```powershell
.\mvnw.cmd clean test
```

Verificação em **24/10/2026**: compilação concluída e **26 testes aprovados**. Os testes funcionais usam servidor HTTP em porta aleatória e SQLite temporário, com pessoas e imagens fictícias. Abrangem cadastros, datas, duplicidade, preparação, falhas, histórico e entrega dos arquivos da interface.

O fluxo da interface também foi conferido em navegador real com banco temporário: preparação de cadastro, nova foto, arquivo falso, limite de upload e consulta do histórico após reabrir a janela.

## Limites da versão atual

- O login é navegação de protótipo, sem autenticação real. Não há controle de acesso de produção.
- O formulário de inserção manual e o OCR continuam em desenvolvimento.
- Relatórios e dashboard possuem rotas demonstrativas; não representam uma consolidação completa do banco.
- A preparação não valida a qualidade biométrica de um rosto nem identifica duplicidade facial.
- A comunicação real, assinatura das chamadas, distribuição dos cadastros ao leitor e homologação com o HikCentral estão pendentes do contrato e das configurações da instalação.
- O anti-passback já utilizado na Fundação não foi reimplementado.

Use dados fictícios nas verificações locais. A versão informada para o Web Client é HikCentral Professional **3.0.1.20250828**; ela não confirma a versão do componente OpenAPI instalado.
