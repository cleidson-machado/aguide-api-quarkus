package br.com.aguideptbr.features.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import br.com.aguideptbr.features.auth.dto.LoginRequest;
import br.com.aguideptbr.features.auth.dto.LoginResponse;
import br.com.aguideptbr.features.auth.dto.RegisterRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

/**
 * REST Controller para autenticação e autorização de usuários.
 *
 * Esta classe implementa endpoints REST para registro de novos usuários,
 * autenticação via email/senha com geração de tokens JWT, e consulta
 * de dados do usuário autenticado.
 *
 * @author Cleidson Machado
 * @since 1.0
 * @see AuthService
 * @see JWTService
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    private final Logger log;
    private final AuthService authService;
    private final JsonWebToken jwt;

    public AuthController(Logger log, AuthService authService, JsonWebToken jwt) {
        this.log = log;
        this.authService = authService;
        this.jwt = jwt;
    }

    /**
     * Registra um novo usuário.
     *
     * @param request Dados de registro
     * @return 201 Created com token JWT
     */
    @POST
    @Path("/register")
    @PermitAll // Endpoint público
    public Response register(@Valid RegisterRequest request) {
        log.infof("POST /api/v1/auth/register - Email: %s", request.getEmail());

        try {
            LoginResponse response = authService.register(request);

            return Response
                    .status(Status.CREATED)
                    .entity(response)
                    .build();
        } catch (WebApplicationException e) {
            throw e; // Re-lança exceções de validação
        } catch (Exception e) {
            log.error("❌ Erro ao registrar usuário", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao processar registro"))
                    .build();
        }
    }

    /**
     * Autentica um usuário existente.
     *
     * @param request Dados de login
     * @return 200 OK com token JWT
     */
    @POST
    @Path("/login")
    @PermitAll // Endpoint público
    public Response login(@Valid LoginRequest request) {
        log.infof("POST /api/v1/auth/login - Email: %s", request.getEmail());

        try {
            LoginResponse response = authService.login(request);

            return Response
                    .ok(response)
                    .build();
        } catch (WebApplicationException e) {
            throw e; // Re-lança exceções de autenticação
        } catch (Exception e) {
            log.error("❌ Erro ao autenticar usuário", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao processar login"))
                    .build();
        }
    }

    /**
     * Retorna as informações do usuário autenticado.
     *
     * Requer token JWT válido no header Authorization.
     *
     * @param securityContext Contexto de segurança injetado
     * @return 200 OK com dados do usuário
     */
    @GET
    @Path("/me")
    @RolesAllowed({ "USER", "ADMIN" }) // Requer autenticação
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        log.info("GET /api/v1/auth/me - Usuário autenticado requisitando dados");

        try {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", UUID.fromString(jwt.getSubject()));
            userInfo.put("email", jwt.getClaim("upn")); // Unique Principal Name
            userInfo.put("name", jwt.getClaim("name"));
            userInfo.put("surname", jwt.getClaim("surname"));
            userInfo.put("role", jwt.getGroups().iterator().next()); // Primeira role

            return Response.ok(userInfo).build();
        } catch (Exception e) {
            log.error("❌ Erro ao buscar dados do usuário", e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao buscar dados do usuário"))
                    .build();
        }
    }

    /**
     * Endpoint de teste para verificar se a API está funcionando.
     *
     * @return 200 OK com mensagem de sucesso
     */
    @GET
    @Path("/health")
    @PermitAll // Endpoint público
    public Response health() {
        return Response
                .ok(Map.of(
                        "status", "OK",
                        "message", "Auth API is running",
                        "timestamp", System.currentTimeMillis()))
                .build();
    }
}
