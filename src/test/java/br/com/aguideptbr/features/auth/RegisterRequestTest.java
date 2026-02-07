package br.com.aguideptbr.features.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.aguideptbr.features.auth.dto.LoginResponse;
import br.com.aguideptbr.features.auth.dto.UserInfoDTO;

/**
 * Testes unitários para DTOs da feature Auth.
 * Valida construção e getters/setters dos DTOs.
 */
class AuthDTOTest {

    @Test
    void testUserInfoDTO_Construction() {
        UserInfoDTO userInfo = new UserInfoDTO(
                UUID.randomUUID(),
                "João",
                "Silva",
                "joao@example.com",
                "USER");

        assertNotNull(userInfo, "UserInfoDTO não deve ser nulo");
        assertNotNull(userInfo.getId(), "ID não deve ser nulo");
        assertEquals("João", userInfo.getName(), "Nome deve estar correto");
        assertEquals("Silva", userInfo.getSurname(), "Sobrenome deve estar correto");
        assertEquals("joao@example.com", userInfo.getEmail(), "Email deve estar correto");
        assertEquals("USER", userInfo.getRole(), "Role deve estar correto");
    }

    @Test
    void testLoginResponse_Construction() {
        UserInfoDTO userInfo = new UserInfoDTO(
                UUID.randomUUID(),
                "Maria",
                "Santos",
                "maria@example.com",
                "ADMIN");

        LoginResponse response = new LoginResponse(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                3600L,
                userInfo);

        assertNotNull(response, "LoginResponse não deve ser nulo");
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", response.getToken(), "Token deve estar correto");
        assertEquals("Bearer", response.getType(), "Tipo deve ser Bearer");
        assertEquals(3600L, response.getExpiresIn(), "Expiration deve estar correto");
        assertNotNull(response.getUser(), "User info não deve ser nulo");
        assertEquals("Maria", response.getUser().getName(), "Nome do usuário deve estar correto");
    }

    @Test
    void testLoginResponse_Setters() {
        LoginResponse response = new LoginResponse(null, null, null);

        response.setToken("novo-token");
        response.setExpiresIn(7200L);

        assertEquals("novo-token", response.getToken(), "Token atualizado deve estar correto");
        assertEquals(7200L, response.getExpiresIn(), "Expiration atualizado deve estar correto");
    }
}
