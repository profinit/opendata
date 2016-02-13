package eu.profinit.opendata.control.impl;

import eu.profinit.opendata.control.RelationshipResolver;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.UnresolvedRelationship;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by dm on 2/13/16.
 */
public class RelationshipResolverImpl implements RelationshipResolver {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void resolveRecordParentRelationships() {
        List<UnresolvedRelationship> unresolvedRelationships = em.createQuery("Select u from UnresolvedRelationship u",
                UnresolvedRelationship.class).getResultList();

        for(UnresolvedRelationship u : unresolvedRelationships) {
            em.createNamedQuery("findByAuthorityIdAndEntity", Record.class)
                    .setParameter("authorityIdentifier", u.getBoundAuthorityIdentifier())
                    .setParameter("authority", u.getSavedRecord().getAuthority());
        }
    }
}
