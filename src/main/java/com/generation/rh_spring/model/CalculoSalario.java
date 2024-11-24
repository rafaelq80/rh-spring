package com.generation.rh_spring.model;

import java.math.BigDecimal;

public class CalculoSalario {

    private int totalHorasExtras;
    private BigDecimal descontos;

    public CalculoSalario(int totalHorasExtras, BigDecimal descontos) {
		super();
		this.totalHorasExtras = totalHorasExtras;
		this.descontos = descontos;
	}

	public int getTotalHorasExtras() {
        return totalHorasExtras;
    }

    public void setTotalHorasExtras(int totalHorasExtras) {
        this.totalHorasExtras = totalHorasExtras;
    }

    public BigDecimal getDescontos() {
        return descontos;
    }

    public void setDescontos(BigDecimal descontos) {
        this.descontos = descontos;
    }
}

