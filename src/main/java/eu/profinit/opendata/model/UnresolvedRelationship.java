package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.RecordTypeConverter;

import javax.persistence.*;

/**
 * Created by dm on 2/13/16.
 */
@javax.persistence.Entity
@Table(name = "unresolved_relationship", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "unresolved_relationship_unresolved_relationship_id_seq", allocationSize = 1)
public class UnresolvedRelationship {

    private Long unresolvedRelationshipId;
    private Record savedRecord;
    private String boundAuthorityIdentifier;
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
