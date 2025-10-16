package com.generation.rh_spring.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tb_departamentos")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição é Obrigatória!")
    private String descricao;

    @Size(max = 5000, message = "O Link do ícone deve ter no máximo 5000 caracteres")
    private String icone;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "departamento", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = "departamento", allowSetters = true)
    private List<Colaborador> colaborador;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	public List<Colaborador> getColaborador() {
		return colaborador;
	}

	public void setColaborador(List<Colaborador> colaborador) {
		this.colaborador = colaborador;
	}

}
