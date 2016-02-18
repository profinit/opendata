package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.EntityType;
import eu.profinit.opendata.model.PartnerListEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static eu.profinit.opendata.common.Util.isNullOrEmpty;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class PartnerQueryService {

    @PersistenceContext
    private EntityManager em;

    private Logger logger = LogManager.getLogger(PartnerQueryService.class);

    public PartnerListEntry findOrCreatePartnerListEntry(Entity authority, Entity partner, String code) {
        List<PartnerListEntry> found = em.createNamedQuery("findByAuthorityAndCode", PartnerListEntry.class)
                                                .setParameter("authority", authority)
                                                .setParameter("code", code)
                                                .getResultList();

        if(found.isEmpty()) {
            PartnerListEntry result = new PartnerListEntry();
            result.setAuthority(authority);
            result.setPartner(partner);
            result.setCode(code);
            em.persist(result);
            return result;
        }
        else return found.get(0);
    }

    public Entity findFromPartnerList(Entity authority, String code) {
        List<PartnerListEntry> found = em.createNamedQuery("findByAuthorityAndCode", PartnerListEntry.class)
                .setParameter("authority", authority)
                .setParameter("code", code)
                .getResultList();

        if(!found.isEmpty()) {
            return found.get(0).getPartner();
        }
        return null;
    }

    public Entity findOrCreateEntity(String name, String ico, String dic) {
        //logger.trace("Calling findEntity");
        Entity found = findEntity(name, ico, dic);
        // Pokud nemame ani jednoho kandidata, musime vytvorit noveho

        // Pokud vracime kandidata, ktery ma neco nevyplnene, doplnime a zmergujeme
        if(found != null) {
            if(!isNullOrEmpty(dic) && found.getDic() == null) {
                found.setDic(dic);
            }
            if(!isNullOrEmpty(ico) && found.getIco() == null) {
                found.setIco(ico);
            }
            em.merge(found);
        }

        if(found == null) {
            found = new Entity();
            found.setName(normalizeEntityName(name));
            found.setEntityType(EntityType.COMPANY);
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

        //logger.trace("Calling query");
        if(!isNullOrEmpty(ico) && !isNullOrEmpty(dic)) {
            candidates = em.createNamedQuery("findByICOAndDIC", Entity.class)
                    .setParameter("ico", ico)
                    .setParameter("dic", dic)
                    .getResultList();
        }
        if(candidates.isEmpty() && !isNullOrEmpty(ico)) {
            candidates = em.createNamedQuery("findByICO", Entity.class)
                    .setParameter("ico", ico)
                    .getResultList();
        }
        if(candidates.isEmpty() && !isNullOrEmpty(dic)) {
            candidates = em.createNamedQuery("findByDIC", Entity.class)
                    .setParameter("dic", ico)
                    .getResultList();
        }
        //logger.trace("Entity query completed with " + candidates.size() + " candidates.");

        // Pokud mame jenom jmeno nebo jsme jeste nenasli,
        // hledame podle jmena
        if(candidates.isEmpty()) {
            Entity fromName = findMatchingEntityByName(normalizeEntityName(name));
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
        String query = normalizeEntityName(name);
        List<Entity> candidates = em.createNamedQuery("findByName", Entity.class)
                .setParameter("name", query).getResultList();

        if(!candidates.isEmpty()) {
            return  candidates.get(0);
        }

        return null;
    }

    public String normalizeEntityName(String name) {

        name = name.toUpperCase();
        name = name.replaceAll("\\W+V LIKVIDACI\\W+", "");
        String[] abbreviations = {"SRO", "AS", "VOS", "SP", "VVI", "KS"};
        for(String abbr : abbreviations) {
            name = normalizeAbbreviation(name, abbr);
        }
        name = name.replaceAll("kom( )?\\.( )?spol( )?\\.", ", K. S.");
        //TODO: Akademicke tituly?
        name = name.replaceAll("^\\d{2,}\\w", "");
        name = name.replaceAll("\\s+", " ");
        name = name.trim();

        return name;
    }

    private String normalizeAbbreviation(String name, String abbr) {
        String regex = createRegexForAbbreviation(abbr);
        String replaceBy = ", ";
        for(int i = 0; i < abbr.length(); i++) {
            replaceBy += abbr.charAt(i) + ".";
            if(i < abbr.length() - 1) {
                replaceBy += " ";
            }
        }
        name = name.replaceAll(regex, replaceBy);
        return name;
    }

    private String createRegexForAbbreviation(String abbreviation) {
        String result = "(,)?( )?(SPOL\\.)?( )?";
        for(int i = 0; i < abbreviation.length(); i++) {
            result += abbreviation.charAt(i);
            result += "(\\. |\\.| | \\.)";
        }
        result += "?";
        return result;
    }

}
