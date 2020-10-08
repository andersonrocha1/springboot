package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportSistem reportSistem;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@RequestMapping(method=RequestMethod.GET, value="/cadastropessoa")
	public ModelAndView inicio() {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("objpessoa", new Pessoa());
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 7, Sort.by("nome"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
		
		
	}
	
	@RequestMapping(method=RequestMethod.POST, value="**/salvarpessoa", consumes = "multipart/form-data")
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindResult, final MultipartFile file) throws IOException {
		
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		//Fazendo validação dos campos
		if(bindResult.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
			
			andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 7, Sort.by("nome"))));
			andView.addObject("objpessoa", pessoa);
			
			List<String> msg = new ArrayList<String>();
			
			for(ObjectError objError : bindResult.getAllErrors()) {
				
				msg.add(objError.getDefaultMessage());// Gerado pelas anotações
				
			}
			andView.addObject("msg", msg);
			andView.addObject("profissoes", profissaoRepository.findAll());
			return andView;
			
			
		}
		if(file.getSize() > 0) {/*Cadastrando um currículo*/
			
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
			
			
		}else {
			
			if(pessoa.getId() != null && pessoa.getId() > 0 ) {/*Editando um currículo*/
				
				Pessoa pessoaTemporario = pessoaRepository.findById(pessoa.getId()).get();
				pessoa.setCurriculo(pessoaTemporario.getCurriculo());
				pessoa.setTipoFileCurriculo(pessoa.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoa.getNomeFileCurriculo());
			}
		}
		pessoaRepository.save(pessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 7, Sort.by("nome"))));//Lista com paginação
		andView.addObject("objpessoa", new Pessoa());
		
		return andView;
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/listarpessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 7, Sort.by("nome"))));
		andView.addObject("objpessoa", new Pessoa());
		return andView;
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("objpessoa", pessoa.get());
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 7, Sort.by("nome"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		return modelAndView;
	
	}
	
	
	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {
		
		pessoaRepository.deleteById(idpessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 7, Sort.by("nome"))));
		modelAndView.addObject("objpessoa", new Pessoa());
		return modelAndView;
	
	}
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarCurriculo(@PathVariable("idpessoa") Long idpessoa, 
			HttpServletResponse response) throws IOException {
		
		/*Consultar objeto pessoa no banco de dados*/
		
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		
		/*Setar o tamanho da resposta*/
		response.setContentLength(pessoa.getCurriculo().length);
		
		/*Tipo da Resposta*/
		response.setContentType(pessoa.getTipoFileCurriculo());
		
		/*Define o cabeçalho da resposta*/
		
		String HeaderKey = "Content-Disposition";
		String HeaderValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
		
		response.setHeader(HeaderKey, HeaderValue);
		
		/*Finaliza a resposta passando o arquivo*/
		response.getOutputStream().write(pessoa.getCurriculo());
		
	}
	
	
	
	@PostMapping("**/buscarpessoas")// Adicionei a pesquisa por sexo...
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("pesqsexo") String pesqsexo,
			@PageableDefault(size = 7, sort= {"nome"}) Pageable pageable) { 
		
		Page<Pessoa> pessoas = null;
		
		if(pesqsexo != null && !pesqsexo.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty() ) {
			 pessoas = pessoaRepository.findPessoaByNameSexoPage(nomepesquisa, pesqsexo, pageable);
		}else if(nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}else if(pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(pesqsexo, pageable);
		}
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("objpessoa", new Pessoa());
		modelAndView.addObject("nomepesquisa", nomepesquisa);
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		
		return modelAndView;
		
	}
	
	@GetMapping("**/buscarpessoas") // Busca o arquivo Jasper mapeando como Get para não redirecionar a página 
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("pesqsexo") String pesqsexo,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
			List<Pessoa> pessoas = new ArrayList<Pessoa>();
			
			if(pesqsexo != null && !pesqsexo.isEmpty() &&
					nomepesquisa != null && !nomepesquisa.isEmpty()) {// Busca por nome e sexo
				
				pessoas =  pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesqsexo);
				
			}else if (nomepesquisa != null && !nomepesquisa.isEmpty()){// Busca pelo nome
				
				pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
				
			}else if (pesqsexo != null && !pesqsexo.isEmpty()){// Busca somente por sexo
				
				pessoas = pessoaRepository.findPessoaBySexo(pesqsexo);
			}
			else{
				// Lista todos caso não entre nas condições acima !
				Iterable<Pessoa> iterator = pessoaRepository.findAll();
				
				for (Pessoa pessoa : iterator) {
					pessoas.add(pessoa);
				}
				
			}
			/*Chamo o serviço que gera o relatório*/
			byte[] pdf = reportSistem.geraRelatorio(pessoas, "pessoa", request.getServletContext());
			
			/*Tamanho da resposta ao navegador*/
			response.setContentLength(pdf.length);
			
			/*Defino na resposta o tipo de arquivo*/
			response.setContentType("application/octet-stream");
			
			/*Defino o cabeçalho da nossa resposta*/
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
			response.setHeader(headerKey, headerValue);
			
			/*Finalizo a resposta para o navegador*/
			response.getOutputStream().write(pdf);
			
	}

	
	@GetMapping("/cadastrotelefone/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
		modelAndView.addObject("objpessoa", pessoa.get());
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return modelAndView;
	
	}
	
	@PostMapping("**/addfonepessoa/{pessoaid}")
	public ModelAndView AddTelefonesPessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {
		
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		
		//Validando os campos de telefone
		if(telefone != null && (telefone.getNumero().isEmpty() ||(telefone.getTipo().isEmpty()))) {
			
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
			modelAndView.addObject("objpessoa", pessoa);
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
			List<String> msg = new ArrayList<String>();
			if(telefone.getNumero().isEmpty()) {
				
				msg.add("O número deve ser informado!");
			}if(telefone.getTipo().isEmpty()){
				msg.add("Escolha o tipo do telefone, EX: Residencial, Celular, Comercial, etc.");
			}
			modelAndView.addObject("msg", msg);
			
			return modelAndView;
			
			
			
		}
		
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
		modelAndView.addObject("objpessoa", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		return modelAndView;
		
	}
	
	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView excluirTelefone(@PathVariable("idtelefone") Long idtelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idtelefone);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
		modelAndView.addObject("objpessoa", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		return modelAndView;
	
	}
	
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoaPaginacao(@PageableDefault(size = 7) Pageable pageable, 
			ModelAndView model, @RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("objpessoa", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
		
	}

}
