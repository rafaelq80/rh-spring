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

import com.generation.rh_spring.model.Departamento;
import com.generation.rh_spring.repository.DepartamentoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/departamentos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DepartamentoController {

	@Autowired
	private DepartamentoRepository departamentoRepository;

	@GetMapping
	public ResponseEntity<List<Departamento>> getAll() {
		return ResponseEntity.ok(departamentoRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Departamento> getById(@PathVariable Long id) {
		return departamentoRepository.findById(id).map(resposta -> ResponseEntity.ok(resposta))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@GetMapping("/descricao/{descricao}")
	public ResponseEntity<List<Departamento>> getByDescricao(@PathVariable String descricao) {
		return ResponseEntity.ok(departamentoRepository.findAllByDescricaoContainingIgnoreCase(descricao));
	}

	@PostMapping
	public ResponseEntity<Departamento> post(@Valid @RequestBody Departamento departamento) {

		System.out.println(departamento.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(departamentoRepository.save(departamento));
	}

	@PutMapping
	public ResponseEntity<Departamento> put(@Valid @RequestBody Departamento departamento) {
		return departamentoRepository.findById(departamento.getId())
				.map(resposta -> ResponseEntity.status(HttpStatus.OK).body(departamentoRepository.save(departamento)))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		Optional<Departamento> departamento = departamentoRepository.findById(id);

		if (departamento.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		departamentoRepository.deleteById(id);
	}

}
