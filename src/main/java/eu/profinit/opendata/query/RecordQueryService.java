package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Record;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dm on 1/31/16.
 */
@Component
public class RecordQueryService {

    @PersistenceContext
    private EntityManager em;

    public List<Record> findRecordsByFilter(HashMap<String, String> filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Record> qr = cb.createQuery(Record.class);
        Root<Record> root = qr.from(Record.class);
        qr.select(root);

        for(String key : filter.keySet()) {
            qr = qr.where(cb.equal(root.get(key), filter.get(key)));
        }

        return em.createQuery(qr).getResultList();
    }
}
