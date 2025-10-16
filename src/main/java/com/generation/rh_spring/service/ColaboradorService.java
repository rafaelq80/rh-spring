package com.generation.rh_spring.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.rh_spring.model.Colaborador;
import com.generation.rh_spring.repository.ColaboradorRepository;
import com.generation.rh_spring.repository.DepartamentoRepository;

import jakarta.validation.Valid;

@Service
public class ColaboradorService {

	@Autowired
	private ColaboradorRepository colaboradorRepository;

	@Autowired
	private DepartamentoRepository departamentoRepository;

	public List<Colaborador> getAll() {
		return colaboradorRepository.findAll();
	}

	public ResponseEntity<Colaborador> getById(Long id) {
		return colaboradorRepository.findById(id)
				.map(resposta -> ResponseEntity.ok(resposta))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	public List<Colaborador> getByNome(String nome) {
		return colaboradorRepository.findAllByNomeContainingIgnoreCase(nome);
	}

	public ResponseEntity<Colaborador> post(@Valid Colaborador colaborador) {
		if (departamentoRepository.existsById(colaborador.getDepartamento().getId()))
			return ResponseEntity.status(HttpStatus.CREATED).body(colaboradorRepository.save(colaborador));

		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento não existe!", null);
	}

	public ResponseEntity<Colaborador> put(@Valid Colaborador colaborador) {
		if (colaboradorRepository.existsById(colaborador.getId())) {

			if (departamentoRepository.existsById(colaborador.getDepartamento().getId()))
				return ResponseEntity.status(HttpStatus.OK).body(colaboradorRepository.save(colaborador));

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento não existe!", null);
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	public void delete(Long id) {
		Optional<Colaborador> colaborador = colaboradorRepository.findById(id);

		if (colaborador.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		colaboradorRepository.deleteById(id);
	}
}
