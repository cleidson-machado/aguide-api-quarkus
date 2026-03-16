package br.com.aguideptbr.features.userchoice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.aguideptbr.features.userchoice.enuns.ChannelAgeRange;
import br.com.aguideptbr.features.userchoice.enuns.CommercialIntent;
import br.com.aguideptbr.features.userchoice.enuns.ContentFormat;
import br.com.aguideptbr.features.userchoice.enuns.CurrentSituation;
import br.com.aguideptbr.features.userchoice.enuns.ImmigrationTimeframe;
import br.com.aguideptbr.features.userchoice.enuns.InfoSource;
import br.com.aguideptbr.features.userchoice.enuns.KnowledgeLevel;
import br.com.aguideptbr.features.userchoice.enuns.MainDifficulty;
import br.com.aguideptbr.features.userchoice.enuns.MainObjective;
import br.com.aguideptbr.features.userchoice.enuns.MonetizationStatus;
import br.com.aguideptbr.features.userchoice.enuns.PreferredContentType;
import br.com.aguideptbr.features.userchoice.enuns.PublishingFrequency;
import br.com.aguideptbr.features.userchoice.enuns.ServiceHiringIntent;
import br.com.aguideptbr.features.userchoice.enuns.SubscriberRange;
import br.com.aguideptbr.features.userchoice.enuns.UserProfileType;
import br.com.aguideptbr.features.userchoice.enuns.VisaTypeInterest;
import br.com.aguideptbr.util.StringListJsonConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade representando as escolhas e perfil de preferências do usuário.
 *
 * OBJETIVO: Armazenar dados coletados do formulário de onboarding preenchido
 * pelo usuário no app Flutter, capturando perfil de preferências e
 * características do usuário.
 *
 * PERFIS SUPORTADOS:
 * 1. CRIADOR de conteúdo (CREATOR) - YouTubers, produtores, influencers
 * 2. CONSUMIDOR de conteúdo (CONSUMER) - Pesquisadores, planejadores, audiência
 *
 * RELACIONAMENTOS SUGERIDOS (NÃO IMPLEMENTADOS AINDA):
 * - @ManyToOne UserModel user (1 perfil por usuário, relacionamento 1-1)
 * - @ManyToOne UserRankingModel userRanking (associação futura para análise
 * integrada)
 *
 * RELACIONAMENTO FUTURO COM UserRankingModel:
 * Esta entidade pode ser associada ao UserRankingModel para análises
 * preditivas,
 * segmentação e personalização. O campo 'userId' permite vincular facilmente
 * ambas as entidades:
 *
 * Exemplo de relacionamento futuro:
 *
 * @ManyToOne(fetch = FetchType.LAZY)
 * @JoinColumn(name = "user_id", nullable = false, unique = true)
 *                  public UserModel user;
 *
 *                  // Método auxiliar para buscar ranking associado:
 *                  public UserRankingModel findAssociatedRanking() {
 *                  return UserRankingModel.find("userId = ?1",
 *                  this.userId).firstResult();
 *                  }
 *
 *                  REGRAS DE NEGÓCIO:
 *                  - Campos exclusivos de CREATOR ficam nulos quando
 *                  profileType =
 *                  CONSUMER
 *                  - Campos exclusivos de CONSUMER ficam nulos quando
 *                  profileType =
 *                  CREATOR
 *                  - Campos comuns (userId, profileType, nicheContext) são
 *                  obrigatórios
 *
 *                  USO TÍPICO:
 *                  - Onboarding: capturar perfil inicial do usuário
 *                  - Segmentação: direcionar conteúdo/features específicas por
 *                  tipo
 *                  de perfil
 *                  - Analytics: analisar distribuição de perfis (CREATOR vs
 *                  CONSUMER)
 *                  - Recomendações: sugerir conteúdo baseado em preferências
 *                  declaradas
 */
@Entity
@Table(name = "app_user_choices_profile")
public class UserChoiceModel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    // ========== RELACIONAMENTO COM USUÁRIO (NÃO IMPLEMENTADO) ==========

    /**
     * ID do usuário (relacionamento manual por enquanto).
     *
     * FUTURO: Transformar em @ManyToOne UserModel quando definir modelagem.
     * Exemplo:
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "user_id", nullable = false, unique = true)
     *                  public UserModel user;
     *
     *                  IMPORTANTE: Adicionar constraint UNIQUE se for 1-1 (1
     *                  perfil por usuário).
     *
     *                  RELACIONAMENTO COM UserRankingModel:
     *                  Após implementar o relacionamento com UserModel, será
     *                  possível associar com UserRankingModel via:
     *
     *                  UserRankingModel ranking = UserRankingModel.find("userId =
     *                  ?1", this.userId).firstResult();
     *
     *                  Ou criar método auxiliar:
     *                  public UserRankingModel findAssociatedRanking() {
     *                  return UserRankingModel.find("userId = ?1 and deletedAt is
     *                  null", this.userId).firstResult();
     *                  }
     */
    @Column(name = "user_id", nullable = false)
    public UUID userId;

    // ========== CAMPOS COMUNS (CREATOR + CONSUMER) ==========

    /**
     * Tipo de perfil do usuário (CREATOR ou CONSUMER).
     * Determina quais campos específicos serão preenchidos.
     *
     * REGRA: Obrigatório para todos os registros.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", length = 20, nullable = false)
    public UserProfileType profileType;

    /**
     * Nicho principal de interesse ou atuação do usuário (escopo macro da
     * plataforma).
     *
     * EXEMPLOS:
     * - "Imigração Portugal" (nicho atual da plataforma)
     * - "Tecnologia" (nicho futuro)
     * - "Educação Financeira" (nicho futuro)
     * - "Lifestyle" (nicho futuro)
     *
     * DIFERENÇA EM RELAÇÃO A 'mainNiche' (CREATOR):
     * - nicheContext: Nicho MACRO da plataforma (ex: "Imigração Portugal")
     * - mainNiche: Tema ESPECÍFICO do canal do criador (ex: "Visto D7 para
     * aposentados")
     *
     * Exemplo prático:
     * - nicheContext = "Imigração Portugal" (todos os usuários do nicho)
     * - mainNiche = "Golden Visa e investimentos" (tema específico do canal
     * CREATOR)
     *
     * IMPORTANTE:
     * Este campo determina quais campos específicos de nicho são obrigatórios.
     * Para nicho "Imigração Portugal", campos como visaTypeInterest e
     * immigrationTimeframe
     * são obrigatórios para CONSUMER. Para outros nichos futuros, esses campos
     * podem
     * ser opcionais.
     *
     * @see #mainNiche Para o tema específico do canal (CREATOR)
     * @see #isImmigrationNiche() Helper para validação condicional por nicho
     */
    @Column(name = "niche_context", length = 200, nullable = false)
    public String nicheContext;

    // ========== CAMPOS ESPECÍFICOS: PERFIL CRIADOR (CREATOR) ==========

    /**
     * Nome do canal no YouTube.
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Column(name = "channel_name", length = 200)
    public String channelName;

    /**
     * @handle ou link do canal do YouTube.
     *
     *         EXEMPLOS:
     *         - "@meucanalpt"
     *         - "https://youtube.com/@meucanalpt"
     *
     *         REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Column(name = "channel_handle", length = 300)
    public String channelHandle;

    /**
     * Tempo de atividade do canal (faixa).
     *
     * VALORES:
     * - LESS_THAN_6_MONTHS
     * - SIX_MONTHS_TO_1_YEAR
     * - ONE_TO_3_YEARS
     * - MORE_THAN_3_YEARS
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_age_range", length = 30)
    public ChannelAgeRange channelAgeRange;

    /**
     * Faixa de inscritos do canal.
     *
     * VALORES:
     * - LESS_THAN_1K
     * - ONE_K_TO_10K
     * - TEN_K_TO_100K
     * - MORE_THAN_100K
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscriber_range", length = 30)
    public SubscriberRange subscriberRange;

    /**
     * Status de monetização no YouTube Partner Program (YPP).
     *
     * VALORES:
     * - MONETIZED (já monetizado)
     * - NOT_MONETIZED (não monetizado)
     * - IN_PROGRESS (em processo de aprovação)
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "monetization_status", length = 30)
    public MonetizationStatus monetizationStatus;

    /**
     * Tema central/específico do conteúdo produzido pelo criador (texto livre).
     *
     * EXEMPLOS (para nicho "Imigração Portugal"):
     * - "Visto D7 para aposentados e pessoas com renda passiva"
     * - "Golden Visa e investimentos imobiliários"
     * - "Vida de brasileiro em Lisboa"
     * - "Processo de reagrupamento familiar"
     *
     * DIFERENÇA EM RELAÇÃO A 'nicheContext':
     * - nicheContext: Nicho MACRO da plataforma (ex: "Imigração Portugal") - Campo
     * comum
     * - mainNiche: Tema ESPECÍFICO do canal do criador (ex: "Visto D7") - Campo
     * CREATOR
     *
     * Exemplo prático:
     * - nicheContext = "Imigração Portugal" (mesmo para todos os usuários do nicho)
     * - mainNiche = "Golden Visa e investimentos" (específico deste canal CREATOR)
     *
     * IMPORTANTE:
     * - nicheContext é preenchido para CREATOR e CONSUMER (é o escopo da
     * plataforma)
     * - mainNiche é preenchido APENAS para CREATOR (é o tema do canal)
     *
     * @see #nicheContext Para o nicho macro da plataforma
     * @since 1.0.0
     * @apiNote Este campo auxilia no matching CONSUMER → CREATOR (match por tema
     *          específico)
     *
     *          REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Column(name = "main_niche", length = 200)
    public String mainNiche;

    /**
     * Formatos de conteúdo produzidos (array serializado em JSON).
     *
     * VALORES PERMITIDOS: Definidos no enum ContentFormat
     * - VLOG (vídeos estilo diário)
     * - TUTORIAL (conteúdo educacional passo a passo)
     * - INTERVIEW (entrevistas com especialistas)
     * - NEWS_ANALYSIS (análise de notícias)
     * - SHORTS (vídeos curtos)
     * - OTHER (outros formatos)
     *
     * FORMATO ARMAZENADO:
     * '["VLOG", "TUTORIAL", "SHORTS"]'
     *
     * CONVERSÃO: Automática via StringListJsonConverter (List<String> ↔ JSON).
     *
     * VALIDAÇÃO: Use ContentFormat.isValidStringList(values) para validar valores
     * antes de persistir no banco.
     *
     * @see ContentFormat Enum com valores permitidos e métodos de conversão
     * @see StringListJsonConverter Conversor JPA utilizado
     * @apiNote Criar validator @ValidContentFormats no DTO CreateUserChoiceRequest
     *
     *          REGRA: Obrigatório se profileType = CREATOR, nulo/vazio se CONSUMER.
     */
    @Column(name = "content_formats", columnDefinition = "TEXT")
    @Convert(converter = StringListJsonConverter.class)
    public List<String> contentFormats;

    /**
     * Intenção comercial do criador na plataforma.
     *
     * VALORES:
     * - BRAND_PARTNERSHIP (parcerias com marcas)
     * - SELL_OWN_SERVICES (vender serviços próprios)
     * - AUDIENCE_GROWTH (crescer audiência)
     * - CONSULTING (consultoria)
     * - OTHER (outros)
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "commercial_intent", length = 30)
    public CommercialIntent commercialIntent;

    /**
     * Descrição do serviço ou produto oferecido pelo criador (texto livre).
     *
     * EXEMPLOS:
     * - "Consultoria de visto D7"
     * - "Curso online sobre imigração"
     * - "E-books sobre Portugal"
     *
     * REGRA: Opcional (pode estar vazio mesmo se CREATOR).
     */
    @Column(name = "offered_service", length = 500)
    public String offeredService;

    /**
     * Frequência de publicação de conteúdo.
     *
     * VALORES:
     * - DAILY (diariamente)
     * - WEEKLY (semanalmente)
     * - BIWEEKLY (quinzenalmente)
     * - MONTHLY (mensalmente)
     * - IRREGULAR (irregular)
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "publishing_frequency", length = 30)
    public PublishingFrequency publishingFrequency;

    /**
     * Diferenciais do conteúdo produzido (texto livre).
     *
     * EXEMPLOS:
     * - "Experiência pessoal de sucesso no visto D7"
     * - "Cobertura de mudanças nas leis de imigração"
     * - "Foco em nichos específicos (investidores, freelancers)"
     *
     * LIMITE: 500 caracteres
     *
     * REGRA: Opcional (pode estar vazio mesmo se CREATOR).
     */
    @Column(name = "content_differential", length = 500)
    public String contentDifferential;

    // ========== CAMPOS ESPECÍFICOS: PERFIL CONSUMIDOR (CONSUMER) ==========

    /**
     * Situação atual do consumidor em relação à imigração.
     *
     * VALORES:
     * - PLANNING_TO_IMMIGRATE (planejando imigrar)
     * - VISA_IN_PROGRESS (visto em andamento)
     * - ALREADY_IN_PORTUGAL (já em Portugal)
     * - JUST_RESEARCHING (apenas pesquisando)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_situation", length = 30)
    public CurrentSituation currentSituation;

    /**
     * Objetivo principal de pesquisa do consumidor.
     *
     * VALORES:
     * - VISA_INFO (informações sobre vistos)
     * - JOB_OPPORTUNITIES (oportunidades de trabalho)
     * - QUALITY_OF_LIFE (qualidade de vida)
     * - EDUCATION (educação)
     * - ENTREPRENEURSHIP (empreendedorismo)
     * - OTHER (outros)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "main_objective", length = 30)
    public MainObjective mainObjective;

    /**
     * Tipo de visto de interesse do consumidor.
     *
     * VALORES:
     * - D7_PASSIVE_INCOME (visto D7 - renda passiva)
     * - D8_DIGITAL_NOMAD (visto D8 - nômade digital)
     * - GOLDEN_VISA (golden visa)
     * - WORK_VISA (visto de trabalho)
     * - STUDY_VISA (visto de estudos)
     * - FAMILY_REUNIFICATION (reunificação familiar)
     * - NOT_SURE_YET (ainda não decidiu)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visa_type_interest", length = 30)
    public VisaTypeInterest visaTypeInterest;

    /**
     * Nível de conhecimento sobre imigração/Portugal.
     *
     * VALORES:
     * - BEGINNER (iniciante)
     * - INTERMEDIATE (intermediário)
     * - ADVANCED (avançado)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "knowledge_level", length = 30)
    public KnowledgeLevel knowledgeLevel;

    /**
     * Fontes de informação atuais do consumidor (array serializado em JSON).
     *
     * VALORES PERMITIDOS: Definidos no enum InfoSource
     * - YOUTUBE (vídeos e canais)
     * - WHATSAPP_TELEGRAM (grupos)
     * - BLOGS (sites especializados)
     * - LAWYERS_CONSULTANTS (profissionais especializados)
     * - FORUMS (comunidades online)
     * - SOCIAL_MEDIA (redes sociais)
     *
     * FORMATO ARMAZENADO:
     * '["YOUTUBE", "BLOGS", "FORUMS"]'
     *
     * CONVERSÃO: Automática via StringListJsonConverter (List<String> ↔ JSON).
     *
     * VALIDAÇÃO: Use InfoSource.isValidStringList(values) para validar valores
     * antes de persistir no banco.
     *
     * @see InfoSource Enum com valores permitidos e métodos de conversão
     * @see StringListJsonConverter Conversor JPA utilizado
     * @apiNote Criar validator @ValidInfoSources no DTO CreateUserChoiceRequest
     *
     *          REGRA: Obrigatório se profileType = CONSUMER, nulo/vazio se CREATOR.
     */
    @Column(name = "current_info_sources", columnDefinition = "TEXT")
    @Convert(converter = StringListJsonConverter.class)
    public List<String> currentInfoSources;

    /**
     * Maior dificuldade ao buscar informações sobre imigração.
     *
     * VALORES:
     * - OUTDATED_INFO (informação desatualizada)
     * - SUPERFICIAL_CONTENT (conteúdo superficial)
     * - HARD_TO_FIND_NICHE (difícil encontrar nicho específico)
     * - CONTRADICTORY_INFO (informações contraditórias)
     * - OTHER (outros)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "main_difficulty", length = 30)
    public MainDifficulty mainDifficulty;

    /**
     * Tipo de conteúdo preferido pelo consumidor.
     *
     * VALORES:
     * - PERSONAL_STORIES (histórias pessoais)
     * - STEP_BY_STEP_TUTORIALS (tutoriais passo a passo)
     * - LEGAL_ANALYSIS (análises jurídicas)
     * - COMPARISONS (comparações)
     * - NEWS_AND_UPDATES (notícias e atualizações)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_content_type", length = 30)
    public PreferredContentType preferredContentType;

    /**
     * Intenção de contratar serviços profissionais.
     *
     * VALORES:
     * - YES_CONSULTING (sim, consultoria)
     * - YES_MENTORING (sim, mentoria)
     * - MAYBE (talvez)
     * - NO_FREE_ONLY (não, só conteúdo grátis)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_hiring_intent", length = 30)
    public ServiceHiringIntent serviceHiringIntent;

    /**
     * Prazo planejado para imigrar.
     *
     * VALORES:
     * - LESS_THAN_6_MONTHS (menos de 6 meses)
     * - SIX_MONTHS_TO_1_YEAR (6 meses a 1 ano)
     * - ONE_TO_2_YEARS (1 a 2 anos)
     * - NO_DEFINED_TIMEFRAME (sem prazo definido)
     * - NOT_PLANNING (não planeja imigrar)
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "immigration_timeframe", length = 30)
    public ImmigrationTimeframe immigrationTimeframe;

    /**
     * Expectativa do consumidor com a plataforma (texto livre).
     *
     * EXEMPLOS:
     * - "Encontrar informações atualizadas sobre vistos"
     * - "Conectar com profissionais confiáveis"
     * - "Aprender sobre custo de vida em Portugal"
     *
     * LIMITE: 500 caracteres
     *
     * REGRA: Opcional (pode estar vazio mesmo se CONSUMER).
     */
    @Column(name = "platform_expectation", length = 500)
    public String platformExpectation;

    // ========== AUDITORIA E METADADOS ==========

    /**
     * Data de criação do registro de perfil.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * Data da última atualização do registro.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Data de exclusão lógica (soft delete).
     * Null significa que o perfil está ativo.
     *
     * NOTA: Raramente usado, pois perfil está vinculado ao usuário.
     * Se usuário for deletado, perfil também deve ser.
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    // ========== MÉTODOS DE NEGÓCIO ==========

    /**
     * Verifica se o perfil é do tipo CRIADOR.
     */
    public boolean isCreator() {
        return this.profileType == UserProfileType.CREATOR;
    }

    /**
     * Verifica se o perfil é do tipo CONSUMIDOR.
     */
    public boolean isConsumer() {
        return this.profileType == UserProfileType.CONSUMER;
    }

    /**
     * Verifica se o perfil está ativo.
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Marca o perfil como deletado (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura o perfil deletado.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Verifica se o nicho atual é relacionado a imigração.
     *
     * CONTEXTO:
     * Campos de imigração (visaTypeInterest, immigrationTimeframe) são
     * obrigatórios APENAS para o nicho "Imigração Portugal".
     * Quando a plataforma escalar para outros nichos (ex: "Tecnologia",
     * "Investimentos"), esses campos serão opcionais.
     *
     * IMPLEMENTAÇÃO:
     * Busca case-insensitive por "Imigração" ou "Immigration" no nicheContext.
     *
     * @return true se nicheContext indica nicho de imigração
     * @apiNote Usado em validateConsumerFields() para validação condicional
     */
    private boolean isImmigrationNiche() {
        if (nicheContext == null) {
            return false;
        }
        String lowerNiche = nicheContext.toLowerCase();
        return lowerNiche.contains("imigração") || lowerNiche.contains("immigration");
    }

    /**
     * Valida se campos obrigatórios do perfil CREATOR estão preenchidos.
     *
     * @return true se todos os campos obrigatórios de CREATOR estão válidos
     */
    public boolean validateCreatorFields() {
        if (profileType != UserProfileType.CREATOR) {
            return false;
        }

        return channelName != null && !channelName.isBlank()
                && channelHandle != null && !channelHandle.isBlank()
                && channelAgeRange != null
                && subscriberRange != null
                && monetizationStatus != null
                && mainNiche != null && !mainNiche.isBlank()
                && contentFormats != null && !contentFormats.isEmpty()
                && commercialIntent != null
                && publishingFrequency != null;
    }

    /**
     * Valida se campos obrigatórios do perfil CONSUMER estão preenchidos.
     *
     * CAMPOS SEMPRE OBRIGATÓRIOS (qualquer nicho):
     * - current Situation
     * - mainObjective
     * - knowledgeLevel
     * - currentInfoSources
     * - mainDifficulty
     * - preferredContentType
     * - serviceHiringIntent
     *
     * CAMPOS CONDICIONAIS (apenas para nicho de imigração):
     * - visaTypeInterest (obrigatório se isImmigrationNiche())
     * - immigrationTimeframe (obrigatório se isImmigrationNiche())
     *
     * ESCALABILIDADE:
     * Quando adicionar nichos "Tecnologia", "Investimentos", etc., apenas
     * os campos genéricos serão validados (visaTypeInterest e immigrationTimeframe
     * ficam opcionais).
     *
     * @return true se todos os campos obrigatórios de CONSUMER estão válidos
     */
    public boolean validateConsumerFields() {
        if (profileType != UserProfileType.CONSUMER) {
            return false;
        }

        // Validar campos genéricos (obrigatórios para qualquer nicho)
        boolean genericFieldsValid = currentSituation != null
                && mainObjective != null
                && knowledgeLevel != null
                && currentInfoSources != null && !currentInfoSources.isEmpty()
                && mainDifficulty != null
                && preferredContentType != null
                && serviceHiringIntent != null;

        if (!genericFieldsValid) {
            return false;
        }

        // Validar campos específicos de imigração (se aplicável)
        if (isImmigrationNiche()) {
            boolean immigrationFieldsValid = visaTypeInterest != null
                    && immigrationTimeframe != null;
            return immigrationFieldsValid;
        }

        // Se não for nicho de imigração, campos genéricos são suficientes
        return true;
    }

    /**
     * Valida coerência lógica entre campos do perfil CONSUMER (nicho imigração).
     *
     * VALIDAÇÕES IMPLEMENTADAS:
     * 1. ALREADY_IN_PORTUGAL: immigrationTimeframe deve ser NOT_PLANNING ou null
     * (quem já está lá não planeja ir)
     * 2. VISA_IN_PROGRESS: immigrationTimeframe NÃO pode ser NOT_PLANNING ou null
     * (quem tem visto em processo está planejando)
     * 3. NOT_PLANNING: currentSituation NÃO pode ser PLANNING_TO_IMMIGRATE ou
     * VISA_IN_PROGRESS
     * (sem planos não pode estar planejando ou com visto em processo)
     * 4. ALREADY_IN_PORTUGAL + NOT_SURE_YET (visaTypeInterest): INCONSISTENTE
     * (quem já está lá não pode estar indeciso sobre visto)
     *
     * CONTEXTO:
     * Flutter permite combinações impossíveis se não houver validação condicional.
     * Exemplo: Usuário seleciona "Já estou em Portugal" mas depois diz "Pretendo
     * ir em menos de 6 meses".
     *
     * ESCALABILIDADE:
     * Esta validação é específica do nicho "Imigração Portugal".
     * Outros nichos (Tecnologia, Investimentos) não terão esses campos, então
     * este método retornará lista vazia.
     *
     * @return Lista de erros lógicos (vazia se tudo ok)
     * @apiNote Chamar em isValid() e getValidationErrors() para garantir coerência
     */
    public List<String> validateConsumerLogicalConsistency() {
        List<String> logicalErrors = new ArrayList<>();

        // Validação só aplica para CONSUMER no nicho imigração
        if (profileType != UserProfileType.CONSUMER || !isImmigrationNiche()) {
            return logicalErrors; // Lista vazia = sem erros
        }

        // VALIDAÇÃO 1: Já em Portugal → timeframe deve ser NOT_PLANNING ou null
        if (currentSituation == CurrentSituation.ALREADY_IN_PORTUGAL) {
            if (immigrationTimeframe != null && immigrationTimeframe != ImmigrationTimeframe.NOT_PLANNING) {
                logicalErrors.add(
                        "Inconsistência: currentSituation=ALREADY_IN_PORTUGAL mas immigrationTimeframe indica planejamento futuro ("
                                + immigrationTimeframe + "). Esperado: NOT_PLANNING ou null.");
            }
        }

        // VALIDAÇÃO 2: Visto em processo → timeframe NÃO pode ser NOT_PLANNING
        if (currentSituation == CurrentSituation.VISA_IN_PROGRESS) {
            if (immigrationTimeframe == null || immigrationTimeframe == ImmigrationTimeframe.NOT_PLANNING) {
                logicalErrors.add(
                        "Inconsistência: currentSituation=VISA_IN_PROGRESS mas immigrationTimeframe=" +
                                immigrationTimeframe + ". Esperado: prazo definido (LESS_THAN_6_MONTHS, etc.).");
            }
        }

        // VALIDAÇÃO 3: Timeframe NOT_PLANNING → currentSituation coerente
        if (immigrationTimeframe == ImmigrationTimeframe.NOT_PLANNING) {
            if (currentSituation == CurrentSituation.PLANNING_TO_IMMIGRATE
                    || currentSituation == CurrentSituation.VISA_IN_PROGRESS) {
                logicalErrors.add(
                        "Inconsistência: immigrationTimeframe=NOT_PLANNING mas currentSituation indica planejamento ativo ("
                                + currentSituation + ").");
            }
        }

        // VALIDAÇÃO 4: Já em Portugal → visto NÃO pode ser NOT_SURE_YET
        if (currentSituation == CurrentSituation.ALREADY_IN_PORTUGAL) {
            if (visaTypeInterest == VisaTypeInterest.NOT_SURE_YET) {
                logicalErrors.add(
                        "Inconsistência: currentSituation=ALREADY_IN_PORTUGAL mas visaTypeInterest=NOT_SURE_YET. "
                                + "Quem já está em Portugal deveria saber o tipo de visto de interesse.");
            }
        }

        return logicalErrors;
    }

    /**
     * Valida se a entidade está completa (campos comuns + campos específicos do
     * perfil).
     *
     * VALIDAÇÕES REALIZADAS:
     * 1. Campos comuns (userId, profileType, nicheContext)
     * 2. Campos específicos por perfil
     * (validateCreatorFields/validateConsumerFields)
     * 3. Coerência lógica CONSUMER (validateConsumerLogicalConsistency)
     *
     * @return true se a entidade está válida
     */
    public boolean isValid() {
        // Validar campos comuns
        if (userId == null || profileType == null || nicheContext == null || nicheContext.isBlank()) {
            return false;
        }

        // Validar campos específicos por perfil
        if (profileType == UserProfileType.CREATOR) {
            return validateCreatorFields();
        } else if (profileType == UserProfileType.CONSUMER) {
            // Validar presença de campos obrigatórios
            if (!validateConsumerFields()) {
                return false;
            }
            // Validar coerência lógica (CONSUMER no nicho imigração)
            return validateConsumerLogicalConsistency().isEmpty();
        }

        return false;
    }

    /**
     * Retorna lista de erros de validação (útil para debugging).
     *
     * TIPOS DE ERROS RETORNADOS:
     * 1. Campos obrigatórios ausentes/vazios
     * 2. Erros lógicos (CONSUMER: validateConsumerLogicalConsistency)
     *
     * @return Lista de campos inválidos e inconsistências lógicas
     */
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();

        // Validar campos comuns
        if (userId == null) {
            errors.add("userId is required");
        }
        if (profileType == null) {
            errors.add("profileType is required");
        }
        if (nicheContext == null || nicheContext.isBlank()) {
            errors.add("nicheContext is required");
        }

        // Validar campos específicos por perfil
        if (profileType == UserProfileType.CREATOR) {
            if (channelName == null || channelName.isBlank()) {
                errors.add("channelName is required for CREATOR");
            }
            if (channelHandle == null || channelHandle.isBlank()) {
                errors.add("channelHandle is required for CREATOR");
            }
            if (channelAgeRange == null) {
                errors.add("channelAgeRange is required for CREATOR");
            }
            if (subscriberRange == null) {
                errors.add("subscriberRange is required for CREATOR");
            }
            if (monetizationStatus == null) {
                errors.add("monetizationStatus is required for CREATOR");
            }
            if (mainNiche == null || mainNiche.isBlank()) {
                errors.add("mainNiche is required for CREATOR");
            }
            if (contentFormats == null || contentFormats.isEmpty()) {
                errors.add("contentFormats is required for CREATOR");
            }
            if (commercialIntent == null) {
                errors.add("commercialIntent is required for CREATOR");
            }
            if (publishingFrequency == null) {
                errors.add("publishingFrequency is required for CREATOR");
            }
        } else if (profileType == UserProfileType.CONSUMER) {
            // Validar campos genéricos (obrigatórios para qualquer nicho)
            if (currentSituation == null) {
                errors.add("currentSituation is required for CONSUMER");
            }
            if (mainObjective == null) {
                errors.add("mainObjective is required for CONSUMER");
            }
            if (knowledgeLevel == null) {
                errors.add("knowledgeLevel is required for CONSUMER");
            }
            if (currentInfoSources == null || currentInfoSources.isEmpty()) {
                errors.add("currentInfoSources is required for CONSUMER");
            }
            if (mainDifficulty == null) {
                errors.add("mainDifficulty is required for CONSUMER");
            }
            if (preferredContentType == null) {
                errors.add("preferredContentType is required for CONSUMER");
            }
            if (serviceHiringIntent == null) {
                errors.add("serviceHiringIntent is required for CONSUMER");
            }

            // Validar campos específicos de imigração (condicionais)
            if (isImmigrationNiche()) {
                if (visaTypeInterest == null) {
                    errors.add("visaTypeInterest is required for CONSUMER in immigration niche");
                }
                if (immigrationTimeframe == null) {
                    errors.add("immigrationTimeframe is required for CONSUMER in immigration niche");
                }
            }

            // Adicionar erros de coerência lógica
            errors.addAll(validateConsumerLogicalConsistency());
        }

        return errors;
    }

    // ========== QUERIES ÚTEIS (IMPLEMENTAR EM UserChoiceRepository) ==========

    /**
     * QUERY: Buscar perfil de um usuário
     * - find("userId = ?1 and deletedAt is null", userId).firstResult()
     *
     * QUERY: Buscar todos os perfis do tipo CREATOR
     * - list("profileType = ?1 and deletedAt is null", UserProfileType.CREATOR)
     *
     * QUERY: Buscar todos os perfis do tipo CONSUMER
     * - list("profileType = ?1 and deletedAt is null", UserProfileType.CONSUMER)
     *
     * QUERY: Buscar criadores monetizados
     * - list("profileType = ?1 and monetizationStatus = ?2 and deletedAt is null",
     * UserProfileType.CREATOR, MonetizationStatus.MONETIZED)
     *
     * QUERY: Buscar consumidores com alto potencial de conversão (intenção de
     * contratar)
     * - list("profileType = ?1 and serviceHiringIntent in (?2, ?3) and deletedAt
     * is null",
     * UserProfileType.CONSUMER,
     * ServiceHiringIntent.YES_CONSULTING,
     * ServiceHiringIntent.YES_MENTORING)
     *
     * QUERY: Buscar perfis criados nos últimos 30 dias
     * - list("createdAt >= ?1 and deletedAt is null",
     * LocalDateTime.now().minusDays(30))
     *
     * QUERY: Buscar por nicho específico
     * - find("nicheContext like ?1 and deletedAt is null", "%Imigração%").list()
     */
}
