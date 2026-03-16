package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando o tipo de visto de interesse do consumidor (CONSUMER).
 *
 * VALORES:
 * - D7_PASSIVE_INCOME: Visto D7 (renda passiva)
 * - D8_DIGITAL_NOMAD: Visto D8 (nômade digital)
 * - GOLDEN_VISA: Golden Visa
 * - WORK_VISA: Visto de trabalho
 * - STUDY_VISA: Visto de estudos
 * - FAMILY_REUNIFICATION: Reunificação familiar
 * - NOT_SURE_YET: Ainda não decidiu
 *
 * USO:
 * - Segmentar consumidores por tipo de visto
 * - Recomendar conteúdo especializado por categoria de visto
 * - Identificar perfis de alto valor (D7, D8, Golden Visa)
 */
public enum VisaTypeInterest {
    /**
     * Visto D7 (renda passiva).
     * Aposentados, investidores, renda de aluguel/dividendos.
     */
    D7_PASSIVE_INCOME,

    /**
     * Visto D8 (nômade digital).
     * Freelancers, trabalhadores remotos, tech professionals.
     */
    D8_DIGITAL_NOMAD,

    /**
     * Golden Visa.
     * Investidores de alto patrimônio, investimento imobiliário/fundos.
     */
    GOLDEN_VISA,

    /**
     * Visto de trabalho.
     * Profissionais contratados por empresas portuguesas.
     */
    WORK_VISA,

    /**
     * Visto de estudos.
     * Estudantes universitários, intercâmbio acadêmico.
     */
    STUDY_VISA,

    /**
     * Reunificação familiar.
     * Familiares de residentes legais em Portugal.
     */
    FAMILY_REUNIFICATION,

    /**
     * Ainda não decidiu.
     * Usuário explorando opções.
     */
    NOT_SURE_YET
}
