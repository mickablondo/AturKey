package io.github.mickablondo.aturkey.example;

import io.github.mickablondo.aturkey.AturKeyUpdater;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final AturKeyUpdater aturKeyUpdater;

    public CustomerService(AturKeyUpdater aturKeyUpdater) {
        this.aturKeyUpdater = aturKeyUpdater;
    }

    @Transactional
    public Customer update(Customer incoming) {
        return aturKeyUpdater.update(incoming);
    }
}
