package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.EntityType;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class PartnerQueryService {

    @PersistenceContext(unitName = "postgres")
    private EntityManager em;

    public Entity findOrCreateEntity(String name, String ico, String dic) {
        // Automatická detekce toho, co je to zač?
        return null;
    }

    public Entity findEntity(String name, String ico, String dic) {
        return null;
    }
}
