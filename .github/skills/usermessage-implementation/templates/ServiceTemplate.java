// Template for Service Layer in usermessage feature
// Replace [Resource] with actual resource name (e.g., Conversation, Message)

package br.com.aguideptbr.features.usermessage;

@ApplicationScoped
public class [Resource]Service {

    // ✅ CONSTRUCTOR INJECTION (not field injection)
    private final [Resource]Repository repository;
    private final Logger log;

    public [Resource]Service([Resource]Repository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    // ✅ @Transactional for CREATE operations
    @Transactional
    public [Resource]Response create(Create[Resource]Request request) {
        log.infof("Creating new [resource]: %s", request.getName());

        // 1. BUSINESS VALIDATION
        validateBusinessRules(request);

        // 2. MAP TO ENTITY
        var entity = new [Resource]Model();
        entity.name = request.getName();
        entity.description = request.getDescription();
        // ... other fields

        // 3. PERSIST
        repository.persist(entity);
        log.infof("[Resource] created with ID: %s", entity.id);

        // 4. MAP TO RESPONSE
        return mapToResponse(entity);
    }

    // ✅ NO @Transactional for READ operations
    public List<[Resource]Response> findAll() {
        log.info("Finding all [resources]");
        return repository.findActive()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public [Resource]Response findById(UUID id) {
        log.infof("Finding [resource] by ID: %s", id);

        var entity = repository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(
                    "[Resource] not found with ID: " + id,
                    404
                ));

        // Check if soft deleted
        if (entity.deletedAt != null) {
            throw new WebApplicationException("[Resource] not found", 404);
        }

        return mapToResponse(entity);
    }

    // ✅ @Transactional for UPDATE operations
    @Transactional
    public [Resource]Response update(UUID id, Update[Resource]Request request) {
        log.infof("Updating [resource] ID: %s", id);

        var entity = repository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(
                    "[Resource] not found",
                    404
                ));

        // BUSINESS VALIDATION
        validateUpdateRules(entity, request);

        // UPDATE FIELDS
        entity.name = request.getName();
        entity.description = request.getDescription();
        // updatedAt is handled by @UpdateTimestamp

        repository.persist(entity);
        return mapToResponse(entity);
    }

    // ✅ @Transactional for DELETE operations (SOFT DELETE)
    @Transactional
    public void delete(UUID id) {
        log.infof("Deleting [resource] ID: %s", id);

        var entity = repository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException(
                    "[Resource] not found",
                    404
                ));

        // SOFT DELETE - set deletedAt timestamp
        entity.deletedAt = LocalDateTime.now();
        repository.persist(entity);

        log.infof("[Resource] soft deleted: %s", id);
    }

    // PRIVATE HELPER METHODS

    private void validateBusinessRules(Create[Resource]Request request) {
        // Example: Check for duplicate names
        var existing = repository.findByName(request.getName());
        if (existing != null) {
            throw new WebApplicationException(
                "[Resource] with this name already exists",
                400
            );
        }

        // Additional validations...
    }

    private void validateUpdateRules([Resource]Model entity, Update[Resource]Request request) {
        // Check if already deleted
        if (entity.deletedAt != null) {
            throw new WebApplicationException(
                "Cannot update deleted [resource]",
                400
            );
        }

        // Additional validations...
    }

    private [Resource]Response mapToResponse([Resource]Model entity) {
        return new [Resource]Response(
            entity.id,
            entity.name,
            entity.description,
            entity.createdAt,
            entity.updatedAt
        );
    }
}
