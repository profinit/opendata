package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.PeriodicityConverter;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;

/**
 * Created by DM on 8. 11. 2015.
 */
@javax.persistence.Entity
@Table(name = "data_instance", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "data_instance_data_instance_id_seq")
public class DataInstance {
    private String format;
    private Timestamp lastProcessedDate;
    private String url;
    private Long dataInstanceId;
    private DataSource dataSource;
    private Collection<Retrieval> retrievals;
    private Periodicity periodicity;
    private Date expires;
    private Integer lastProcessedRow;
    private String authorityId;
    private String description;

    @Basic
    @Column(name = "format")
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Basic
    @Column(name = "authoridy_id")
    public String getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(String authorityId) {
        this.authorityId = authorityId;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "last_processed_date")
    public Timestamp getLastProcessedDate() {
        return lastProcessedDate;
    }

    public void setLastProcessedDate(Timestamp last_processed_date) {
        this.lastProcessedDate = last_processed_date;
    }

    @Basic
    @Column(name = "expires")
    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @Convert(converter = PeriodicityConverter.class)
    @Column(name = "periodicity")
    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "last_processed_row")
    public Integer getLastProcessedRow() {
        return lastProcessedRow;
    }

    public void setLastProcessedRow(Integer lastProcessedRow) {
        this.lastProcessedRow = lastProcessedRow;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pk")
    @Column(name = "data_instance_id")
    public Long getDataInstanceId() {
        return dataInstanceId;
    }

    public void setDataInstanceId(Long dataInstanceId) {
        this.dataInstanceId = dataInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataInstance that = (DataInstance) o;

        if (dataInstanceId != that.dataInstanceId) return false;
        if (periodicity != null ? !periodicity.equals(that.periodicity) : that.periodicity != null) return false;
        if (lastProcessedDate != null ?
                !lastProcessedDate.equals(that.lastProcessedDate) : that.lastProcessedDate != null) return false;
        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = format != null ? format.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + dataInstanceId.intValue();
        result = 31 * result + periodicity.hashCode();
        result = 31 * result + lastProcessedDate.hashCode();
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "data_source_id", referencedColumnName = "data_source_id", nullable = false)
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @OneToMany(mappedBy = "dataInstance", cascade = CascadeType.REMOVE)
    public Collection<Retrieval> getRetrievals() {
        return retrievals;
    }

    public void setRetrievals(Collection<Retrieval> retrievals) {
        this.retrievals = retrievals;
    }

    public void expire() {
        if(!hasExpired()) {
            setExpires(new Date(System.currentTimeMillis()));
        }
    }

    public boolean hasExpired() {
        return getExpires() != null
                && getExpires().after(new java.util.Date(System.currentTimeMillis() - Duration.ofDays(1).toMillis()));
    }
}
