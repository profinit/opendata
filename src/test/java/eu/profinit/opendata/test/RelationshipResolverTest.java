package eu.profinit.opendata.test;

import eu.profinit.opendata.control.RelationshipResolver;
import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.util.DatabaseUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Date;

/**
 * Created by dm on 2/13/16.
 */
public class RelationshipResolverTest extends ApplicationContextTestCase {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DatabaseUtils databaseUtils;

    @Autowired
    private RelationshipResolver relationshipResolver;

    @Test
    @Transactional
    public void testRelationshipResolver() throws Exception {
        databaseUtils.cleanRecords();
        databaseUtils.createRelationshipTestStructure();

        relationshipResolver.resolveRecordParentRelationships();

        Record rec = em.createQuery("Select r from Record r where r.masterId = '2'", Record.class)
                .getSingleResult();

        assertNotNull(rec.getParentRecord());
        assertEquals("1", rec.getParentRecord().getMasterId());

    }
}
