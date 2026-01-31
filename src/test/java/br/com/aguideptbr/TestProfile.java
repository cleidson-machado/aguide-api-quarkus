package br.com.aguideptbr;

import java.util.Collections;
import java.util.Set;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Perfil de teste que desabilita o AuthenticationFilter.
 * Aplicado em todos os testes de integração
 * com @TestProfile(TestProfile.class).
 */
public class TestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Collections.emptySet();
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }

    @Override
    public Set<String> tags() {
        return Collections.singleton("test");
    }
}
