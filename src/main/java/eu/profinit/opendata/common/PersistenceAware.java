package eu.profinit.opendata.common;

import javax.persistence.EntityManager;

/**
 * A component that allows an EntityManager to be set explicitly. Only used for mocking.
 */
public interface PersistenceAware {
    EntityManager getEm();
    void setEm(EntityManager em);
}
