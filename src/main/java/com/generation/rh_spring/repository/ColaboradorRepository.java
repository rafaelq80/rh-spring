﻿package com.generation.rh_spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.generation.rh_spring.model.Colaborador;


public interface ColaboradorRepository extends JpaRepository<Colaborador, Long>{
	
    List<Colaborador> findAllByNomeContainingIgnoreCase(@Param("nome") String nome);
    
}