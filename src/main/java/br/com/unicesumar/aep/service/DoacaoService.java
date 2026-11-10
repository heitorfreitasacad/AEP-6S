package br.com.unicesumar.aep.service;

import br.com.unicesumar.aep.exception.DoacaoInvalidaException;
import br.com.unicesumar.aep.exception.DoacaoNaoEncontradaException;
import br.com.unicesumar.aep.exception.InstituicaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.repository.DoacaoRepository;
import br.com.unicesumar.aep.repository.InstituicaoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negocio do cadastro de doacoes: validacao de dados
 * de entrada (incluindo a existencia da instituicao destinataria) e
 * traducao de "nao encontrado" em excecao de dominio.
 */
public class DoacaoService {

    private final DoacaoRepository repository;
    private final InstituicaoRepository instituicaoRepository;

    public DoacaoService(DoacaoRepository repository, InstituicaoRepository instituicaoRepository) {
        this.repository = repository;
        this.instituicaoRepository = instituicaoRepository;
    }

    public Doacao registrar(String doador, String item, double quantidade, String unidade, LocalDate dataDoacao,
                            String instituicaoId) {
        validar(doador, item, quantidade, unidade, dataDoacao, instituicaoId);
        Doacao doacao = new Doacao(doador, item, quantidade, unidade, dataDoacao, instituicaoId);
        return repository.salvar(doacao);
    }

    public List<Doacao> listar() {
        return repository.listarTodas();
    }

    public List<Doacao> listarPorInstituicao(String instituicaoId) {
        if (instituicaoRepository.buscarPorId(instituicaoId).isEmpty()) {
            throw new InstituicaoNaoEncontradaException(instituicaoId);
        }
        return repository.listarPorInstituicao(instituicaoId);
    }

    public Doacao buscarPorId(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new DoacaoNaoEncontradaException(id));
    }

    public void atualizarStatus(String id, StatusDoacao status) {
        if (status == null) {
            throw new DoacaoInvalidaException("Status nao pode ser nulo.");
        }
        boolean atualizado = repository.atualizarStatus(id, status);
        if (!atualizado) {
            throw new DoacaoNaoEncontradaException(id);
        }
    }

    public void remover(String id) {
        boolean removido = repository.remover(id);
        if (!removido) {
            throw new DoacaoNaoEncontradaException(id);
        }
    }

    private void validar(String doador, String item, double quantidade, String unidade, LocalDate dataDoacao,
                         String instituicaoId) {
        if (doador == null || doador.isBlank()) {
            throw new DoacaoInvalidaException("Doador e obrigatorio.");
        }
        if (item == null || item.isBlank()) {
            throw new DoacaoInvalidaException("Item e obrigatorio.");
        }
        if (quantidade <= 0) {
            throw new DoacaoInvalidaException("Quantidade deve ser maior que zero.");
        }
        if (unidade == null || unidade.isBlank()) {
            throw new DoacaoInvalidaException("Unidade e obrigatoria.");
        }
        if (dataDoacao == null) {
            throw new DoacaoInvalidaException("Data da doacao e obrigatoria.");
        }
        if (dataDoacao.isAfter(LocalDate.now())) {
            throw new DoacaoInvalidaException("Data da doacao nao pode ser futura.");
        }
        if (instituicaoId == null || instituicaoId.isBlank()) {
            throw new DoacaoInvalidaException("Instituicao e obrigatoria.");
        }
        if (instituicaoRepository.buscarPorId(instituicaoId).isEmpty()) {
            throw new DoacaoInvalidaException("Instituicao nao encontrada para o id: " + instituicaoId);
        }
    }
}
