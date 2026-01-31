package br.com.aguideptbr;

import java.util.Collections;
import java.util.Set;

import br.com.aguideptbr.auth.NoOpAuthenticationFilter;
import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Perfil de teste que desabilita o AuthenticationFilter.
 * Aplicado em todos os testes de integração
 * com @TestProfile(NoAuthTestProfile.class).
 */
public class NoAuthTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        // Ativa o filtro NoOp que permite todas as requisições
        return Collections.singleton(NoOpAuthenticationFilter.class);
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
