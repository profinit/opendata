package eu.profinit.opendata.test.util;

import eu.profinit.opendata.model.*;
import eu.profinit.opendata.test.DataGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Date;

/**
 * Created by dm on 1/16/16.
 */
@Component
public class DatabaseUtils {

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanRecords() {
        em.createQuery("Delete from UnresolvedRelationship r").executeUpdate();
        em.createQuery("Delete from PartnerListEntry p").executeUpdate();
        em.createQuery("Delete from Record r").executeUpdate();
        em.createQuery("Delete from Entity e where e.public = false").executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRelationshipTestStructure() {
        Entity entity = DataGenerator.getTestMinistry();
        em.persist(entity);
        DataSource ds = DataGenerator.getDataSource(entity);
        em.persist(ds);
        DataInstance di = DataGenerator.getDataInstance(ds);
        em.persist(di);
        Retrieval ret = DataGenerator.getRetrieval(di);
        em.persist(ret);
        Entity partner = DataGenerator.getTestCompany();
        em.persist(partner);

        Record rec = new Record();
        rec.setAuthority(entity);
        rec.setDateCreated(new Date(System.currentTimeMillis()));
        rec.setPartner(partner);
        rec.setMasterId("1");
        rec.setRecordType(RecordType.CONTRACT);
        rec.setCurrency("CZK");
        rec.setRetrieval(ret);
        em.persist(rec);

        UnresolvedRelationship u = new UnresolvedRelationship();
        u.setSavedRecord(rec);
        u.setBoundAuthorityIdentifier("bla");
        u.setRecordType(RecordType.INVOICE);
        u.setSavedRecordIsParent(true);
        em.persist(u);

        Record rec2 = new Record();
        rec2.setDateCreated(new Date(System.currentTimeMillis()));
        rec2.setAuthority(entity);
        rec2.setPartner(partner);
        rec2.setMasterId("2");
        rec2.setRecordType(RecordType.INVOICE);
        rec2.setCurrency("CZK");
        rec2.setRetrieval(ret);
        rec2.setAuthorityIdentifier("bla");
        em.persist(rec2);
    }
}
