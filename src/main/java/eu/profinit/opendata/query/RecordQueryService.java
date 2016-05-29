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
 * A service for querying records in the database. Isn't designed as a single access point to Records and
 * other components may query the database directly. The queries are run on the database, but also on Records
 * saved in a current Retrieval and not yet persisted.
 */
@Component
public class RecordQueryService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Finds records in the database and/or in the specified Retrieval. Currently only supports querying by the
     * "authorityIdentifier" field.
     * @param filter A map of attribute-value pairs to be used as filters.
     * @param currentRetrieval The Retrieval to be searched in along with the database.
     * @return A list of found records.
     */
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

    /**
     * Finds records in the database. Supports querying by any String attribute.
     * @param filter A map of attribute-value pairs to be used as filters.
     * @return A list of found records.
     */
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
                case "authorityIdentifier":
                    return record.getAuthorityIdentifier() != null
                            && record.getAuthorityIdentifier().equals(value);
                case "budgetCategory":
                    return record.getBudgetCategory() != null
                            && record.getBudgetCategory().equals(value);
                case "subject":
                    return record.getSubject() != null
                            && record.getSubject().equals(value);
                default:
                    return false;
            }
        }
    }
}
