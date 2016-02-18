package eu.profinit.opendata.control;

/**
 * A component that runs bootstrapping logic on already extracted and persisted data. Currently only links parent and
 * child records based on persisted UnresolvedRelationships
 * @see eu.profinit.opendata.model.UnresolvedRelationship
 */
public interface RelationshipResolver {
    /**
     * Retrieves UnresolvedRelationships from the database and tries to find actual parent and child records. If found,
     * they are linked and the UnresolvedRelationship object is deleted from the database.
     */
    void resolveRecordParentRelationships();
}
