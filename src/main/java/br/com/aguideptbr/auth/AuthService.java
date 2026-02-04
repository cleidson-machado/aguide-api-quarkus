package br.com.aguideptbr.auth;

import org.jboss.logging.Logger;

import br.com.aguideptbr.auth.dto.LoginRequest;
import br.com.aguideptbr.auth.dto.LoginResponse;
import br.com.aguideptbr.auth.dto.RegisterRequest;
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
        log.infof("📝 Tentativa de registro: %s", request.email);

        // Verifica se o email já está cadastrado
        UserModel existingUser = UserModel.findByEmail(request.email);
        if (existingUser != null) {
            log.warnf("⚠️ Email já cadastrado: %s", request.email);
            throw new WebApplicationException(
                    "Email já cadastrado",
                    Response.Status.CONFLICT);
        }

        // Cria novo usuário
        UserModel newUser = new UserModel();
        newUser.name = request.name;
        newUser.surname = request.surname;
        newUser.email = request.email.toLowerCase().trim();
        newUser.passwordHash = passwordEncoder.hashPassword(request.password);
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
        log.infof("🔐 Tentativa de login: %s", request.email);

        // Busca usuário pelo email
        UserModel user = UserModel.findByEmail(request.email.toLowerCase().trim());

        if (user == null) {
            log.warnf("⚠️ Usuário não encontrado: %s", request.email);
            throw new WebApplicationException(
                    "Email ou senha inválidos",
                    Response.Status.UNAUTHORIZED);
        }

        // Verifica se o usuário usa OAuth2 (não tem senha local)
        if (user.isOAuthUser()) {
            log.warnf("⚠️ Usuário OAuth2 tentando login com senha: %s", request.email);
            throw new WebApplicationException(
                    "Este usuário está vinculado a uma conta social. Use o login social.",
                    Response.Status.BAD_REQUEST);
        }

        // Verifica a senha
        boolean passwordValid = passwordEncoder.verifyPassword(request.password, user.passwordHash);

        if (!passwordValid) {
            log.warnf("⚠️ Senha incorreta para usuário: %s", request.email);
            throw new WebApplicationException(
                    "Email ou senha inválidos",
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
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
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
