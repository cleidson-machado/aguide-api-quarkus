package br.com.aguideptbr.features.userposition;

/**
 * Enum representando o potencial de conversão de um usuário.
 *
 * CONVERSÃO = Likelihood de pagar por serviços (consultoria, premium,
 * contatos).
 *
 * CRITÉRIOS DE CLASSIFICAÇÃO:
 * - VERY_HIGH: totalScore >= 80 AND hasPhones = true AND totalMessagesSent >=
 * 10
 * - HIGH: totalScore >= 60 AND (hasPhones = true OR totalMessagesSent >= 5)
 * - MEDIUM: totalScore >= 40
 * - LOW: totalScore >= 20
 * - VERY_LOW: totalScore < 20
 *
 * USO:
 * - Priorização de leads (time de vendas foca em VERY_HIGH e HIGH)
 * - Automação de marketing (enviar ofertas personalizadas)
 * - Previsão de receita (estimar quantos converterão)
 * - A/B testing (testar diferentes abordagens por potencial)
 */
public enum ConversionPotential {
    /**
     * Potencial MUITO BAIXO de conversão.
     * Usuário pouco engajado, sem telefone, sem interações.
     * Foco: Engajamento básico (não vender ainda).
     */
    VERY_LOW,

    /**
     * Potencial BAIXO de conversão.
     * Usuário com algum engajamento, mas faltam indicadores-chave.
     * Foco: Educação e engajamento (não vender ainda).
     */
    LOW,

    /**
     * Potencial MÉDIO de conversão.
     * Usuário engajado, pode converter com abordagem certa.
     * Foco: Ofertas de entrada (trials, descontos).
     */
    MEDIUM,

    /**
     * Potencial ALTO de conversão.
     * Usuário muito engajado, tem telefone OU mensagens.
     * Foco: Contato direto, ofertas personalizadas.
     */
    HIGH,

    /**
     * Potencial MUITO ALTO de conversão.
     * Usuário super engajado, tem telefone E muitas mensagens.
     * Foco: Prioridade máxima, contato imediato do time de vendas.
     */
    VERY_HIGH
}
