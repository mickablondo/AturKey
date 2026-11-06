package io.github.mickablondo.aturkey.spring.boot.autoconfigure;

import io.github.mickablondo.aturkey.AturKeyUpdater;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures an {@link AturKeyUpdater} when a JPA persistence context is available.
 */
@AutoConfiguration(afterName = "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration")
@ConditionalOnClass({EntityManager.class, AturKeyUpdater.class})
@ConditionalOnBean(EntityManagerFactory.class)
public class AturKeyAutoConfiguration {
    /**
     * Creates the updater backed by the application's transaction-aware entity manager.
     *
     * @param entityManager entity manager provided by the JPA integration
     * @return the updater bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AturKeyUpdater aturKeyUpdater(EntityManager entityManager) {
        return new AturKeyUpdater(entityManager);
    }
}
