package br.com.aguideptbr.features.userchoice.enuns;

import br.com.aguideptbr.features.userchoice.UserChoiceModel;

/**
 * Formatos de conteúdo produzidos pelo criador (CREATOR).
 *
 * CONTEXTO:
 * Campo utilizado no onboarding para capturar os tipos de vídeos/conteúdos
 * que o criador produz no YouTube. Permite seleção múltipla.
 *
 * USO:
 * - Armazenado como JSON array no campo 'contentFormats' (List<String>)
 * - Conversão automática via StringListJsonConverter
 * - Formato no banco: '["VLOG", "TUTORIAL", "INTERVIEW"]'
 *
 * VALIDAÇÃO:
 * - Flutter deve enviar valores exatamente como definidos (uppercase)
 * - Backend deve validar se valores recebidos correspondem ao enum
 * - Mínimo 1 formato deve ser selecionado (validado em validateCreatorFields)
 *
 * IMPORTANTE:
 * Este enum foi criado para garantir type safety e validação de valores
 * permitidos. Anteriormente, o campo aceitava strings arbitrárias sem
 * validação, permitindo valores inconsistentes como "vlog" (minúsculo)
 * ou "VLOGS" (plural).
 *
 * @see UserChoiceModel#contentFormats
 * @since 1.0.0 (criado em 15/03/2026 - Parte 5 da análise de mapeamento)
 */
public enum ContentFormat {
    /**
     * Vlog - vídeos no estilo diary/diário pessoal.
     * Exemplo: "Um dia na minha vida em Portugal"
     */
    VLOG,

    /**
     * Tutorial - conteúdo educacional passo a passo.
     * Exemplo: "Como aplicar para o visto D7: passo a passo completo"
     */
    TUTORIAL,

    /**
     * Entrevista - conversas com especialistas ou pessoas relevantes.
     * Exemplo: "Entrevista com advogado de imigração português"
     */
    INTERVIEW,

    /**
     * Análise de notícias - comentários sobre eventos atuais.
     * Exemplo: "Mudanças no Golden Visa 2026: o que você precisa saber"
     */
    NEWS_ANALYSIS,

    /**
     * Shorts - vídeos curtos (formato YouTube Shorts, TikTok, Reels).
     * Exemplo: "3 documentos essenciais para o visto D7 #shorts"
     */
    SHORTS,

    /**
     * Outros formatos não listados acima.
     * Usar quando nenhuma das opções anteriores se aplica.
     */
    OTHER;

    /**
     * Converte lista de strings para lista de enums.
     *
     * @param values Lista de valores como strings (ex: ["VLOG", "TUTORIAL"])
     * @return Lista de enums correspondentes
     * @throws IllegalArgumentException se algum valor não corresponder a um enum
     *                                  válido
     */
    public static java.util.List<ContentFormat> fromStringList(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return values.stream()
                .map(String::toUpperCase) // Normaliza para uppercase
                .map(ContentFormat::valueOf)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Converte lista de enums para lista de strings.
     *
     * @param formats Lista de enums (ex: [VLOG, TUTORIAL])
     * @return Lista de strings (ex: ["VLOG", "TUTORIAL"])
     */
    public static java.util.List<String> toStringList(java.util.List<ContentFormat> formats) {
        if (formats == null || formats.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return formats.stream()
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
