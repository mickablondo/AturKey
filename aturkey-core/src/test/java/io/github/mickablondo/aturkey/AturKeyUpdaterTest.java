package io.github.mickablondo.aturkey;

import io.github.mickablondo.aturkey.exception.AturKeyConfigurationException;
import io.github.mickablondo.aturkey.exception.AturKeyNonUniqueException;
import io.github.mickablondo.aturkey.exception.AturKeyNotFoundException;
import io.github.mickablondo.aturkey.exception.AturKeyNullValueException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AturKeyUpdaterTest {
    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setUpEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aturkey-test");
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    @Test
    void throwsWhenNoEntityMatches() {
        inTransaction(entityManager -> assertThatThrownBy(
                () -> new AturKeyResolver(entityManager).resolve(product("missing", "new")))
                .isInstanceOf(AturKeyNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("sku")
                .hasMessageContaining("missing"));
    }

    @Test
    void resolvesTheOnlyMatchingEntity() {
        inTransaction(entityManager -> {
            Product stored = product("one", "stored");
            entityManager.persist(stored);
            entityManager.flush();

            Product resolved = new AturKeyResolver(entityManager).resolve(product("one", "incoming"));

            assertThat(resolved).isSameAs(stored);
        });
    }

    @Test
    void throwsWhenSeveralEntitiesMatch() {
        inTransaction(entityManager -> {
            entityManager.persist(product("duplicate", "first"));
            entityManager.persist(product("duplicate", "second"));
            entityManager.flush();

            assertThatThrownBy(() -> new AturKeyResolver(entityManager).resolve(product("duplicate", "incoming")))
                    .isInstanceOfSatisfying(AturKeyNonUniqueException.class, exception -> {
                        assertThat(exception.getEntityClassName()).contains("AturKeyUpdaterTest$Product");
                        assertThat(exception.getFieldName()).isEqualTo("sku");
                        assertThat(exception.getKeyValue()).isEqualTo("duplicate");
                    });
        });
    }

    @Test
    void rejectsANullKeyBeforeQuerying() {
        inTransaction(entityManager -> assertThatThrownBy(
                () -> new AturKeyResolver(entityManager).resolve(product(null, "incoming")))
                .isInstanceOf(AturKeyNullValueException.class)
                .hasMessageContaining("AturKey"));
    }

    @Test
    void rejectsEntitiesWithMoreThanOneAturKey() {
        inTransaction(entityManager -> assertThatThrownBy(
                () -> new AturKeyResolver(entityManager).resolve(new MisconfiguredProduct()))
                .isInstanceOf(AturKeyConfigurationException.class)
                .hasMessageContaining("MisconfiguredProduct"));
    }

    @Test
    void resolvesProxySubclassesUsingTheirEntityClass() {
        inTransaction(entityManager -> {
            Product stored = product("proxy", "stored");
            entityManager.persist(stored);
            entityManager.flush();

            Product resolved = new AturKeyResolver(entityManager).resolve(new ProductProxy("proxy"));

            assertThat(resolved).isSameAs(stored);
        });
    }

    @Test
    void updatesValuesWithoutReplacingIdOrVersionAndCopiesNullByDefault() {
        inTransaction(entityManager -> {
            Product stored = product("update", "stored");
            stored.description = "existing";
            entityManager.persist(stored);
            entityManager.flush();
            Long id = stored.id;

            Product incoming = product("update", "updated");
            incoming.description = null;
            incoming.version = 900;

            Product updated = new AturKeyUpdater(entityManager).update(incoming);

            assertThat(updated).isSameAs(stored);
            assertThat(updated.id).isEqualTo(id);
            assertThat(updated.version).isNotEqualTo(900);
            assertThat(updated.name).isEqualTo("updated");
            assertThat(updated.description).isNull();
        });
    }

    @Test
    void ignoreNullLikeAturOptionPreservesExistingValues() {
        inTransaction(entityManager -> {
            IgnoreNullProduct stored = new IgnoreNullProduct();
            stored.sku = "ignore-null";
            stored.description = "keep";
            entityManager.persist(stored);
            entityManager.flush();

            IgnoreNullProduct incoming = new IgnoreNullProduct();
            incoming.sku = "ignore-null";
            incoming.description = null;

            IgnoreNullProduct updated = new AturKeyUpdater(entityManager).update(incoming);

            assertThat(updated.description).isEqualTo("keep");
        });
    }

    @Test
    void doesNotCopyIdentifierOrVersionWithPropertyAccess() {
        inTransaction(entityManager -> {
            PropertyAccessProduct stored = new PropertyAccessProduct();
            stored.sku = "property-access";
            stored.name = "stored";
            entityManager.persist(stored);
            entityManager.flush();
            Long id = stored.id;
            int version = stored.version;

            PropertyAccessProduct incoming = new PropertyAccessProduct();
            incoming.sku = "property-access";
            incoming.name = "updated";
            incoming.id = 900L;
            incoming.version = 900;

            PropertyAccessProduct updated = new AturKeyUpdater(entityManager).update(incoming);

            assertThat(updated.id).isEqualTo(id);
            assertThat(updated.version).isEqualTo(version);
            assertThat(updated.name).isEqualTo("updated");
        });
    }

    private static Product product(String sku, String name) {
        Product product = new Product();
        product.sku = sku;
        product.name = name;
        return product;
    }

    private static void inTransaction(EntityManagerConsumer consumer) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            consumer.accept(entityManager);
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    @FunctionalInterface
    private interface EntityManagerConsumer {
        void accept(EntityManager entityManager);
    }

    @jakarta.persistence.Entity(name = "Product")
    public static class Product {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue
        private Long id;

        @jakarta.persistence.Version
        private int version;

        @AturKey
        private String sku;

        private String name;
        private String description;
    }

    public static class ProductProxy extends Product {
        ProductProxy(String sku) {
            super.sku = sku;
        }
    }

    @jakarta.persistence.Entity(name = "IgnoreNullProduct")
    public static class IgnoreNullProduct {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue
        private Long id;

        @AturKey(ignoreNullLikeAtur = true)
        private String sku;

        private String description;
    }

    @jakarta.persistence.Entity(name = "MisconfiguredProduct")
    public static class MisconfiguredProduct {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue
        private Long id;

        @AturKey
        private String firstKey;

        @AturKey
        private String secondKey;
    }

    @jakarta.persistence.Entity(name = "PropertyAccessProduct")
    @jakarta.persistence.Access(jakarta.persistence.AccessType.PROPERTY)
    public static class PropertyAccessProduct {
        private Long id;
        private int version;

        @AturKey
        private String sku;

        private String name;

        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @jakarta.persistence.Version
        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
