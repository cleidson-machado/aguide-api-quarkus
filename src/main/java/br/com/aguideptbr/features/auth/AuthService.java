package br.com.aguideptbr.features.auth;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.auth.dto.LoginRequest;
import br.com.aguideptbr.features.auth.dto.LoginResponse;
import br.com.aguideptbr.features.auth.dto.RegisterRequest;
import br.com.aguideptbr.features.auth.dto.UserInfoDTO;
import br.com.aguideptbr.features.user.UserModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Serviço de autenticação responsável por:
 * - Registro de novos usuários
 * - Login de usuários existentes
 * - Geração de tokens JWT
 */
@ApplicationScoped
public class AuthService {

    @Inject
    Logger log;

    @Inject
    JWTService jwtService;

    @Inject
    PasswordEncoder passwordEncoder;

    /**
     * Registra um novo usuário na aplicação.
     *
     * @param request Dados de registro
     * @return Resposta de login com token JWT
     * @throws WebApplicationException se o email já estiver cadastrado
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.infof("📝 Tentativa de registro: %s", request.getEmail());

        // Verifica se o email já está cadastrado
        UserModel existingUser = UserModel.findByEmail(request.getEmail());
        if (existingUser != null) {
            log.warnf("⚠️ Email already registered: %s", request.getEmail());
            throw new WebApplicationException(
                    "Email already registered",
                    Response.Status.CONFLICT);
        }

        // Cria novo usuário
        UserModel newUser = new UserModel();
        newUser.name = request.getName();
        newUser.surname = request.getSurname();
        newUser.email = request.getEmail().toLowerCase().trim();
        newUser.passwordHash = passwordEncoder.hashPassword(request.getPassword());
        newUser.role = "USER"; // Role padrão

        // Persiste no banco
        newUser.persist();

        log.infof("✅ Usuário registrado com sucesso: %s (ID: %s)",
                newUser.email, newUser.id);

        // Gera token JWT
        String token = jwtService.generateToken(newUser);

        return buildLoginResponse(token, newUser);
    }

    /**
     * Autentica um usuário existente.
     *
     * @param request Dados de login
     * @return Resposta de login com token JWT
     * @throws WebApplicationException se as credenciais forem inválidas
     */
    public LoginResponse login(LoginRequest request) {
        log.infof("🔐 Tentativa de login: %s", request.getEmail());

        // Busca usuário pelo email
        UserModel user = UserModel.findByEmail(request.getEmail().toLowerCase().trim());

        if (user == null) {
            log.warnf("⚠️ User not found: %s", request.getEmail());
            throw new WebApplicationException(
                    "Invalid email or password",
                    Response.Status.UNAUTHORIZED);
        }

        // Verifica se o usuário usa OAuth2 (não tem senha local)
        if (user.isOAuthUser()) {
            log.warnf("⚠️ OAuth2 user trying password login: %s", request.getEmail());
            throw new WebApplicationException(
                    "This account is linked to a social provider. Please use social login.",
                    Response.Status.BAD_REQUEST);
        }

        // Verifica a senha
        log.debugf("🔐 Verifying password for user: %s", request.getEmail());
        log.debugf("📝 Hash from DB: %s",
                user.passwordHash != null ? user.passwordHash.substring(0, 20) + "..." : "NULL");

        boolean passwordValid = passwordEncoder.verifyPassword(request.getPassword(), user.passwordHash);

        if (!passwordValid) {
            log.warnf("⚠️ Invalid password for user: %s", request.getEmail());
            throw new WebApplicationException(
                    "Invalid email or password",
                    Response.Status.UNAUTHORIZED);
        }

        log.infof("✅ Login bem-sucedido: %s", user.email);

        // Gera token JWT
        String token = jwtService.generateToken(user);

        return buildLoginResponse(token, user);
    }

    /**
     * Constrói a resposta de login com token e informações do usuário.
     *
     * @param token Token JWT gerado
     * @param user  Usuário autenticado
     * @return Resposta de login completa
     */
    private LoginResponse buildLoginResponse(String token, UserModel user) {
        UserInfoDTO userInfo = new UserInfoDTO(
                user.id,
                user.name,
                user.surname,
                user.email,
                user.role);

        return new LoginResponse(
                token,
                jwtService.getExpirationTime(),
                userInfo);
    }
}
