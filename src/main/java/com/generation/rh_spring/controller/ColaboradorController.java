package com.generation.rh_spring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.generation.rh_spring.model.Colaborador;
import com.generation.rh_spring.records.CalculoSalario;
import com.generation.rh_spring.records.Holerite;
import com.generation.rh_spring.service.CalcularSalarioService;
import com.generation.rh_spring.service.ColaboradorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/colaboradores")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ColaboradorController {

	@Autowired
	private ColaboradorService colaboradorService;

	@Autowired
	private CalcularSalarioService calcularSalarioService;

	@GetMapping
	public ResponseEntity<List<Colaborador>> getAll() {
		return ResponseEntity.ok(colaboradorService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Colaborador> getById(@PathVariable Long id) {
		return colaboradorService.getById(id);
	}

	@GetMapping("/nome/{nome}")
	public ResponseEntity<List<Colaborador>> getByNome(@PathVariable String nome) {
		return ResponseEntity.ok(colaboradorService.getByNome(nome));
	}

	@PostMapping
	public ResponseEntity<Colaborador> post(@Valid @RequestBody Colaborador colaborador) {
		return colaboradorService.post(colaborador);
	}

	@PutMapping
	public ResponseEntity<Colaborador> put(@Valid @RequestBody Colaborador colaborador) {
		return colaboradorService.put(colaborador);
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		colaboradorService.delete(id);
	}

	@PostMapping("/calcularsalario/{id}")
	public ResponseEntity<Holerite> calcularSalario(
			@PathVariable Long id,
			@RequestBody CalculoSalario dadosSalario) {

		Holerite holerite = calcularSalarioService.calcularSalario(id, dadosSalario);
		return ResponseEntity.status(HttpStatus.OK).body(holerite);
	}
}
