package com.generation.rh_spring.model;

import java.math.BigDecimal;

public class Holerite {

    private BigDecimal salario;
    private int horasExtras;
    private BigDecimal valorHoraExtra;
    private BigDecimal valorTotalHorasExtras;
    private BigDecimal inss;
    private BigDecimal irrf;
    private BigDecimal totalDescontos;
    private BigDecimal salarioLiquido;


    public Holerite(
            BigDecimal salario, int horasExtras, BigDecimal valorHoraExtra, BigDecimal valorTotalHorasExtras,
            BigDecimal inss, BigDecimal irrf, BigDecimal totalDescontos, BigDecimal salarioLiquido
    ) {
        this.salario = salario;
        this.horasExtras = horasExtras;
        this.valorHoraExtra = valorHoraExtra;
        this.valorTotalHorasExtras = valorTotalHorasExtras;
        this.inss = inss;
        this.irrf = irrf;
        this.totalDescontos = totalDescontos;
        this.salarioLiquido = salarioLiquido;
    }


	public BigDecimal getSalario() {
		return salario;
	}


	public void setSalario(BigDecimal salario) {
		this.salario = salario;
	}


	public int getHorasExtras() {
		return horasExtras;
	}


	public void setHorasExtras(int horasExtras) {
		this.horasExtras = horasExtras;
	}


	public BigDecimal getValorHoraExtra() {
		return valorHoraExtra;
	}


	public void setValorHoraExtra(BigDecimal valorHoraExtra) {
		this.valorHoraExtra = valorHoraExtra;
	}


	public BigDecimal getValorTotalHorasExtras() {
		return valorTotalHorasExtras;
	}


	public void setValorTotalHorasExtras(BigDecimal valorTotalHorasExtras) {
		this.valorTotalHorasExtras = valorTotalHorasExtras;
	}


	public BigDecimal getInss() {
		return inss;
	}


	public void setInss(BigDecimal inss) {
		this.inss = inss;
	}


	public BigDecimal getIrrf() {
		return irrf;
	}


	public void setIrrf(BigDecimal irrf) {
		this.irrf = irrf;
	}


	public BigDecimal getTotalDescontos() {
		return totalDescontos;
	}


	public void setTotalDescontos(BigDecimal totalDescontos) {
		this.totalDescontos = totalDescontos;
	}


	public BigDecimal getSalarioLiquido() {
		return salarioLiquido;
	}


	public void setSalarioLiquido(BigDecimal salarioLiquido) {
		this.salarioLiquido = salarioLiquido;
	}

}
