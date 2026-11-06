package io.github.mickablondo.aturkey;

import io.github.mickablondo.aturkey.exception.AturKeyNonUniqueException;
import io.github.mickablondo.aturkey.exception.AturKeyNotFoundException;
import io.github.mickablondo.aturkey.exception.AturKeyNullValueException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Objects;

/**
 * Resolves an entity by its field annotated with {@link AturKey}.
 */
public final class AturKeyResolver {
    private final EntityManager entityManager;

    /**
     * Creates a resolver using the supplied persistence context.
     *
     * @param entityManager persistence context used to query the entity
     */
    public AturKeyResolver(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    /**
     * Finds the unique managed entity matching the incoming entity's business key.
     *
     * @param entity incoming entity carrying the business-key value
     * @param <T> entity type
     * @return the matching managed entity
     * @throws AturKeyNotFoundException if no entity matches
     * @throws AturKeyNonUniqueException if multiple entities match
     */
    public <T> T resolve(T entity) {
        AturKeyMetadata metadata = AturKeyMetadata.forInstance(entity);
        Object keyValue = metadata.readKey(entity);
        if (keyValue == null) {
            throw new AturKeyNullValueException(metadata.getEntityClass().getName(), metadata.getKeyField().getName());
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
        Root<?> root = criteriaQuery.from(metadata.getEntityClass());
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get(metadata.getKeyField().getName()), keyValue));

        TypedQuery<Object> query = entityManager.createQuery(criteriaQuery);
        query.setMaxResults(2);
        List<Object> matches = query.getResultList();
        if (matches.isEmpty()) {
            throw new AturKeyNotFoundException("No " + metadata.getEntityClass().getName() + " entity matches AturKey field '"
                    + metadata.getKeyField().getName() + "' with value '" + keyValue + "'.");
        }
        if (matches.size() > 1) {
            throw new AturKeyNonUniqueException(
                    metadata.getEntityClass().getName(), metadata.getKeyField().getName(), keyValue);
        }

        @SuppressWarnings("unchecked")
        T resolved = (T) matches.getFirst();
        return resolved;
    }
}
