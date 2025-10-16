package com.generation.rh_spring.records;

import java.math.BigDecimal;

public record Holerite(
	    BigDecimal salario,
	    int horasExtras,
	    BigDecimal valorHoraExtra,
	    BigDecimal valorTotalHorasExtras,
	    BigDecimal inss,
	    BigDecimal irrf,
	    BigDecimal totalDescontos,
	    BigDecimal salarioLiquido
) {}