package com.generation.rh_spring.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.generation.rh_spring.model.CalculoSalario;
import com.generation.rh_spring.model.Colaborador;
import com.generation.rh_spring.model.Holerite;
import com.generation.rh_spring.repository.ColaboradorRepository;
import com.generation.rh_spring.repository.DepartamentoRepository;
import com.generation.rh_spring.service.CalcularSalarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/colaboradores")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ColaboardorController {

	@Autowired
	private ColaboradorRepository colaboradorRepository;

	@Autowired
	private DepartamentoRepository departamentoRepository;
	
	@Autowired
    private CalcularSalarioService calcularSalarioService;

	@GetMapping
	public ResponseEntity<List<Colaborador>> getAll() {
		return ResponseEntity.ok(colaboradorRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Colaborador> getById(@PathVariable Long id) {
		return colaboradorRepository.findById(id).map(resp -> ResponseEntity.ok(resp))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@GetMapping("/nome/{nome}")
	public ResponseEntity<List<Colaborador>> getByNome(@PathVariable String nome) {
		return ResponseEntity.ok(colaboradorRepository.findAllByNomeContainingIgnoreCase(nome));
	}

	@PostMapping
	public ResponseEntity<Colaborador> post(@Valid @RequestBody Colaborador colaborador) {
		if (departamentoRepository.existsById(colaborador.getDepartamento().getId()))
			return ResponseEntity.status(HttpStatus.CREATED).body(colaboradorRepository.save(colaborador));

		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento não existe!", null);
	}

	@PutMapping
	public ResponseEntity<Colaborador> put(@Valid @RequestBody Colaborador colaborador) {
		if (colaboradorRepository.existsById(colaborador.getId())) {

			if (departamentoRepository.existsById(colaborador.getDepartamento().getId()))
				return ResponseEntity.status(HttpStatus.OK).body(colaboradorRepository.save(colaborador));

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento não existe!", null);

		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		Optional<Colaborador> colaborador = colaboradorRepository.findById(id);

		if (colaborador.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		colaboradorRepository.deleteById(id);
	}

	@PostMapping("/calcularsalario/{id}")
    public ResponseEntity<Holerite> calcularSalario(
            @PathVariable Long id, 
            @RequestBody CalculoSalario dadosSalario
    ) {
        Holerite holerite = calcularSalarioService.calcularSalario(id, dadosSalario);
        return ResponseEntity.status(HttpStatus.OK).body(holerite);
    }
	
}
