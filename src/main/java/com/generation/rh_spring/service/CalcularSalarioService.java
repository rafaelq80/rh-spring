package com.generation.rh_spring.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.rh_spring.model.Colaborador;
import com.generation.rh_spring.records.CalculoSalario;
import com.generation.rh_spring.records.FaixaImposto;
import com.generation.rh_spring.records.Holerite;
import com.generation.rh_spring.repository.ColaboradorRepository;

@Service
public class CalcularSalarioService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // Faixas INSS 2025
    private static final List<FaixaImposto> FAIXAS_INSS = List.of(
        new FaixaImposto(1518.00, 7.5, 0),
        new FaixaImposto(2793.87, 9.0, 28.80),
        new FaixaImposto(4190.82, 12.0, 135.57),
        new FaixaImposto(8381.66, 14.0, 259.17)
    );

    // Faixas IRRF 2025
    private static final List<FaixaImposto> FAIXAS_IRRF = List.of(
        new FaixaImposto(2352.00, 0, 0),
        new FaixaImposto(2826.65, 7.5, 176.15),
        new FaixaImposto(3751.05, 15.0, 404.78),
        new FaixaImposto(4664.68, 22.5, 694.54),
        new FaixaImposto(Double.MAX_VALUE, 27.5, 917.24)
    );

    private static final BigDecimal DEDUCAO_DEPENDENTE = new BigDecimal("175.00");
    private static final BigDecimal PERCENTUAL_HORA_EXTRA = new BigDecimal("1.5");
    private static final int SCALE = 2;

    public Holerite calcularSalario(Long id, CalculoSalario dadosSalario) {
        Colaborador colaborador = buscarColaborador(id);

        BigDecimal salarioPorHora = calcularSalarioPorHora(colaborador);
        BigDecimal valorHoraExtra = salarioPorHora.multiply(PERCENTUAL_HORA_EXTRA);
        BigDecimal valorTotalHorasExtras = valorHoraExtra.multiply(new BigDecimal(dadosSalario.totalHorasExtras()));
        BigDecimal salarioComHorasExtras = colaborador.getSalario().add(valorTotalHorasExtras);

        BigDecimal descontoINSS = calcularINSS(salarioComHorasExtras);
        BigDecimal descontoIRRF = calcularIRRF(salarioComHorasExtras, descontoINSS, colaborador.getDependentes());

        BigDecimal totalDescontos = descontoINSS.add(descontoIRRF).add(dadosSalario.descontos());
        BigDecimal salarioLiquido = salarioComHorasExtras.subtract(totalDescontos);

        return new Holerite(
            colaborador.getSalario(),
            dadosSalario.totalHorasExtras(),
            valorHoraExtra,
            valorTotalHorasExtras,
            descontoINSS,
            descontoIRRF,
            totalDescontos,
            salarioLiquido
        );
    }

    private Colaborador buscarColaborador(Long id) {
        Optional<Colaborador> colaborador = colaboradorRepository.findById(id);
        if (colaborador.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Colaborador não encontrado");
        }
        return colaborador.get();
    }

    private BigDecimal calcularSalarioPorHora(Colaborador colaborador) {
        return colaborador.getSalario()
            .divide(new BigDecimal(colaborador.getHorasMensais()), SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularINSS(BigDecimal salarioBruto) {
        return buscarFaixaAplicavel(salarioBruto, FAIXAS_INSS);
    }

    private BigDecimal calcularIRRF(BigDecimal salarioBruto, BigDecimal descontoINSS, int dependentes) {
        BigDecimal deducaoDependentes = DEDUCAO_DEPENDENTE.multiply(BigDecimal.valueOf(dependentes));
        BigDecimal baseDeCalculo = salarioBruto
            .subtract(descontoINSS)
            .subtract(deducaoDependentes)
            .setScale(SCALE, RoundingMode.HALF_UP);

        return buscarFaixaAplicavel(baseDeCalculo, FAIXAS_IRRF);
    }

    private BigDecimal buscarFaixaAplicavel(BigDecimal valor, List<FaixaImposto> faixas) {
        for (FaixaImposto faixa : faixas) {
            if (valor.compareTo(BigDecimal.valueOf(faixa.limite())) <= 0) {
                return aplicarFaixa(valor, faixa);
            }
        }
        return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal aplicarFaixa(BigDecimal base, FaixaImposto faixa) {
        BigDecimal aliquota = BigDecimal.valueOf(faixa.aliquota())
            .divide(new BigDecimal("100"), SCALE, RoundingMode.HALF_UP);
        
        BigDecimal valorAliquota = base.multiply(aliquota)
            .setScale(SCALE, RoundingMode.HALF_UP);
        
        BigDecimal desconto = valorAliquota.subtract(BigDecimal.valueOf(faixa.deducao()));
        
        return desconto.max(BigDecimal.ZERO)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }
}