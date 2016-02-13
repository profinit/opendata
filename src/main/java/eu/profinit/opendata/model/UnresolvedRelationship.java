package eu.profinit.opendata.model;

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

    @Override
    public int hashCode() {
        int result = 0;
        result += 31 * result + (savedRecord != null ? savedRecord.hashCode() : 0);
        result += 31 * result + (boundAuthorityIdentifier != null ? boundAuthorityIdentifier.hashCode() : 0);
        result += 31 * result + (unresolvedRelationshipId != null ? unresolvedRelationshipId : 0);

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

        return true;
    }
}
