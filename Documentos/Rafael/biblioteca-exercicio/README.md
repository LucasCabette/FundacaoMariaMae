# Biblioteca — exercício

Este é o ponto de partida dos alunos. Ele contém a aplicação web e as regras de negócio, mas **não possui testes nem arquivos de configuração de perfil**.

## Objetivo

Criar testes para as regras de empréstimo da biblioteca:

1. Testes unitários para `Livro` e `Emprestimo`.
2. Testes de `EmprestimoService` com Mockito, simulando os repositórios e `AlertaEmprestimoService`.
3. Testes de integração dos repositórios com H2 em memória.
4. Testes web de controllers com `MockMvc`.

## Configuração que deve ser criada

Os alunos devem criar o perfil `test` e configurá-lo com H2 em memória. A configuração precisa separar o banco usado pelos testes do ambiente de desenvolvimento. Depois, devem executar:

```bash
mvn test
```

## Regras a cobrir

- Livro indisponível não pode ser emprestado.
- Um aluno possui no máximo três empréstimos ativos.
- Devolver um empréstimo libera o livro.
- Um empréstimo já devolvido não pode ser devolvido novamente.
- Um alerta é enviado após um empréstimo válido.
