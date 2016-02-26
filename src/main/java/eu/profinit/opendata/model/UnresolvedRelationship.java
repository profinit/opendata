package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.RecordTypeConverter;

import javax.persistence.*;

/**
 * A placeholder entry for a parent-child relationship between two Records where one of the sides is not yet present in
 * the database. Used for all documents that contain parent or child record identifiers because the order of processing
 * of data instances cannot be guaranteed. One of the records may not even have been published at all at the time of
 * processing.
 *
 * Resolving relationships is not part of the extraction logic, a special service is run at the end of each DataSource
 * processing cycle.
 * @see eu.profinit.opendata.control.RelationshipResolver
 */
@javax.persistence.Entity
@Table(name = "unresolved_relationship", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "unresolved_relationship_unresolved_relationship_id_seq", allocationSize = 1)
public class UnresolvedRelationship {

    /** The application's primary key */
    private Long unresolvedRelationshipId;

    /** The side of the relationship that is saved in the database */
    private Record savedRecord;

    /** The authority identifier of the record that is to be the other side of the relationship. */
    private String boundAuthorityIdentifier;

    /** Indicates which side of the relationship is the parent record. */
    private Boolean savedRecordIsParent;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pk")
    @Column(name = "unresolved_relationship_id")
    public Long getUnresolvedRelationshipId() {
        return unresolvedRelationshipId;
    }

    public void setUnresolvedRelationshipId(Long unresolvedRelationshipId) {
        this.unresolvedRelationshipId = unresolvedRelationshipId;
    }

    @ManyToOne
    @JoinColumn(name = "saved_record_id", referencedColumnName = "record_id")
    public Record getSavedRecord() {
        return savedRecord;
    }

    public void setSavedRecord(Record savedRecord) {
        this.savedRecord = savedRecord;
    }

    @Basic
    @Column(name = "bound_authority_identifier")
    public String getBoundAuthorityIdentifier() {
        return boundAuthorityIdentifier;
    }

    public void setBoundAuthorityIdentifier(String boundAuthorityIdentifier) {
        this.boundAuthorityIdentifier = boundAuthorityIdentifier;
    }

    @Basic
    @Column(name = "saved_record_is_parent")
    public Boolean getSavedRecordIsParent() {
        return savedRecordIsParent;
    }

    public void setSavedRecordIsParent(Boolean savedRecordIsParent) {
        this.savedRecordIsParent = savedRecordIsParent;
    }

    private RecordType recordType;

    @Convert(converter = RecordTypeConverter.class)
    @Column(name = "record_type")
    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result += 31 * result + (savedRecord != null ? savedRecord.hashCode() : 0);
        result += 31 * result + (boundAuthorityIdentifier != null ? boundAuthorityIdentifier.hashCode() : 0);
        result += 31 * result + (unresolvedRelationshipId != null ? unresolvedRelationshipId : 0);
        result += 31 * result + (savedRecordIsParent != null? savedRecordIsParent.hashCode() : 0);
        result += 31 * result + (recordType != null? recordType.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnresolvedRelationship other = (UnresolvedRelationship) o;
        if(!other.getSavedRecord().equals(getSavedRecord())) return false;
        if(!other.getBoundAuthorityIdentifier().equals(getBoundAuthorityIdentifier())) return false;
        if(!other.getUnresolvedRelationshipId().equals(getUnresolvedRelationshipId())) return false;
        if(!other.getSavedRecordIsParent().equals(getSavedRecordIsParent())) return false;
        if(!other.getRecordType().equals(getRecordType())) return false;

        return true;
    }
}
