package eu.profinit.opendata.test.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by dm on 1/16/16.
 */
@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanRecords() {
        em.createQuery("Delete from Record r").executeUpdate();
    }
}
