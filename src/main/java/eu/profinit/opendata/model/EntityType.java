package eu.profinit.opendata.model;

import javax.persistence.AttributeConverter;

/**
 * The possible types of an Entity. Currently, only MINISTRY and COMPANY are used and no effort is made to automatically
 * sort Entities into categories.
 */
public enum EntityType {
    MINISTRY,
    INDIVIDUAL,
    COMPANY,
    OTHER;
}


