package eu.profinit.opendata.common;

import javax.persistence.EntityManager;

/**
 * Created by dm on 12/19/15.
 */
public interface PersistenceAware {
    EntityManager getEm();
    void setEm(EntityManager em);
}
