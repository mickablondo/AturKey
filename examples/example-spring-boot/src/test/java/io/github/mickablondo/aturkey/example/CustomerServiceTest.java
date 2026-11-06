package io.github.mickablondo.aturkey.example;

import io.github.mickablondo.aturkey.AturKeyUpdater;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomerServiceTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AturKeyUpdater aturKeyUpdater;

    @Test
    void autoConfiguresAnUpdaterBean() {
        assertThat(aturKeyUpdater).isNotNull();
    }

    @Test
    void updatesCustomerByEmailInTheCurrentTransaction() {
        // 1 - création d'un objet client
        Customer stored = new Customer("customer@example.com", "nom de jeune fille");

        // 2 - insertion du client dans la base de données
        entityManager.persist(stored);
        entityManager.flush();

        Long id = stored.getId();
        long version = stored.getVersion();

        String updatedName = "nom de mariée";

        // 3 - création d'un nouvel objet client avec le même email mais un nom différent
        // la recherche se fait par mail car le mail est annoté d'un AturKey
        Customer updated = customerService.update(new Customer("customer@example.com", updatedName));

        // 4 - Vérifications
        assertThat(updated).isSameAs(stored);
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getVersion()).isEqualTo(version);
        assertThat(updated.getName()).isEqualTo(updatedName);
    }
}
