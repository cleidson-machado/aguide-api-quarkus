package br.com.aguideptbr.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String SECRET_TOKEN = "my-token-super-recur-12345";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String authorizationHeader = requestContext.getHeaderString("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Is necessary a Bearer Token for auth process!!!")
                            .build());
            return;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (!token.equals(SECRET_TOKEN)) {

            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Invalid Token was send!!!")
                            .build());
        }
    }
}
