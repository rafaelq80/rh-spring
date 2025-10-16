package com.generation.rh_spring.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.rh_spring.model.Departamento;
import com.generation.rh_spring.repository.DepartamentoRepository;

import jakarta.validation.Valid;

@Service
public class DepartamentoService {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    public List<Departamento> getAll() {
        return departamentoRepository.findAll();
    }

    public Optional<Departamento> getById(Long id) {
        return departamentoRepository.findById(id);
    }

    public List<Departamento> getByDescricao(String descricao) {
        return departamentoRepository.findAllByDescricaoContainingIgnoreCase(descricao);
    }

    public Departamento post(@Valid Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public Departamento put(@Valid Departamento departamento) {
        return departamentoRepository.findById(departamento.getId())
                .map(resposta -> departamentoRepository.save(departamento))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento não encontrado"));
    }

    public void delete(Long id) {
        Optional<Departamento> departamento = departamentoRepository.findById(id);

        if (departamento.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento não encontrado");

        departamentoRepository.deleteById(id);
    }
}
