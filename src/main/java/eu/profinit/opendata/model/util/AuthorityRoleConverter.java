package eu.profinit.opendata.model.util;

import eu.profinit.opendata.model.AuthorityRole;

import javax.persistence.AttributeConverter;

/**
 * Created by DM on 11. 11. 2015.
 */
public class AuthorityRoleConverter implements AttributeConverter<AuthorityRole, String> {
    @Override
    public String convertToDatabaseColumn(AuthorityRole authorityRole) {
        if (authorityRole != null) {
            return authorityRole.name().toLowerCase();
        } else return null;
    }

    @Override
    public AuthorityRole convertToEntityAttribute(String s) {
        if(s == null) {
            return null;
        }
        return AuthorityRole.valueOf(s.toUpperCase());
    }
}
