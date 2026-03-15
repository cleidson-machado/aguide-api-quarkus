package br.com.aguideptbr.features.userchoice;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.aguideptbr.features.userchoice.enuns.ChannelAgeRange;
import br.com.aguideptbr.features.userchoice.enuns.CommercialIntent;
import br.com.aguideptbr.features.userchoice.enuns.CurrentSituation;
import br.com.aguideptbr.features.userchoice.enuns.ImmigrationTimeframe;
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
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
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
     * Nicho principal de interesse ou atuação do usuário.
     *
     * EXEMPLOS:
     * - "Imigração Portugal"
     * - "Tecnologia"
     * - "Educação Financeira"
     * - "Lifestyle"
     *
     * FUTURO: Escalar para múltiplos nichos ou associar a uma tabela de nichos
     * predefinidos.
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
     * Tema central do conteúdo produzido (texto livre).
     *
     * EXEMPLOS:
     * - "Imigração para Portugal"
     * - "Programação e tecnologia"
     * - "Viagens e lifestyle"
     * - "Educação financeira"
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Column(name = "main_niche", length = 200)
    public String mainNiche;

    /**
     * Formatos de conteúdo produzidos (array serializado em JSON).
     *
     * VALORES DE REFERÊNCIA (enviados pelo app Flutter):
     * - VLOG
     * - TUTORIAL
     * - INTERVIEW
     * - NEWS_ANALYSIS
     * - SHORTS
     * - OTHER
     *
     * FORMATO ARMAZENADO:
     * '["VLOG", "TUTORIAL", "SHORTS"]'
     *
     * FUTURO: Considerar usar PostgreSQL JSON/JSONB column ou tabela associativa.
     *
     * REGRA: Obrigatório se profileType = CREATOR, nulo se CONSUMER.
     */
    @Column(name = "content_formats", columnDefinition = "TEXT")
    public String contentFormats;

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
     * VALORES DE REFERÊNCIA (enviados pelo app Flutter):
     * - YOUTUBE
     * - WHATSAPP_TELEGRAM
     * - BLOGS
     * - LAWYERS_CONSULTANTS
     * - FORUMS
     * - SOCIAL_MEDIA
     *
     * FORMATO ARMAZENADO:
     * '["YOUTUBE", "BLOGS", "FORUMS"]'
     *
     * FUTURO: Considerar usar PostgreSQL JSON/JSONB column ou tabela associativa.
     *
     * REGRA: Obrigatório se profileType = CONSUMER, nulo se CREATOR.
     */
    @Column(name = "current_info_sources", columnDefinition = "TEXT")
    public String currentInfoSources;

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
     * Valida se campos obrigatórios do perfil CREATOR estão preenchidos.
     *
     * IMPLEMENTAÇÃO FUTURA:
     * public boolean validateCreatorFields() {
     * if (profileType != UserProfileType.CREATOR) return false;
     * return channelName != null && !channelName.isBlank()
     * && channelHandle != null && !channelHandle.isBlank()
     * && channelAgeRange != null
     * && subscriberRange != null
     * && monetizationStatus != null
     * && publishingFrequency != null;
     * }
     */

    /**
     * Valida se campos obrigatórios do perfil CONSUMER estão preenchidos.
     *
     * IMPLEMENTAÇÃO FUTURA:
     * public boolean validateConsumerFields() {
     * if (profileType != UserProfileType.CONSUMER) return false;
     * return currentSituation != null
     * && mainObjective != null
     * && visaTypeInterest != null
     * && knowledgeLevel != null
     * && mainDifficulty != null
     * && preferredContentType != null
     * && serviceHiringIntent != null
     * && immigrationTimeframe != null;
     * }
     */

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
