package br.com.vianna.biblioteca.service;
import br.com.vianna.biblioteca.domain.Aluno;
import br.com.vianna.biblioteca.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlunoService {
    private final AlunoRepository repository;
    public AlunoService(AlunoRepository repository) { this.repository = repository; }
    public Aluno cadastrar(Aluno aluno) { return repository.save(aluno); }
    public List<Aluno> listar() { return repository.findAll(); }
}
