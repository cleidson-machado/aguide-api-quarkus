package br.com.aguideptbr.auth;

import java.io.IOException;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro de autenticação MOCK para testes.
 * Permite todas as requisições sem validação de token.
 * Ativo apenas quando o perfil "test" está habilitado.
 */
@Alternative
@Priority(1)
@ApplicationScoped
@Provider
@IfBuildProfile("test")
public class NoOpAuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Não faz nada - permite todas as requisições em ambiente de teste
    }
}
