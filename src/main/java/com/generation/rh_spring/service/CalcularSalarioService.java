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

/**
 * Service responsável pelo cálculo de salários, descontos de impostos e geração de holerites.
 * Calcula INSS, IRRF e valor total de horas extras conforme as faixas de 2025.
 */
@Service
public class CalcularSalarioService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // ========== CONSTANTES - Faixas de Impostos 2025 ==========
    
    /**
     * Tabela INSS 2025 com limites de salário, alíquota e deução.
     * Estrutura: (limite do salário, alíquota %, deução base)
     */
    private static final List<FaixaImposto> FAIXAS_INSS = List.of(
        new FaixaImposto(1518.00, 7.5, 0),
        new FaixaImposto(2793.87, 9.0, 28.80),
        new FaixaImposto(4190.82, 12.0, 135.57),
        new FaixaImposto(8381.66, 14.0, 259.17)
    );

    /**
     * Tabela IRRF 2025 com limites de salário, alíquota e deução.
     * Estrutura: (limite do salário, alíquota %, deução base)
     */
    private static final List<FaixaImposto> FAIXAS_IRRF = List.of(
        new FaixaImposto(2352.00, 0, 0),
        new FaixaImposto(2826.65, 7.5, 176.15),
        new FaixaImposto(3751.05, 15.0, 404.78),
        new FaixaImposto(4664.68, 22.5, 694.54),
        new FaixaImposto(Double.MAX_VALUE, 27.5, 917.24)
    );

    // Valor da deução por dependente no IRRF (2025)
    private static final BigDecimal DEDUCAO_DEPENDENTE = new BigDecimal("175.00");
    
    // Multiplicador para hora extra (50% de acréscimo = 1.5x)
    private static final BigDecimal PERCENTUAL_HORA_EXTRA = new BigDecimal("1.5");
    
    // Casas decimais para cálculos (2 = centavos)
    private static final int SCALE = 2;

    /**
     * Método principal que calcula o holerite completo de um colaborador.
     * 
     * @param id - ID do colaborador
     * @param dadosSalario - objeto com total de horas extras e descontos adicionais
     * @return Holerite - objeto com todos os valores calculados
     * @throws ResponseStatusException - se colaborador não for encontrado
     */
    public Holerite calcularSalario(Long id, CalculoSalario dadosSalario) {
        // 1. Busca o colaborador no banco de dados
        Colaborador colaborador = buscarColaborador(id);

        // 2. Calcula valores básicos
        BigDecimal salarioPorHora = calcularSalarioPorHora(colaborador);
        BigDecimal valorHoraExtra = salarioPorHora.multiply(PERCENTUAL_HORA_EXTRA);
        BigDecimal valorTotalHorasExtras = valorHoraExtra.multiply(new BigDecimal(dadosSalario.totalHorasExtras()));
        
        // 3. Soma salário com horas extras (necessário para cálculo dos impostos)
        BigDecimal salarioComHorasExtras = colaborador.getSalario().add(valorTotalHorasExtras);

        // 4. Calcula os descontos (INSS e IRRF)
        BigDecimal descontoINSS = calcularINSS(salarioComHorasExtras);
        BigDecimal descontoIRRF = calcularIRRF(salarioComHorasExtras, descontoINSS, colaborador.getDependentes());

        // 5. Soma todos os descontos
        BigDecimal totalDescontos = descontoINSS.add(descontoIRRF).add(dadosSalario.descontos());
        
        // 6. Calcula salário líquido (bruto - descontos)
        BigDecimal salarioLiquido = salarioComHorasExtras.subtract(totalDescontos);

        // 7. Retorna o holerite com todos os valores calculados
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

    /**
     * Busca um colaborador pelo ID no banco de dados.
     * 
     * @param id - ID do colaborador
     * @return Colaborador encontrado
     * @throws ResponseStatusException - HTTP 404 se não encontrar
     */
    private Colaborador buscarColaborador(Long id) {
        Optional<Colaborador> colaborador = colaboradorRepository.findById(id);
        if (colaborador.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Colaborador não encontrado");
        }
        return colaborador.get();
    }

    /**
     * Calcula o salário por hora dividindo o salário mensal pelas horas mensais.
     * 
     * @param colaborador - objeto do colaborador
     * @return BigDecimal - valor do salário por hora
     */
    private BigDecimal calcularSalarioPorHora(Colaborador colaborador) {
        return colaborador.getSalario()
            .divide(new BigDecimal(colaborador.getHorasMensais()), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o desconto de INSS aplicando a tabela progressiva.
     * 
     * @param salarioBruto - salário bruto (salário + horas extras)
     * @return BigDecimal - valor do desconto INSS
     */
    private BigDecimal calcularINSS(BigDecimal salarioBruto) {
        return buscarFaixaAplicavel(salarioBruto, FAIXAS_INSS);
    }

    /**
     * Calcula o desconto de IRRF (Imposto de Renda).
     * Deduz o INSS e as deduções por dependentes da base de cálculo.
     * 
     * @param salarioBruto - salário bruto
     * @param descontoINSS - valor já calculado de INSS
     * @param dependentes - número de dependentes (0 a N)
     * @return BigDecimal - valor do desconto IRRF
     */
    private BigDecimal calcularIRRF(BigDecimal salarioBruto, BigDecimal descontoINSS, int dependentes) {
        // Calcula a dedução total de dependentes
        BigDecimal deducaoDependentes = DEDUCAO_DEPENDENTE.multiply(BigDecimal.valueOf(dependentes));
        
        // Base de cálculo = Salário - INSS - Deduções dependentes
        BigDecimal baseDeCalculo = salarioBruto
            .subtract(descontoINSS)
            .subtract(deducaoDependentes)
            .setScale(SCALE, RoundingMode.HALF_UP);

        return buscarFaixaAplicavel(baseDeCalculo, FAIXAS_IRRF);
    }

    /**
     * Busca a faixa de imposto aplicável ao valor do salário.
     * Percorre a lista de faixas até encontrar aquela em que o valor se encaixa.
     * 
     * @param valor - valor do salário a ser enquadrado
     * @param faixas - lista de faixas de impostos
     * @return BigDecimal - valor do desconto aplicado
     */
    private BigDecimal buscarFaixaAplicavel(BigDecimal valor, List<FaixaImposto> faixas) {
        for (FaixaImposto faixa : faixas) {
            // Se o valor é menor ou igual ao limite da faixa, aplica esta faixa
            if (valor.compareTo(BigDecimal.valueOf(faixa.limite())) <= 0) {
                return aplicarFaixa(valor, faixa);
            }
        }
        // Se não enquadrar em nenhuma faixa (muito raro), retorna ZERO
        return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Aplica uma faixa de imposto ao valor, calculando:
     * Desconto = (valor × alíquota) - deução
     * Se resultado negativo, retorna ZERO (não pode ser negativo).
     * 
     * @param base - valor da base de cálculo
     * @param faixa - faixa com alíquota e deução
     * @return BigDecimal - desconto final
     */
    private BigDecimal aplicarFaixa(BigDecimal base, FaixaImposto faixa) {
        // 1. Converte alíquota de percentual para decimal (ex: 7.5% = 0.075)
        BigDecimal aliquota = BigDecimal.valueOf(faixa.aliquota())
            .divide(new BigDecimal("100"), SCALE, RoundingMode.HALF_UP);
        
        // 2. Multiplica base × alíquota
        BigDecimal valorAliquota = base.multiply(aliquota)
            .setScale(SCALE, RoundingMode.HALF_UP);
        
        // 3. Subtrai a deução da faixa
        BigDecimal desconto = valorAliquota.subtract(BigDecimal.valueOf(faixa.deducao()));
        
        // 4. Se negativo, retorna ZERO; senão, retorna o desconto
        return desconto.max(BigDecimal.ZERO)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }
}