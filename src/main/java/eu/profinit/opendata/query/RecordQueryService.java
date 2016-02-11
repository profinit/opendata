package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by dm on 1/31/16.
 */
@Component
public class RecordQueryService {

    @PersistenceContext
    private EntityManager em;

    public List<Record> findRecordsByFilter(Map<String, String> filter, Retrieval currentRetrieval) {
        // Look in the retrieval first
        Collection<Record> finishedRecords = currentRetrieval.getRecords();
        Stream<Record> stream = finishedRecords.stream();
        for(String key : filter.keySet()) {
            stream = stream.filter(new RecordPropertyPredicate(key, filter.get(key)));
        }
        List<Record> found = stream.collect(Collectors.toList());

        // Then try older records
        if(found.isEmpty()) {
            found = findRecordsByFilter(filter);
        }

        return found;

    }

    public List<Record> findRecordsByFilter(Map<String, String> filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Record> qr = cb.createQuery(Record.class);
        Root<Record> root = qr.from(Record.class);
        qr.select(root);

        for(String key : filter.keySet()) {
            qr = qr.where(cb.equal(root.get(key), filter.get(key)));
        }

        return em.createQuery(qr).getResultList();
    }

    static class RecordPropertyPredicate implements Predicate<Record> {

        String property;
        String value;

        public RecordPropertyPredicate(String property, String value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public boolean test(Record record) {
            switch(property) {
                // TODO: There should be all the others here
                case "authorityIdentifier":
                    return record.getAuthorityIdentifier() != null
                            && record.getAuthorityIdentifier().equals(value);
                default:
                    return false;
            }
        }
    }
}
