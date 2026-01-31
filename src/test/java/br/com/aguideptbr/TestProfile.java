package br.com.aguideptbr;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Profile de teste que desabilita o AuthenticationFilter para permitir testes
 * sem token.
 * Usado em conjunto com @TestProfile(TestProfile.class) nos testes.
 */
public class TestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                // Desabilita o AuthenticationFilter em testes
                "quarkus.arc.exclude-types", "br.com.aguideptbr.auth.AuthenticationFilter");
    }
}
