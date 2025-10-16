package com.generation.rh_spring.controller;

import java.util.List;

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

import com.generation.rh_spring.model.Departamento;
import com.generation.rh_spring.service.DepartamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/departamentos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DepartamentoController {

    @Autowired
    private DepartamentoService departamentoService;

    @GetMapping
    public ResponseEntity<List<Departamento>> getAll() {
        return ResponseEntity.ok(departamentoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departamento> getById(@PathVariable Long id) {
        return departamentoService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/descricao/{descricao}")
    public ResponseEntity<List<Departamento>> getByDescricao(@PathVariable String descricao) {
        return ResponseEntity.ok(departamentoService.getByDescricao(descricao));
    }

    @PostMapping
    public ResponseEntity<Departamento> post(@Valid @RequestBody Departamento departamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departamentoService.post(departamento));
    }

    @PutMapping
    public ResponseEntity<Departamento> put(@Valid @RequestBody Departamento departamento) {
        return ResponseEntity.ok(departamentoService.put(departamento));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        departamentoService.delete(id);
    }
}
