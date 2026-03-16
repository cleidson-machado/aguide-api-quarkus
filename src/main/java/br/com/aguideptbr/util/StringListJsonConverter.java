package br.com.aguideptbr.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter JPA para serializar List<String> como JSON no banco de dados.
 * Útil para campos como contentFormats e currentInfoSources.
 *
 * USO:
 *
 * @Convert(converter = StringListJsonConverter.class)
 *                    public List<String> contentFormats;
 *
 *                    CONVERSÃO:
 *                    - List: ["VLOG", "TUTORIAL", "SHORTS"]
 *                    - JSON: '["VLOG", "TUTORIAL", "SHORTS"]'
 *
 *                    BENEFÍCIOS:
 *                    - ✅ Conversão automática List ↔ JSON
 *                    - ✅ Validação de formato JSON pelo ObjectMapper
 *                    - ✅ Type-safe (List<String> no código Java)
 *                    - ✅ Facilita manipulação (add, remove, contains)
 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converte List<String> para JSON (persistir no banco).
     *
     * @param attribute Lista de strings
     * @return JSON string (ex: '["A", "B", "C"]')
     */
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Converte JSON para List<String> (carregar do banco).
     *
     * @param dbData JSON string do banco de dados
     * @return Lista de strings
     */
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || dbData.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to list: " + e.getMessage(), e);
        }
    }
}
