package br.com.aguideptbr.features.userchoice.enuns;

/**
 * Enum representando a intenção comercial do criador na plataforma (CREATOR).
 *
 * VALORES:
 * - BRAND_PARTNERSHIP: Parcerias com marcas
 * - SELL_OWN_SERVICES: Vender serviços próprios (consultoria, mentorias)
 * - AUDIENCE_GROWTH: Crescer audiência (sem foco comercial imediato)
 * - CONSULTING: Oferecer consultoria especializada
 * - OTHER: Outras intenções
 *
 * USO:
 * - Identificar criadores com potencial de monetização na plataforma
 * - Direcionar features específicas (marketplace, parcerias)
 * - Priorizar criadores que vendem serviços (alto potencial de conversão)
 */
public enum CommercialIntent {
    /**
     * Busca parcerias com marcas.
     * Criador focado em publicidade e patrocínios.
     */
    BRAND_PARTNERSHIP,

    /**
     * Vender serviços/produtos próprios.
     * Criador empreendedor (cursos, e-books, mentorias).
     */
    SELL_OWN_SERVICES,

    /**
     * Crescer audiência.
     * Criador focado em engajamento (monetização futura).
     */
    AUDIENCE_GROWTH,

    /**
     * Oferecer consultoria especializada.
     * Criador especialista (vistos, imigração, legal).
     */
    CONSULTING,

    /**
     * Outras intenções comerciais.
     */
    OTHER
}
