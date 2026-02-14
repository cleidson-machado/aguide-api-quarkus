package br.com.aguideptbr.features.auth;

import org.jboss.logging.Logger;

import br.com.aguideptbr.features.auth.dto.GoogleOAuthRequest;
import br.com.aguideptbr.features.auth.dto.LoginRequest;
import br.com.aguideptbr.features.auth.dto.LoginResponse;
import br.com.aguideptbr.features.auth.dto.RegisterRequest;
import br.com.aguideptbr.features.auth.dto.UserInfoDTO;
import br.com.aguideptbr.features.user.UserModel;
import br.com.aguideptbr.features.user.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * Serviço de autenticação responsável por:
 * - Registro de novos usuários
 * - Login de usuários existentes
 * - Geração de tokens JWT
 */
@ApplicationScoped
public class AuthService {

    private final Logger log;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(Logger log, JWTService jwtService, PasswordEncoder passwordEncoder) {
        this.log = log;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

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
                    Status.CONFLICT);
        }

        // Cria novo usuário
        UserModel newUser = new UserModel();
        newUser.name = request.getName();
        newUser.surname = request.getSurname();
        newUser.email = request.getEmail().toLowerCase().trim();
        newUser.passwordHash = passwordEncoder.hashPassword(request.getPassword());
        newUser.role = UserRole.FREE; // Role padrão para novos usuários

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
                    Status.UNAUTHORIZED);
        }

        // Verifica se o usuário usa OAuth2 (não tem senha local)
        if (user.isOAuthUser()) {
            log.warnf("⚠️ OAuth2 user trying password login: %s", request.getEmail());
            throw new WebApplicationException(
                    "This account is linked to a social provider. Please use social login.",
                    Status.BAD_REQUEST);
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
                    Status.UNAUTHORIZED);
        }

        log.infof("✅ Login bem-sucedido: %s", user.email);

        // Gera token JWT
        String token = jwtService.generateToken(user);

        return buildLoginResponse(token, user);
    }

    /**
     * Autentica ou registra um usuário via Google OAuth.
     *
     * <p>
     * Fluxo de autenticação OAuth:
     * </p>
     * <ol>
     * <li>Busca usuário pelo oauthId do Google</li>
     * <li>Se não existir, busca por email</li>
     * <li>Se não existir, cria novo usuário com dados do Google</li>
     * <li>Atualiza tokens OAuth do usuário</li>
     * <li>Gera token JWT da aplicação</li>
     * <li>Retorna resposta de login</li>
     * </ol>
     *
     * @param request Dados de autenticação do Google
     * @return Resposta de login com token JWT
     * @throws WebApplicationException se o email já estiver cadastrado com senha
     *                                 local
     */
    @Transactional
    public LoginResponse loginWithGoogle(GoogleOAuthRequest request) {
        log.infof("🔐 Google OAuth login attempt: %s (OAuth ID: %s)",
                request.getEmail(), request.getOauthId());

        // 1. Busca usuário pelo OAuth ID (mais confiável)
        UserModel user = UserModel.findByOAuth(
                request.getOauthProvider().toUpperCase(),
                request.getOauthId());

        // 2. Se não encontrou, busca por email
        if (user == null) {
            user = UserModel.findByEmail(request.getEmail().toLowerCase().trim());

            // 3. Se encontrou por email mas já tem senha local, retorna erro
            if (user != null && !user.isOAuthUser()) {
                log.warnf("⚠️ Email %s já está cadastrado com senha local", request.getEmail());
                throw new WebApplicationException(
                        "Email already registered with password. Please login with email and password.",
                        Status.CONFLICT);
            }

            // 4. Se não existe usuário, cria um novo
            if (user == null) {
                log.infof("📝 Creating new user from Google OAuth: %s", request.getEmail());
                user = new UserModel();
                user.name = request.getName();
                user.surname = request.getSurname();
                user.email = request.getEmail().toLowerCase().trim();
                user.role = UserRole.FREE; // Role padrão para novos usuários OAuth
                user.passwordHash = null; // OAuth users não têm senha local
            }

            // 5. Atualiza dados OAuth do usuário
            user.oauthProvider = request.getOauthProvider().toUpperCase();
            user.oauthId = request.getOauthId();
        }

        // 6. Persiste no banco (cria ou atualiza)
        user.persist();

        log.infof("✅ Google OAuth login successful: %s (ID: %s)", user.email, user.id);

        // 7. Gera token JWT da aplicação
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
