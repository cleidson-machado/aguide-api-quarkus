package br.com.aguideptbr.features.userchoice.enuns;

import br.com.aguideptbr.features.userchoice.UserChoiceModel;

/**
 * Fontes de informação atuais do consumidor (CONSUMER).
 *
 * CONTEXTO:
 * Campo utilizado no onboarding para capturar onde o consumidor costuma
 * buscar informações sobre o tema (ex: imigração para Portugal).
 * Permite seleção múltipla.
 *
 * USO:
 * - Armazenado como JSON array no campo 'currentInfoSources' (List<String>)
 * - Conversão automática via StringListJsonConverter
 * - Formato no banco: '["YOUTUBE", "BLOGS", "FORUMS"]'
 *
 * VALIDAÇÃO:
 * - Flutter deve enviar valores exatamente como definidos (uppercase)
 * - Backend deve validar se valores recebidos correspondem ao enum
 * - Mínimo 1 fonte deve ser selecionada (validado em validateConsumerFields)
 *
 * IMPORTANTE:
 * Este enum foi criado para garantir type safety e validação de valores
 * permitidos. Anteriormente, o campo aceitava strings arbitrárias sem
 * validação, permitindo valores inconsistentes como "youtube" (minúsculo)
 * ou "YOUTUBES" (plural).
 *
 * ESCALABILIDADE:
 * 5/6 valores (83%) são genéricos e aplicáveis a qualquer nicho de conteúdo.
 * Apenas LAWYERS_CONSULTANTS é mais específico de imigração, mas pode ser
 * renomeado para PROFESSIONALS em nichos futuros.
 *
 * @see UserChoiceModel#currentInfoSources
 * @since 1.0.0 (criado em 15/03/2026 - Parte 5 da análise de mapeamento)
 */
public enum InfoSource {
    /**
     * YouTube - vídeos e canais sobre o tema.
     * Exemplo: Canais de imigração, vlogs de portugueses
     */
    YOUTUBE,

    /**
     * Grupos em WhatsApp ou Telegram.
     * Exemplo: Grupos de brasileiros em Portugal, comunidades de imigrantes
     */
    WHATSAPP_TELEGRAM,

    /**
     * Blogs e sites especializados.
     * Exemplo: Blogs de imigração, portais de notícias
     */
    BLOGS,

    /**
     * Advogados, consultores ou profissionais especializados.
     * Exemplo: Escritórios de advocacia, consultorias de imigração
     *
     * NOTA: Pode ser renomeado para PROFESSIONALS em nichos não-imigração.
     */
    LAWYERS_CONSULTANTS,

    /**
     * Fóruns e comunidades online.
     * Exemplo: Reddit, fóruns de expatriados
     */
    FORUMS,

    /**
     * Redes sociais (Instagram, Facebook, TikTok, etc.).
     * Exemplo: Posts, stories, reels sobre imigração
     */
    SOCIAL_MEDIA;

    /**
     * Converte lista de strings para lista de enums.
     *
     * @param values Lista de valores como strings (ex: ["YOUTUBE", "BLOGS"])
     * @return Lista de enums correspondentes
     * @throws IllegalArgumentException se algum valor não corresponder a um enum
     *                                  válido
     */
    public static java.util.List<InfoSource> fromStringList(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return values.stream()
                .map(String::toUpperCase) // Normaliza para uppercase
                .map(InfoSource::valueOf)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Converte lista de enums para lista de strings.
     *
     * @param sources Lista de enums (ex: [YOUTUBE, BLOGS])
     * @return Lista de strings (ex: ["YOUTUBE", "BLOGS"])
     */
    public static java.util.List<String> toStringList(java.util.List<InfoSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return sources.stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Valida se uma lista de strings contém apenas valores permitidos.
     *
     * @param values Lista de valores a validar
     * @return true se todos os valores são válidos, false caso contrário
     */
    public static boolean isValidStringList(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }

        try {
            fromStringList(values);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
