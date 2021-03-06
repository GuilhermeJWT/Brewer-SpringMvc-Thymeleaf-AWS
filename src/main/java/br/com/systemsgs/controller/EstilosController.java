package br.com.systemsgs.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.systemgs.util.EstiloFilter;
import br.com.systemsgs.controller.page.PageWrapper;
import br.com.systemsgs.exception.NomeEstiloJaCadastradoException;
import br.com.systemsgs.model.ModelEstilo;
import br.com.systemsgs.repository.EstilosRepository;
import br.com.systemsgs.service.EstiloService;

@Controller
@RequestMapping(value = "/estilos")
public class EstilosController {

	@Autowired
	private EstiloService estiloService;
	
	@Autowired
	private EstilosRepository estiloRepository;

	@RequestMapping("/novo")
	public ModelAndView novo(ModelEstilo modelEstilo) {
		return new ModelAndView("estilo/CadastroEstilo");
	}

	@RequestMapping(value = "/novo", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ModelAndView cadastrar(@Valid ModelEstilo modelEstilo, BindingResult result, RedirectAttributes attributes) {

		if (result.hasErrors()) {
			return novo(modelEstilo);
		}

		try {
			estiloService.salvar(modelEstilo);
		} catch (NomeEstiloJaCadastradoException e) {
			result.rejectValue("nome", e.getMessage(), e.getMessage());
			return novo(modelEstilo);
		}

		attributes.addFlashAttribute("mensagem", "Estilo Salvo com sucesso");
		return new ModelAndView("redirect:/estilos/novo");
	}

	/* Salva pelo Modal de Estilo */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ResponseEntity<?> salvar(@RequestBody @Valid ModelEstilo modelEstilo, BindingResult result) {

		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body(result.getFieldError("nome").getDefaultMessage());
		}

		try {
			modelEstilo = estiloService.salvar(modelEstilo);
		} catch (NomeEstiloJaCadastradoException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

		return ResponseEntity.ok(modelEstilo);
	}

	@GetMapping
	public ModelAndView pesquisar(EstiloFilter estiloFilter, BindingResult result, @PageableDefault(size = 2) Pageable pageable, HttpServletRequest httpServletRequest) {
		ModelAndView mv = new ModelAndView("estilo/PesquisaEstilos");

		PageWrapper<ModelEstilo> paginaWrapper = new PageWrapper<>(estiloRepository.filtrar(estiloFilter, pageable),httpServletRequest);
		mv.addObject("pagina", paginaWrapper);
		return mv;
	}

}
