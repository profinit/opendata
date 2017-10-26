package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.EntityType;
import eu.profinit.opendata.model.PartnerListEntry;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static eu.profinit.opendata.common.Util.isNullOrEmpty;

/**
 * A service that provides a single access point for querying and inserting partner Entities.
 */
@Component
public class PartnerQueryService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Creates a PartnerListEntry for a specified authority and partner under the specified code. If an identical
     * entry is already present in the database, it is returned and nothing is inserted.
     * @param authority
     * @param partner
     * @param code
     * @return The inserted PartnerListEntry, or an identical one already in the database.
     */
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

    /**
     * Retrieves a partner Entity by a reference code saved as a PartnerListEntry.
     * @param authority The authority that has published information about the partner under the specified code.
     * @param code The code associated with the searched partner.
     * @return An Entity, if one is found, or null.
     */
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

    /**
     * Attempts to find an Entity saved in the database with attributes equal to the parameters. The
     * parameters can be null, though not all at the same time. If no Entity is found, a new one is
     * inserted into the database.
     * @param name The name of the entity. It is normalized before being applied as a filter.
     * @param ico The tax identification number of the entity. Must already be in the correct 8-digit format.
     * @param dic
     * @return A retrieved or newly created Entity with the specified atttributes. If more than one Entity is
     * found, only the first result is returned.
     */
    public Entity findOrCreateEntity(String name, String ico, String dic) {
        Entity found = findEntity(name, ico, dic);

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

    /**
     * Attempts to find an Entity saved in the database with attributes equal to the parameters. The
     * parameters can be null, though not all at the same time.
     * @param name The name of the entity. It is normalized before being applied as a filter.
     * @param ico The tax identification number of the entity. Must already be in the correct 8-digit format.
     * @param dic
     * @return A retrieved Entity with the specified atttributes, or null if none is found. If more than
     * one Entity is found, the first result is returned.
     */
    public Entity findEntity(String name, String ico, String dic) {
        List<Entity> candidates = new ArrayList<>();
        // Pokud mame ico a dic, zkusime napred podle obou
        // Pokud jsme jeste nic nenalezli, zkusime podle dic
        // Pokud jsme jeste nic nenalezli, zkusime podle ico
        // Mame-li kandidata, vracime ho.

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

        // Pokud mame jenom jmeno nebo jsme jeste nenasli,
        // hledame podle jmena
        if(candidates.isEmpty() && name != null) {
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

    /**
     * Attempts to find a saved in the database by name only. This method will normalize the name to avoid
     * false negatives as much as possible.
     * @param name The name of the entity. It is normalized before being applied as a filter.
     * @return An Entity, if one is found, or null. If more than one Entity is found, only the first result
     * is returned.
     */
    private Entity findMatchingEntityByName(String name) {
        String query = normalizeEntityName(name);
        List<Entity> candidates = em.createNamedQuery("findByName", Entity.class)
                .setParameter("name", query).getResultList();

        if(!candidates.isEmpty()) {
            return  candidates.get(0);
        }

        return null;
    }

    /**
     * Normalizes an Entity name to a common format. The method is designed for company names, academic
     * titles are not taken into account. The name is converted to uppercase, cleaned of extraneous
     * whitespace, and abbreviations are converted to a single format.
     * @param name The name to be normalized.
     * @return The normalized name, suitable for insertion into the database.
     */
    public String normalizeEntityName(final String name) {

        if(name == null) {
            return null;
        }

        String normalizedName = name;
        
        normalizedName = normalizedName.toUpperCase();
        normalizedName = normalizedName.replaceAll("\\W+V LIKVIDACI\\W+", "");
        String[] abbreviations = {"SRO", "AS", "VOS", "SP", "VVI", "KS"};
        for(String abbr : abbreviations) {
            normalizedName = normalizeAbbreviation(normalizedName, abbr);
        }
        normalizedName = normalizedName.replaceAll("kom( )?\\.( )?spol( )?\\.", ", K. S.");
        //TODO: Akademicke tituly?
        normalizedName = normalizedName.replaceAll("^\\d{2,}\\w", "");
        normalizedName = normalizedName.replaceAll("\\s+", " ");
        normalizedName = normalizedName.trim();

        return normalizedName;
    }

    private String normalizeAbbreviation(String name, String abbr) {
        String regex = createRegexForAbbreviation(abbr);
        StringBuilder replaceBy = new StringBuilder(", ");
        for(int i = 0; i < abbr.length(); i++) {
            replaceBy.append(abbr.charAt(i)).append(".");
            if(i < abbr.length() - 1) {
                replaceBy.append(" ");
            }
        }
        return name.replaceAll(regex, replaceBy.toString());
    }

    private String createRegexForAbbreviation(String abbreviation) {
        StringBuilder result = new StringBuilder("(,)?( )?(SPOL\\.)?( )?");
        for(int i = 0; i < abbreviation.length(); i++) {
            result.append(abbreviation.charAt(i));
            result.append("(\\. |\\.| | \\.)");
        }
        result.append("?");
        return result.toString();
    }

}
