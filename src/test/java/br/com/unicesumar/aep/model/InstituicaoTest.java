package br.com.unicesumar.aep.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstituicaoTest {

    @Test
    void deveCriarInstituicaoComEnderecoEContatosAninhados() {
        Endereco endereco = new Endereco("Rua A", "Maringa", "PR");
        Contato contato = new Contato("Ana", "44999990000", "ana@ong.org");

        Instituicao instituicao = new Instituicao("Banco de Alimentos", endereco, List.of(contato));

        assertEquals("Banco de Alimentos", instituicao.getNome());
        assertEquals("Maringa", instituicao.getEndereco().getCidade());
        assertEquals(1, instituicao.getContatos().size());
        assertEquals("Ana", instituicao.getContatos().get(0).getNome());
    }

    @Test
    void deveIniciarComListaDeContatosVaziaQuandoNaoInformada() {
        Instituicao instituicao = new Instituicao("ONG", null, null);

        assertNotNull(instituicao.getContatos());
        assertTrue(instituicao.getContatos().isEmpty());
    }

    @Test
    void devePermitirAlterarCamposPorSetters() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId("1");
        instituicao.setNome("ONG Esperanca");
        instituicao.setEndereco(new Endereco());
        instituicao.getEndereco().setRua("Rua B");
        instituicao.getEndereco().setCidade("Londrina");
        instituicao.getEndereco().setEstado("PR");
        Contato contato = new Contato();
        contato.setNome("Joao");
        contato.setTelefone("43988880000");
        contato.setEmail("joao@ong.org");
        instituicao.setContatos(List.of(contato));

        assertEquals("1", instituicao.getId());
        assertEquals("Rua B", instituicao.getEndereco().getRua());
        assertEquals("PR", instituicao.getEndereco().getEstado());
        assertEquals("43988880000", instituicao.getContatos().get(0).getTelefone());
        assertEquals("joao@ong.org", instituicao.getContatos().get(0).getEmail());
    }

    @Test
    void duasInstituicoesComMesmoIdDevemSerIguais() {
        Instituicao a = new Instituicao();
        a.setId("1");
        Instituicao b = new Instituicao();
        b.setId("1");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, null);
        assertNotEquals(a, "outro tipo");
    }

    @Test
    void toStringDeveConterNomeEEndereco() {
        Instituicao instituicao = new Instituicao("ONG", new Endereco("Rua A", "Maringa", "PR"),
                List.of(new Contato("Ana", "1", "a@a.com")));

        String texto = instituicao.toString();

        assertTrue(texto.contains("ONG"));
        assertTrue(texto.contains("Maringa/PR"));
        assertTrue(texto.contains("Ana"));
    }
}
