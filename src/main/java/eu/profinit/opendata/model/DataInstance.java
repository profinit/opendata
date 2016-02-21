package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.PeriodicityConverter;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single published data file. Could be an XLS file, a link to generate one or a dummy container for
 * metadata. A single DataSource can have multiple DataInstances. Some DataInstances are be generated automatically by
 * Handlers, some are used perpetually, others need to be inserted periodically via SQL.
 * Refer to the data catalogue for details about individual sources.
 * @see DataSource
 */
@javax.persistence.Entity
@Table(name = "data_instance", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "data_instance_data_instance_id_seq", allocationSize = 1)
public class DataInstance {

    /** "xls" or "xlsx" depending on the data file format*/
    private String format;

    /** The time of the last <em>successful</em> processing of this DataInstance */
    private Timestamp lastProcessedDate;

    /** URL to be used to retrieve the actual XLS file */
    private String url;

    /** Application identifier */
    private Long dataInstanceId;

    /** The parent DataSource */
    private DataSource dataSource;

    /** Retrievals performed on this DataInstance */
    private Collection<Retrieval> retrievals;

    /** How often this DataInstance can be expected to contain new data. This should be APERIODIC unless the exact
     * same data file at the exact same URL can be periodically reused. */
    private Periodicity periodicity;

    /** The date after which this DataInstance should no longer be processed. */
    private Date expires;

    /** Indicates whether the rows that have already been processed previously can be skipped in any new retrieval.
     * Should be false unless new records are only ever added at the end of this data file. */
    private Boolean incremental = true;

    /** The row at which the last retrieval has ended. Only applicable if incremental is true. */
    private Integer lastProcessedRow;

    /** The authority's own identifier for this DataInstance. Used for identifying results returned by an API */
    private String authorityId;

    /** Description of the data file's contents */
    private String description;

    /** Path to the mapping file that should be used when processing this DataInstance.*/
    private String mappingFile;

    @Basic
    @Column(name = "format")
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Basic
    @Column(name = "mapping_file")
    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    @Basic
    @Column(name = "authority_id")
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

    @Basic
    @Column(name="incremental")
    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataInstance that = (DataInstance) o;

        if (!Objects.equals(dataInstanceId, that.dataInstanceId)) return false;
        if (periodicity != null ? !periodicity.equals(that.periodicity) : that.periodicity != null) return false;
        if (lastProcessedDate != null ?
                !lastProcessedDate.equals(that.lastProcessedDate) : that.lastProcessedDate != null) return false;
        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (incremental != ((DataInstance) o).isIncremental()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = format != null ? format.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + dataInstanceId.intValue();
        result = 31 * result + periodicity.hashCode();
        result = 31 * result + lastProcessedDate.hashCode();
        result = 31 * result + (incremental ? 1 : 0);
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

    /**
     * Sets the expiry date of this DataInstance to the current date.
     */
    public void expire() {
        if(!hasExpired()) {
            setExpires(new Date(System.currentTimeMillis()));
        }
    }

    /**
     * @return True if this DataInstance's expiry date is in the past.
     */
    public boolean hasExpired() {
        return getExpires() != null
                && getExpires().after(new java.util.Date(System.currentTimeMillis() - Duration.ofDays(1).toMillis()));
    }
}
