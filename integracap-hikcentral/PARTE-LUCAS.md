# Parte do Lucas — preparação de cadastros e fotos

Entrega correspondente ao escopo informado para 09/10/2026: formatar e preparar cadastros e fotos para futura integração com HikCentral. Não altera passagens nem relatórios.

## Arquivos acrescentados

- `dto/CadastroEntrada.java`: representa código de origem, nome e código da organização; exige os três campos. São campos provisórios até confirmar o modelo da origem.
- `service/PreparacaoCadastroService.java`: valida campos e foto, remove espaços externos, preserva códigos com zeros à esquerda, prepara Base64, hash SHA-256 e metadados da imagem. Não persiste nem envia dados.
- `controller/PreparacaoCadastroController.java`: recebe JSON e foto por multipart em `POST /api/sincronizacao/preparar`; retorna o resultado e responde HTTP 400 para entradas inválidas. O tratamento de erro fica neste controller, sem modificar o comportamento dos controllers de outras partes.

## Limites e contrato

A imagem recebida mostra HikCentral Professional 3.0.1.20250828, não a versão do OpenAPI. O resultado é marcado como `PREPARADO_LOCALMENTE` e `INTERNO_PROVISORIO`: não é um payload oficial validado. Base64 é uma representação interna nesta etapa.

Padrões locais: JPEG/PNG, até 2 MiB, até 16 milhões de pixels. Não são limites confirmados da Hikvision. Bytes e pixels podem ser configurados por `hikcentral.preparacao.max-foto-bytes` e `hikcentral.preparacao.max-pixels`; não foi alterado o application.properties compartilhado. Os limites HTTP de multipart continuam os padrões existentes do Spring e podem rejeitar a requisição antes do serviço. Na próxima etapa, alinhar esses limites com o limite do serviço.

São verificadas legibilidade e dimensões; não é verificada qualidade facial. O hash identifica os bytes da foto e não implementa controle de duplicatas de cadastros.

## Entrada e resultado

Parte multipart `cadastro`, Content-Type `application/json`:

```json
{"codigoOrigem":"0001","nome":"Pessoa de Teste","codigoOrganizacao":"ORG-TESTE"}
```

Parte multipart `foto`: arquivo JPEG/PNG fictício.

O resultado contém estado, contrato, cadastro normalizado e foto com tipo, tamanho, largura, altura, hash e Base64. A foto é devolvida na resposta; não usar dados reais na demonstração.

## Próxima etapa

Nenhum teste nem compilação desta alteração foi executado, conforme solicitado. Na próxima etapa: compilar, testar cadastro válido/inválido, multipart, fotos inválidas e limites; depois obter o manual OpenAPI compatível para definir o mapeamento e a assinatura reais. Não há autenticação do endpoint local, cliente HTTP, fila, persistência, reprocessamento nem envio ao HikCentral nesta etapa.

## Referência de compatibilidade

Hikvision orienta obter o OpenAPI correspondente ao HikCentral e configurar API Key/API Secret: https://tpp.hikvision.com/eu/HCPIntegration . O contrato exato da instalação 3.0.1 continua pendente de confirmação.
