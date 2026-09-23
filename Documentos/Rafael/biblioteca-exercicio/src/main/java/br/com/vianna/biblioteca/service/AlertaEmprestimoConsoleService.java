package br.com.vianna.biblioteca.service;
import br.com.vianna.biblioteca.domain.Emprestimo;
import org.springframework.stereotype.Service;

@Service
public class AlertaEmprestimoConsoleService implements AlertaEmprestimoService {
    @Override public void enviar(Emprestimo emprestimo) {
        System.out.printf("Empréstimo registrado: %s para %s%n", emprestimo.getLivro().getTitulo(), emprestimo.getAluno().getNome());
    }
}
