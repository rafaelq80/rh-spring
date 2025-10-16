package com.generation.rh_spring.records;

import java.math.BigDecimal;

public record CalculoSalario(
	    int totalHorasExtras,
	    BigDecimal descontos
) {}