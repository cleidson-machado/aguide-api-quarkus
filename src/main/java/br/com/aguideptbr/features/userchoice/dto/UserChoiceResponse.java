package br.com.aguideptbr.features.userchoice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

/**
 * DTO de resposta para escolhas de perfil de usuário.
 * Retornado nos endpoints GET, POST e PUT de /api/v1/user-choices.
 *
 * Contém todos os campos da escolha incluindo metadados (id, timestamps).
 */
public class UserChoiceResponse {

    private UUID id;
    private UUID userId;
    private UserProfileType profileType;
    private String nicheContext;

    // ========== CAMPOS CREATOR ==========
    private String channelName;
    private String channelHandle;
    private ChannelAgeRange channelAgeRange;
    private SubscriberRange subscriberRange;
    private MonetizationStatus monetizationStatus;
    private String mainNiche;
    private List<String> contentFormats;
    private CommercialIntent commercialIntent;
    private String offeredService;
    private PublishingFrequency publishingFrequency;
    private String contentDifferential;

    // ========== CAMPOS CONSUMER ==========
    private CurrentSituation currentSituation;
    private MainObjective mainObjective;
    private VisaTypeInterest visaTypeInterest;
    private KnowledgeLevel knowledgeLevel;
    private List<String> currentInfoSources;
    private MainDifficulty mainDifficulty;
    private PreferredContentType preferredContentType;
    private ServiceHiringIntent serviceHiringIntent;
    private ImmigrationTimeframe immigrationTimeframe;
    private String platformExpectation;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtores
    public UserChoiceResponse() {
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UserProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(UserProfileType profileType) {
        this.profileType = profileType;
    }

    public String getNicheContext() {
        return nicheContext;
    }

    public void setNicheContext(String nicheContext) {
        this.nicheContext = nicheContext;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelHandle() {
        return channelHandle;
    }

    public void setChannelHandle(String channelHandle) {
        this.channelHandle = channelHandle;
    }

    public ChannelAgeRange getChannelAgeRange() {
        return channelAgeRange;
    }

    public void setChannelAgeRange(ChannelAgeRange channelAgeRange) {
        this.channelAgeRange = channelAgeRange;
    }

    public SubscriberRange getSubscriberRange() {
        return subscriberRange;
    }

    public void setSubscriberRange(SubscriberRange subscriberRange) {
        this.subscriberRange = subscriberRange;
    }

    public MonetizationStatus getMonetizationStatus() {
        return monetizationStatus;
    }

    public void setMonetizationStatus(MonetizationStatus monetizationStatus) {
        this.monetizationStatus = monetizationStatus;
    }

    public String getMainNiche() {
        return mainNiche;
    }

    public void setMainNiche(String mainNiche) {
        this.mainNiche = mainNiche;
    }

    public List<String> getContentFormats() {
        return contentFormats;
    }

    public void setContentFormats(List<String> contentFormats) {
        this.contentFormats = contentFormats;
    }

    public CommercialIntent getCommercialIntent() {
        return commercialIntent;
    }

    public void setCommercialIntent(CommercialIntent commercialIntent) {
        this.commercialIntent = commercialIntent;
    }

    public String getOfferedService() {
        return offeredService;
    }

    public void setOfferedService(String offeredService) {
        this.offeredService = offeredService;
    }

    public PublishingFrequency getPublishingFrequency() {
        return publishingFrequency;
    }

    public void setPublishingFrequency(PublishingFrequency publishingFrequency) {
        this.publishingFrequency = publishingFrequency;
    }

    public String getContentDifferential() {
        return contentDifferential;
    }

    public void setContentDifferential(String contentDifferential) {
        this.contentDifferential = contentDifferential;
    }

    public CurrentSituation getCurrentSituation() {
        return currentSituation;
    }

    public void setCurrentSituation(CurrentSituation currentSituation) {
        this.currentSituation = currentSituation;
    }

    public MainObjective getMainObjective() {
        return mainObjective;
    }

    public void setMainObjective(MainObjective mainObjective) {
        this.mainObjective = mainObjective;
    }

    public VisaTypeInterest getVisaTypeInterest() {
        return visaTypeInterest;
    }

    public void setVisaTypeInterest(VisaTypeInterest visaTypeInterest) {
        this.visaTypeInterest = visaTypeInterest;
    }

    public KnowledgeLevel getKnowledgeLevel() {
        return knowledgeLevel;
    }

    public void setKnowledgeLevel(KnowledgeLevel knowledgeLevel) {
        this.knowledgeLevel = knowledgeLevel;
    }

    public List<String> getCurrentInfoSources() {
        return currentInfoSources;
    }

    public void setCurrentInfoSources(List<String> currentInfoSources) {
        this.currentInfoSources = currentInfoSources;
    }

    public MainDifficulty getMainDifficulty() {
        return mainDifficulty;
    }

    public void setMainDifficulty(MainDifficulty mainDifficulty) {
        this.mainDifficulty = mainDifficulty;
    }

    public PreferredContentType getPreferredContentType() {
        return preferredContentType;
    }

    public void setPreferredContentType(PreferredContentType preferredContentType) {
        this.preferredContentType = preferredContentType;
    }

    public ServiceHiringIntent getServiceHiringIntent() {
        return serviceHiringIntent;
    }

    public void setServiceHiringIntent(ServiceHiringIntent serviceHiringIntent) {
        this.serviceHiringIntent = serviceHiringIntent;
    }

    public ImmigrationTimeframe getImmigrationTimeframe() {
        return immigrationTimeframe;
    }

    public void setImmigrationTimeframe(ImmigrationTimeframe immigrationTimeframe) {
        this.immigrationTimeframe = immigrationTimeframe;
    }

    public String getPlatformExpectation() {
        return platformExpectation;
    }

    public void setPlatformExpectation(String platformExpectation) {
        this.platformExpectation = platformExpectation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
