package com.generation.rh_spring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.rh_spring.model.CalculoSalario;
import com.generation.rh_spring.model.Colaborador;
import com.generation.rh_spring.model.FaixaImposto;
import com.generation.rh_spring.model.Holerite;
import com.generation.rh_spring.repository.ColaboradorRepository;

@Service
public class CalcularSalarioService {

	@Autowired
	private ColaboradorRepository colaboradorRepository;

	private static final List<FaixaImposto> FAIXAS_INSS = List.of(new FaixaImposto(1412.00, 7.5, 0),
			new FaixaImposto(2666.68, 9.0, 21.18), new FaixaImposto(4000.03, 12.0, 101.18),
			new FaixaImposto(7786.02, 14.0, 181.18));

	private static final List<FaixaImposto> FAIXAS_IRRF = List.of(new FaixaImposto(2259.20, 0, 0),
			new FaixaImposto(2826.65, 7.5, 169.44), new FaixaImposto(3751.05, 15.0, 381.44),
			new FaixaImposto(4664.68, 22.5, 662.77), new FaixaImposto(Double.MAX_VALUE, 27.5, 896.00));

	public Holerite calcularSalario(Long id, CalculoSalario dadosSalario) {

		Optional<Colaborador> colaborador = colaboradorRepository.findById(id);

		if (colaborador.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		// Salário por hora
		BigDecimal salarioPorHora = colaborador.get().getSalario()
				.divide(new BigDecimal(colaborador.get().getHorasMensais()), RoundingMode.HALF_UP);

		// Valor da hora extra (50% adicional)
		BigDecimal valorHoraExtra = salarioPorHora.multiply(new BigDecimal("1.5"));

		// Valor total de horas extras
		BigDecimal valorTotalHorasExtras = valorHoraExtra.multiply(new BigDecimal(dadosSalario.getTotalHorasExtras()));

		// Salário base + horas extras (para cálculo dos impostos)
		BigDecimal salarioComHorasExtras = colaborador.get().getSalario().add(valorTotalHorasExtras);

		// Cálculo de descontos (Ex.: INSS e IRRF)
		BigDecimal descontoINSS = calcularINSS(salarioComHorasExtras);
		BigDecimal descontoIRRF = calcularIRRF(salarioComHorasExtras, descontoINSS, colaborador.get().getDependentes());

		// Total de descontos
		BigDecimal totalDescontos = descontoINSS.add(descontoIRRF).add(dadosSalario.getDescontos());

		// Cálculo do salário líquido
		BigDecimal salarioLiquido = salarioComHorasExtras.subtract(totalDescontos);

		// Retorno do objeto Holerite
		return new Holerite(colaborador.get().getSalario(), dadosSalario.getTotalHorasExtras(), valorHoraExtra,
				valorTotalHorasExtras, descontoINSS, descontoIRRF, totalDescontos, salarioLiquido);
	}

	private BigDecimal calcularINSS(BigDecimal salarioBruto) {
		System.out.println("\n=== CÁLCULO INSS ===");
		System.out.println("Salário Bruto: R$ " + salarioBruto);

		BigDecimal descontoTotal = BigDecimal.ZERO;
		BigDecimal salarioAcumulado = BigDecimal.ZERO;

		for (FaixaImposto faixa : FAIXAS_INSS) {
			System.out.println(
					"\nVerificando faixa: Limite R$ " + faixa.getLimite() + " | Alíquota " + faixa.getAliquota() + "%");

			BigDecimal limiteAnterior = salarioAcumulado;
			BigDecimal limiteFaixa = BigDecimal.valueOf(faixa.getLimite());

			if (salarioBruto.compareTo(limiteAnterior) > 0) {
				BigDecimal valorNaFaixa = salarioBruto.min(limiteFaixa).subtract(limiteAnterior);
				System.out.println("Valor na faixa: R$ " + valorNaFaixa);

				BigDecimal descontoFaixa = valorNaFaixa.multiply(BigDecimal.valueOf(faixa.getAliquota()))
						.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

				System.out.println("Desconto desta faixa: R$ " + descontoFaixa);

				descontoTotal = descontoTotal.add(descontoFaixa);
				salarioAcumulado = limiteFaixa;

				System.out.println("Desconto acumulado até agora: R$ " + descontoTotal);
			}
		}

		BigDecimal resultado = descontoTotal.setScale(2, RoundingMode.HALF_UP);
		System.out.println("\nResultado Final INSS: R$ " + resultado);
		System.out.println("=====================================");
		return resultado;
	}

	private BigDecimal calcularIRRF(BigDecimal salarioBruto, BigDecimal descontoINSS, int dependentes) {
		System.out.println("\n=== CÁLCULO IRRF ===");
		System.out.println("Salário Bruto: R$ " + salarioBruto);
		System.out.println("Desconto INSS: R$ " + descontoINSS);
		System.out.println("Número de Dependentes: " + dependentes);

		// Dedução por dependente 2024
		BigDecimal deducaoPorDependente = new BigDecimal("189.59");
		BigDecimal deducaoDependentes = deducaoPorDependente.multiply(BigDecimal.valueOf(dependentes));
		System.out.println("Dedução por Dependentes: R$ " + deducaoDependentes);

		// Base de cálculo do IRRF com 2 casas decimais
		BigDecimal baseDeCalculo = salarioBruto.subtract(descontoINSS).subtract(deducaoDependentes).setScale(2,
				RoundingMode.HALF_UP);
		System.out.println("Base de Cálculo: R$ " + baseDeCalculo);

		for (FaixaImposto faixa : FAIXAS_IRRF) {
			System.out.println("\nVerificando faixa: Limite R$ " + faixa.getLimite() + " | Alíquota "
					+ faixa.getAliquota() + "%" + " | Dedução R$ " + faixa.getDeducao());

			if (baseDeCalculo.compareTo(BigDecimal.valueOf(faixa.getLimite())) <= 0) {
				// 1. Calcula o valor da alíquota mantendo 2 casas decimais em cada operação
				BigDecimal aliquota = BigDecimal.valueOf(faixa.getAliquota()).divide(new BigDecimal("100"), 4,
						RoundingMode.HALF_UP);
				System.out.println("Alíquota aplicável (decimal): " + aliquota);

				BigDecimal valorAliquota = baseDeCalculo.multiply(aliquota).setScale(2, RoundingMode.HALF_UP);
				System.out.println("Valor da Alíquota: R$ " + valorAliquota);

				// 2. Subtrai a dedução
				BigDecimal deducao = BigDecimal.valueOf(faixa.getDeducao());
				System.out.println("Valor da Dedução: R$ " + deducao);

				BigDecimal descontoIR = valorAliquota.subtract(deducao);
				System.out.println("Desconto IR (antes do max): R$ " + descontoIR);

				// 3. Garante que o resultado não seja negativo e mantenha 2 casas decimais
				BigDecimal resultado = descontoIR.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
				System.out.println("Resultado Final IRRF: R$ " + resultado);
				System.out.println("=====================================");

				return resultado;
			}
		}

		System.out.println("Nenhuma faixa aplicável - Retornando ZERO");
		System.out.println("=====================================");
		return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	}
}