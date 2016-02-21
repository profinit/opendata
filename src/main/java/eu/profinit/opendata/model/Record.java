package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.AuthorityRoleConverter;
import eu.profinit.opendata.model.util.PeriodicityConverter;
import eu.profinit.opendata.model.util.RecordTypeConverter;

import javax.persistence.*;
import java.sql.Date;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by DM on 8. 11. 2015.
 */
@javax.persistence.Entity
@Table(name = "record", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "record_record_id_seq", allocationSize = 1)
@NamedQueries({
        @NamedQuery(name = "findByAuthorityIdAndEntity",
        query = "SELECT OBJECT(r) FROM Record r WHERE r.authorityIdentifier = :authorityIdentifier " +
                "AND r.authority = :authority"),
        @NamedQuery(name = "findByUnresolvedRelationship",
        query = "SELECT OBJECT(r) FROM Record r WHERE r.authorityIdentifier = :authorityIdentifier " +
                "AND r.authority = :authority AND r.recordType = :recordType")
})
public class Record {
    private Double amountCzkWithVat;

    @Basic
    @Column(name = "amount_czk_with_vat")
    public Double getAmountCzkWithVat() {
        return amountCzkWithVat;
    }

    public void setAmountCzkWithVat(Double amountCzkWithVat) {
        this.amountCzkWithVat = amountCzkWithVat;
    }

    private Double amountCzkWithoutVat;

    @Basic
    @Column(name = "amount_czk_without_vat")
    public Double getAmountCzkWithoutVat() {
        return amountCzkWithoutVat;
    }

    public void setAmountCzkWithoutVat(Double amountCzkWithoutVat) {
        this.amountCzkWithoutVat = amountCzkWithoutVat;
    }

    private String authorityIdentifier;

    @Basic
    @Column(name = "authority_identifier")
    public String getAuthorityIdentifier() {
        return authorityIdentifier;
    }

    public void setAuthorityIdentifier(String authorityIdentifier) {
        this.authorityIdentifier = authorityIdentifier;
    }

    private String currency;

    @Basic
    @Column(name = "currency")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private Date dateCreated;

    @Basic
    @Column(name = "date_created")
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    private Date dateOfExpiry;

    @Basic
    @Column(name = "date_of_expiry")
    public Date getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(Date dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }

    private Date dateOfPayment;

    @Basic
    @Column(name = "date_of_payment")
    public Date getDateOfPayment() {
        return dateOfPayment;
    }

    public void setDateOfPayment(Date dateOfPayment) {
        this.dateOfPayment = dateOfPayment;
    }

    private Date dueDate;

    @Basic
    @Column(name = "due_date")
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    private Boolean inEffect;

    @Basic
    @Column(name = "in_effect")
    public Boolean getInEffect() {
        return inEffect;
    }

    public void setInEffect(Boolean inEffect) {
        this.inEffect = inEffect;
    }

    private String masterId;

    @Basic
    @Column(name = "master_id")
    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    private Double originalCurrencyAmount;

    @Basic
    @Column(name = "original_currency_amount")
    public Double getOriginalCurrencyAmount() {
        return originalCurrencyAmount;
    }

    public void setOriginalCurrencyAmount(Double originalCurrencyAmount) {
        this.originalCurrencyAmount = originalCurrencyAmount;
    }

    private String partnerCode;

    @Basic
    @Column(name = "partner_code")
    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    private String budgetCategory;

    @Basic
    @Column(name = "budget_category")
    public String getBudgetCategory() {
        return budgetCategory;
    }

    public void setBudgetCategory(String budgetCategory) {
        this.budgetCategory = budgetCategory;
    }

    private String subject;

    @Basic
    @Column(name = "subject")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    private String variableSymbol;

    @Basic
    @Column(name = "variable_symbol")
    public String getVariableSymbol() {
        return variableSymbol;
    }

    public void setVariableSymbol(String variableSymbol) {
        this.variableSymbol = variableSymbol;
    }

    private Long recordId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pk")
    @Column(name = "record_id")
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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

    private AuthorityRole authorityRole;

    @Convert(converter = AuthorityRoleConverter.class)
    @Column(name = "authority_role")
    public AuthorityRole getAuthorityRole() {
        return authorityRole;
    }

    public void setAuthorityRole(AuthorityRole authorityRole) {
        this.authorityRole = authorityRole;
    }

    private Retrieval retrieval;

    @ManyToOne
    @JoinColumn(name = "retrieval_id", referencedColumnName = "retrieval_id")
    public Retrieval getRetrieval() {
        return retrieval;
    }

    public void setRetrieval(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    private Entity authority;

    @ManyToOne
    @JoinColumn(name = "authority", referencedColumnName = "entity_id")
    public Entity getAuthority() {
        return authority;
    }

    public void setAuthority(Entity authority) {
        this.authority = authority;
    }

    private Entity partner;

    @ManyToOne
    @JoinColumn(name = "partner", referencedColumnName = "entity_id")
    public Entity getPartner() {
        return partner;
    }

    public void setPartner(Entity partner) {
        this.partner = partner;
    }

    private Record parentRecord;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "record_id")
    public Record getParentRecord() {
        return parentRecord;
    }

    public void setParentRecord(Record parentRecord) {
        this.parentRecord = parentRecord;
    }

    private Collection<Record> childRecords;

    private Periodicity periodicity;

    @Convert(converter = PeriodicityConverter.class)
    @Column(name = "periodicity")
    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    @OneToMany(mappedBy = "parentRecord")
    public Collection<Record> getChildRecords() {
        return childRecords;
    }

    public void setChildRecords(Collection<Record> childRecords) {
        this.childRecords = childRecords;
    }

    private Collection<UnresolvedRelationship> unresolvedRelationships;

    @OneToMany(mappedBy = "savedRecord", cascade = CascadeType.PERSIST)
    public Collection<UnresolvedRelationship> getUnresolvedRelationships() {
        return unresolvedRelationships;
    }

    public void setUnresolvedRelationships(Collection<UnresolvedRelationship> unresolvedRelationships) {
        this.unresolvedRelationships = unresolvedRelationships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        if (!Objects.equals(recordId, record.recordId)) return false;
        if (amountCzkWithVat != null ? !amountCzkWithVat.equals(record.amountCzkWithVat) : record.amountCzkWithVat != null)
            return false;
        if (amountCzkWithoutVat != null ? !amountCzkWithoutVat.equals(record.amountCzkWithoutVat) : record.amountCzkWithoutVat != null)
            return false;
        if (authorityIdentifier != null ? !authorityIdentifier.equals(record.authorityIdentifier) : record.authorityIdentifier != null)
            return false;
        if (authorityRole != null ? !authorityRole.equals(record.authorityRole) : record.authorityRole != null)
            return false;
        if (currency != null ? !currency.equals(record.currency) : record.currency != null) return false;
        if (dateCreated != null ? !dateCreated.equals(record.dateCreated) : record.dateCreated != null) return false;
        if (dateOfExpiry != null ? !dateOfExpiry.equals(record.dateOfExpiry) : record.dateOfExpiry != null)
            return false;
        if (dateOfPayment != null ? !dateOfPayment.equals(record.dateOfPayment) : record.dateOfPayment != null)
            return false;
        if (dueDate != null ? !dueDate.equals(record.dueDate) : record.dueDate != null) return false;
        if (inEffect != null ? !inEffect.equals(record.inEffect) : record.inEffect != null) return false;
        if (masterId != null ? !masterId.equals(record.masterId) : record.masterId != null) return false;
        if (originalCurrencyAmount != null ? !originalCurrencyAmount.equals(record.originalCurrencyAmount) : record.originalCurrencyAmount != null)
            return false;
        if (partnerCode != null ? !partnerCode.equals(record.partnerCode) : record.partnerCode != null) return false;
        if (recordType != null ? !recordType.equals(record.recordType) : record.recordType != null) return false;
        if (subject != null ? !subject.equals(record.subject) : record.subject != null) return false;
        if (variableSymbol != null ? !variableSymbol.equals(record.variableSymbol) : record.variableSymbol != null)
            return false;
        if (retrieval != null ? !retrieval.equals(record.retrieval) : record.retrieval != null) return false;
        if (authority != null ? !authority.equals(record.authority) : record.authority != null) return false;
        if (partner != null ? !partner.equals(record.partner) : record.partner != null) return false;
        if (parentRecord != null ? !parentRecord.equals(record.parentRecord) : record.parentRecord != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = amountCzkWithVat != null ? amountCzkWithVat.hashCode() : 0;
        result = 31 * result + (amountCzkWithoutVat != null ? amountCzkWithoutVat.hashCode() : 0);
        result = 31 * result + (authorityIdentifier != null ? authorityIdentifier.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
        result = 31 * result + (dateOfExpiry != null ? dateOfExpiry.hashCode() : 0);
        result = 31 * result + (dateOfPayment != null ? dateOfPayment.hashCode() : 0);
        result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
        result = 31 * result + (inEffect != null ? inEffect.hashCode() : 0);
        result = 31 * result + (masterId != null ? masterId.hashCode() : 0);
        result = 31 * result + (originalCurrencyAmount != null ? originalCurrencyAmount.hashCode() : 0);
        result = 31 * result + (partnerCode != null ? partnerCode.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (variableSymbol != null ? variableSymbol.hashCode() : 0);
        result = 31 * result + recordId.intValue();
        result = 31 * result + (recordType != null ? recordType.hashCode() : 0);
        result = 31 * result + (authorityRole != null ? authorityRole.hashCode() : 0);
        result = 31 * result + (retrieval != null ? retrieval.hashCode() : 0);
        result = 31 * result + (authority != null ? authority.hashCode() : 0);
        result = 31 * result + (partner != null ? partner.hashCode() : 0);
        result = 31 * result + (parentRecord != null ? parentRecord.hashCode() : 0);
        return result;
    }

}
