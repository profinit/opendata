package eu.profinit.opendata.model;

import javax.persistence.*;

/**
 * Created by dm on 2/11/16.
 */
@javax.persistence.Entity
@Table(name = "partner_list_entry", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "partner_list_entry_partner_list_entry_id_seq", allocationSize = 1)
@NamedQueries({
        @NamedQuery(name = "findByAuthorityAndCode",
        query = "SELECT OBJECT(p) FROM PartnerListEntry p WHERE p.authority = :authority AND p.code = :code"),
        @NamedQuery(name = "findByCode",
                query = "SELECT OBJECT(p) FROM PartnerListEntry p WHERE p.code = :code")
})
public class PartnerListEntry {

    private Entity authority;
    private Entity partner;
    private String code;
    private Long partnerListEntryId;

    @ManyToOne
    @JoinColumn(name = "authority_id", referencedColumnName = "entity_id")
    public Entity getAuthority() {
        return authority;
    }

    public void setAuthority(Entity authority) {
        this.authority = authority;
    }

    @ManyToOne
    @JoinColumn(name = "partner_id", referencedColumnName = "entity_id")
    public Entity getPartner() {
        return partner;
    }

    public void setPartner(Entity partner) {
        this.partner = partner;
    }

    @Basic
    @Column(name="code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pk")
    @Column(name = "partner_list_entry_id")
    public Long getPartnerListEntryId() {
        return partnerListEntryId;
    }

    public void setPartnerListEntryId(Long partnerListEntryId) {
        this.partnerListEntryId = partnerListEntryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartnerListEntry other = (PartnerListEntry) o;
        if(!other.getAuthority().equals(getAuthority())) return false;
        if(!other.getPartner().equals(getPartner())) return false;
        if(!other.getPartnerListEntryId().equals(getPartnerListEntryId())) return false;
        if(!other.getCode().equals(getCode())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result += 31 * result + (partnerListEntryId != null ? partnerListEntryId : 0);
        result += 31 * result + (authority != null ? authority.hashCode() : 0);
        result += 31 * result + (partner != null ? partner.hashCode() : 0);
        result += 31 * result + (code != null ? code.hashCode() : 0);

        return result;
    }
}
