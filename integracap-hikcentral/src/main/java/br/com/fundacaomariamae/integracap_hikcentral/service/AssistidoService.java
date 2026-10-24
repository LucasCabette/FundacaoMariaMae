package br.com.fundacaomariamae.integracap_hikcentral.service;

import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoRequest;
import br.com.fundacaomariamae.integracap_hikcentral.dto.AssistidoResponse;
import br.com.fundacaomariamae.integracap_hikcentral.model.Assistido;
import br.com.fundacaomariamae.integracap_hikcentral.model.StatusAssistido;
import br.com.fundacaomariamae.integracap_hikcentral.repository.AssistidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@Transactional
public class AssistidoService {
    private final AssistidoRepository repository;

    public AssistidoService(AssistidoRepository repository) {
        this.repository = repository;
    }

    public AssistidoResponse cadastrar(AssistidoRequest request) {
        if (repository.findByCodigoAssistido(request.codigoAssistido()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código do assistido já cadastrado.");
        }
        if (request.cpf() != null && !request.cpf().isBlank()
                && repository.findByCpf(request.cpf()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        if (repository.existsByNomeIgnoreCaseAndDataNascimento(
                request.nome().trim(), request.dataNascimento())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe assistido com esse nome e data de nascimento. Confira antes de cadastrar.");
        }

        Assistido a = new Assistido();
        a.setCodigoAssistido(request.codigoAssistido().trim());
        a.setNome(request.nome().trim());
        a.setDataNascimento(request.dataNascimento());
        a.setNomeMae(request.nomeMae());
        a.setNomePai(request.nomePai());
        a.setCpf(request.cpf());
        a.setRg(request.rg());
        a.setCidadeNascimento(request.cidadeNascimento());
        a.setSexo(request.sexo());
        a.setStatus(StatusAssistido.ATIVO);

        return AssistidoResponse.from(repository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AssistidoResponse> listar(String nome) {
        List<Assistido> encontrados = (nome == null || nome.isBlank())
                ? repository.findAll()
                : repository.findByNomeContainingIgnoreCase(nome.trim());
        return encontrados.stream().map(AssistidoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AssistidoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(AssistidoResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistido não encontrado."));
    }

    public AssistidoResponse atualizar(Long id, AssistidoRequest request) {
        Assistido a = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistido não encontrado."));

        repository.findByCodigoAssistido(request.codigoAssistido()).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Código do assistido já pertence a outro cadastro.");
            }
        });
        if (request.cpf() != null && !request.cpf().isBlank()) {
            repository.findByCpf(request.cpf()).ifPresent(outro -> {
                if (!outro.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já pertence a outro cadastro.");
                }
            });
        }

        a.setCodigoAssistido(request.codigoAssistido().trim());
        a.setNome(request.nome().trim());
        a.setDataNascimento(request.dataNascimento());
        a.setNomeMae(request.nomeMae());
        a.setNomePai(request.nomePai());
        a.setCpf(request.cpf());
        a.setRg(request.rg());
        a.setCidadeNascimento(request.cidadeNascimento());
        a.setSexo(request.sexo());
        return AssistidoResponse.from(repository.save(a));
    }

    public void inativar(Long id) {
        Assistido a = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistido não encontrado."));
        a.setStatus(StatusAssistido.INATIVO);
        repository.save(a);
    }
}
