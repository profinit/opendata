package eu.profinit.opendata.control.impl;

import eu.profinit.opendata.control.RelationshipResolver;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.UnresolvedRelationship;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


/**
 * Created by dm on 2/13/16.
 */
@Component
public class RelationshipResolverImpl implements RelationshipResolver {

    @PersistenceContext
    private EntityManager em;

    private Logger logger = LogManager.getLogger(RelationshipResolverImpl.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resolveRecordParentRelationships() {
        List<UnresolvedRelationship> unresolvedRelationships = em.createQuery("Select u from UnresolvedRelationship u",
                UnresolvedRelationship.class).getResultList();

        logger.info("Got list of " + unresolvedRelationships.size() + " unresolved relationships");
        int resolved = 0;
        for(UnresolvedRelationship u : unresolvedRelationships) {
            List<Record> candidates = em.createNamedQuery("findByUnresolvedRelationship", Record.class)
                    .setParameter("authorityIdentifier", u.getBoundAuthorityIdentifier())
                    .setParameter("authority", u.getSavedRecord().getAuthority())
                    .setParameter("recordType", u.getRecordType())
                    .getResultList();

            if(!candidates.isEmpty()) {
                if(candidates.size() > 1) {
                    logger.warn("There is more than one candidate for the unresolved relationship between record " +
                        u.getSavedRecord().getRecordId() + " and identifier " + u.getBoundAuthorityIdentifier());
                }
                Record toJoin = candidates.get(0);
                logger.trace("Linking records " + u.getSavedRecord().getRecordId() + " and " + toJoin.getRecordId());
                if(u.getSavedRecordIsParent()) {
                    toJoin.setParentRecord(u.getSavedRecord());
                    em.merge(toJoin);
                }
                else {
                    u.getSavedRecord().setParentRecord(toJoin);
                    em.merge(u.getSavedRecord());
                }
                resolved++;
                em.remove(u);
            }
        }
        logger.info("Finished resolving relationships, linked " + resolved + " pairs of records.");
    }
}
