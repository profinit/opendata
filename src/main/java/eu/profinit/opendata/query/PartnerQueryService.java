package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.EntityType;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class PartnerQueryService {

    @PersistenceContext
    private EntityManager em;

    public Entity findOrCreateEntity(String name, String ico, String dic) {

        Entity found = findEntity(name, ico, dic);
        // Pokud nemame ani jednoho kandidata, musime vytvorit noveho

        // Pokud vracime kandidata, ktery ma neco nevyplnene, doplnime a zmergujeme
        if(found != null) {
            if(dic != null && found.getDic() == null) {
                found.setDic(dic);
            }
            if(ico != null && found.getIco() == null) {
                found.setIco(ico);
            }
            em.merge(found);
        }

        if(found == null) {
            found = new Entity();
            found.setName(name);
            found.setEntityType(EntityType.COMPANY); // TODO: Automatická detekce toho, co je to za EntityType?
            found.setDic(dic);
            found.setIco(ico);
            found.setPublic(false);
            em.persist(found);
        }

        return found;
    }

    public Entity findEntity(String name, String ico, String dic) {
        List<Entity> candidates = new ArrayList<>();
        // Pokud mame ico a dic, zkusime napred podle obou
        // Pokud jsme jeste nic nenalezli, zkusime podle dic
        // Pokud jsme jeste nic nenalezli, zkusime podle ico
        // Mame-li kandidata, vracime ho.

        if(ico != null && dic != null) {
            candidates = em.createNamedQuery("findByICOAndDIC", Entity.class)
                    .setParameter("ico", ico)
                    .setParameter("dic", dic)
                    .getResultList();
        }
        if(candidates.isEmpty() && ico != null) {
            candidates = em.createNamedQuery("findByICO", Entity.class)
                    .setParameter("ico", ico)
                    .getResultList();
        }
        if(candidates.isEmpty() && dic != null) {
            candidates = em.createNamedQuery("findByDIC", Entity.class)
                    .setParameter("dic", ico)
                    .getResultList();
        }

        // Pokud mame jenom jmeno nebo jsme jeste nenasli,
        // hledame podle jmena
        if(candidates.isEmpty()) {
            Entity fromName = findMatchingEntityByName(name);
            if(fromName != null) {
                candidates.add(fromName);
            }
        }

        if(!candidates.isEmpty()) {
            return candidates.get(0);
        }
        return null;
    }

    private Entity findMatchingEntityByName(String name) {
        // TODO: Tady je potreba brat v potaz mozne preklepy, substringy, zkratky apod.
        // Vyber z vice kandidatu a tak
        // Muzeme pak nejak zajistit deduplikaci? Treba v nejakem GUI - databaze si ale pak musi pamatovat,
        // co deduplikovala na co a na jake zaznamy se to vztahuje
        // Pokud mame vic kandidatu, vezmeme zatim prvniho

        String query = name.toLowerCase().trim();
        List<Entity> candidates = em.createNamedQuery("findByName", Entity.class)
                .setParameter("name", "%" + query + "%").getResultList();

        if(!candidates.isEmpty()) {
            return  candidates.get(0);
        }

        return null;
    }
}
