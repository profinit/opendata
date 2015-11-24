package eu.profinit.opendata.test;

import eu.profinit.opendata.model.*;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by dm on 11/24/15.
 */
public class DataGenerator {

    static Entity getTestMinistry() {
        Entity entity = new Entity();
        entity.setName("Ministerstvo zpraseného kódu");
        entity.setEntityType(EntityType.MINISTRY);
        entity.setPublic(true);
        return entity;
    }

    static Entity getTestCompany() {
        Entity entity = new Entity();
        entity.setName("Git a. s.");
        entity.setEntityType(EntityType.COMPANY);
        entity.setPublic(false);
        entity.setIco("01234");
        return entity;
    }

    static DataSource getDataSource(Entity forEntity) {
        DataSource ds = new DataSource();
        ds.setEntity(forEntity);
        ds.setRecordType(RecordType.INVOICE);
        ds.setPeriodicity(Periodicity.QUARTERLY);
        ds.setActive(true);
        return ds;
    }

    static DataInstance getDataInstance(DataSource forDataSource) {
        DataInstance di = new DataInstance();
        di.setDataSource(forDataSource);
        di.setFormat("xls");
        di.setPeriodicity(Periodicity.APERIODIC);
        di.setUrl("http://mzk.cz/data");
        return di;
    }

    static Retrieval getRetrieval(DataInstance forDataInstance) {
        Retrieval ret = new Retrieval();
        ret.setDate(new Timestamp(System.currentTimeMillis()));
        ret.setNumRecordsInserted(100);
        ret.setNumBadRecords(2);
        ret.setDataInstance(forDataInstance);
        ret.setSuccess(true);
        return ret;
    }

    static Record getInvoice(Retrieval fromRetrieval, Entity authority, Entity partner) {
        Record record = new Record();
        record.setAuthorityRole(AuthorityRole.CUSTOMER);
        record.setCurrency("CZK");
        record.setDateCreated(new Date(System.currentTimeMillis()));
        record.setMasterId("abcdef");
        record.setAmountCzkWithVat(1_350_000.0);
        record.setRecordType(RecordType.INVOICE);
        record.setRetrieval(fromRetrieval);
        record.setAuthority(authority);
        record.setPartner(partner);
        return record;
    }

    static Record getContract(Retrieval fromRetrieval, Entity authority, Entity partner) {
        Record record = new Record();
        record.setAuthorityRole(AuthorityRole.CUSTOMER);
        record.setCurrency("CZK");
        record.setDateCreated(new Date(System.currentTimeMillis()));
        record.setMasterId("ghijklm");
        record.setAmountCzkWithVat(1_000_000.0);
        record.setRecordType(RecordType.CONTRACT);
        record.setRetrieval(fromRetrieval);
        record.setAuthority(authority);
        record.setPartner(partner);
        return record;
    }
}
