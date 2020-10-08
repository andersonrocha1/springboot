package curso.springboot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Profissao {
	

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String nomeprofissao;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeprofissao() {
		return nomeprofissao;
	}

	public void setNomeprofissao(String nomeprofissao) {
		this.nomeprofissao = nomeprofissao;
	}
	
	
	

}
