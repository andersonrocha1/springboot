package curso.springboot.repository;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.springboot.model.Pessoa;


@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
	@Query("select tb From Pessoa tb where tb.nome like %?1% ")
	List<Pessoa> findPessoaByName(String nome);
	
	@Query("select tb From Pessoa tb where tb.sexopessoa like ?1 ")
	List<Pessoa> findPessoaBySexo(String sexo);
	
	@Query("select tb From Pessoa tb where tb.nome like %?1% and tb.sexopessoa = ?2 ")
	List<Pessoa> findPessoaByNameSexo(String nome, String sexopessoa);
	
	
	default Page<Pessoa> findPessoaByNamePage(String nome, Pageable pageable){
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		/*Estamos configurando a pesquisa para consultar por partes do nome no BD*/
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		/*Fazendo a união do objeto com o valor e a configuração de consulta */
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);
		
		
		Page<Pessoa> pessoas = findAll(example, pageable);
		
		return pessoas;
	}
	
	/*Método que para paginação do pesquisa por nome e sexo*/
	default Page<Pessoa> findPessoaByNameSexoPage(String nome, String sexo, Pageable pageable){
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		pessoa.setSexopessoa(sexo);
		/*Estamos configurando a pesquisa para consultar por partes do nome no BD*/
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
				.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
				.withMatcher("sexopessoa", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		/*Fazendo a união do objeto com o valor e a configuração de consulta */
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);
		
		
		Page<Pessoa> pessoas = findAll(example, pageable);
		
		return pessoas;
	}
	
	/*Método que para paginação do pesquisa por sexo*/
	default Page<Pessoa> findPessoaBySexoPage(String sexo, Pageable pageable){
		
		Pessoa pessoa = new Pessoa();
		pessoa.setSexopessoa(sexo);
		
		// Configurando pesquisa para consulta por partes do nome no banco (análogo ao LIKE do SQL)
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
				.withMatcher("sexopessoa", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		// Une objeto com valor e a configuração para consultar
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);
		
		return findAll(example, pageable);
		
	}
	
	
	

}
