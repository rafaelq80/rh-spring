package com.generation.rh_spring.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tb_colaboradores")
public class Colaborador {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "O Nome é Obrigatório!")
	private String nome;

	@NotBlank(message = "O E-mail é Obrigatório!")
	@Email(message = "Informe um e-mail válido!")
	private String email;
	
	@NotBlank(message = "O Cargo é Obrigatório!")
	private String cargo;
			
	@NotNull(message = "O Salário é obrigatório!")
	@Positive(message = "O Salário deve ser um valor positivo")
	@Digits(integer = 10, fraction = 2)
	private BigDecimal salario;

	@NotNull(message = "O número de Horas Mensais é obrigatório!")
	@Positive(message = "O número de Horas Mensais deve ser um numero positivo")
	private int horasMensais;

	@Min(value = 0, message = "O Número de Dependentes deve ser um numero positivo ou zero")
	@NotNull(message = "O Número de Dependentes é obrigatório!")
	@PositiveOrZero(message = "O número de Dependentes deve ser um numero positivo ou zero")
	private int dependentes = 0;
	
	@Size(max = 5000, message = "O link da Foto deve ter no máximo 5000 caracteres")
	private String foto;
	
	@ManyToOne
	@JsonIgnoreProperties("colaborador")
	private Departamento departamento;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public BigDecimal getSalario() {
		return salario;
	}

	public void setSalario(BigDecimal salario) {
		this.salario = salario;
	}

	public int getHorasMensais() {
		return horasMensais;
	}

	public void setHorasMensais(int horasMensais) {
		this.horasMensais = horasMensais;
	}

	public int getDependentes() {
		return dependentes;
	}

	public void setDependentes(int dependentes) {
		this.dependentes = dependentes;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public Departamento getDepartamento() {
		return departamento;
	}

	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}
	
}
