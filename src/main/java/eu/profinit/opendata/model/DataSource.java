package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.ClassNameConverter;
import eu.profinit.opendata.model.util.PeriodicityConverter;
import eu.profinit.opendata.model.util.RecordTypeConverter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents source of data for the application. Essentially a connector between a public Entity and a record type
 * since physical data files are modeled as DataInstances. The invocation of a DataSource's handler provides an entry
 * point for the retrieval process. Each time the application is run, each active DataSource's handler is called exactly
 * once.
 * @see DataSourceHandler
 * @see DataInstance
 */
@javax.persistence.Entity
@SequenceGenerator(name = "seq_pk", sequenceName = "data_source_data_source_id_seq", allocationSize = 1)
@Table(name = "data_source", schema = "public", catalog = "opendata")
@NamedQuery(
        name="findActiveDataSources",
        query="SELECT OBJECT(ds) FROM DataSource ds WHERE ds.active = true"
)
public class DataSource {
    /** The time this data source was last processed. This may or may not have involved retrievals (both successful
     * and unsuccessful) */
    private Timestamp lastProcessedDate;

    /** The application identifier of this DataSource */
    private Long dataSourceId;

    /** The type of record one can expect to get from this DataSource. This does not mean data files can't contain any
     * others - this field is used to specify the main type. */
    private RecordType recordType;

    /** Specifies how often <em>new</em> DataInstances should be generated for this DataSource. Should be APERIODIC
     * unless the DataSource's handler can generate new instances automatically. */
    private Periodicity periodicity;

    /** The physical data files available for this DataSource */
    private Collection<DataInstance> dataInstances;

    /** The public Entity that is publishing this DataSource */
    private Entity entity;

    /** A description of this DataSource */
    private String description;

    /** Indicates whether this DataSource should be processed when the application runs. */
    private boolean active;

    /** The DataSourceHandler that should be invoked when processing this DataSource. Must be a Spring component
     * accessible from inside the application's ApplicationContext. */
    private Class<? extends DataSourceHandler> handlingClass;

    @Basic
    @Column(name = "active")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Convert(converter = ClassNameConverter.class)
    @Column(name = "handling_class")
    public Class<? extends DataSourceHandler> getHandlingClass() {
        return handlingClass;
    }

    public void setHandlingClass(Class<? extends DataSourceHandler> handlingClass) {
        this.handlingClass = handlingClass;
    }

    @Basic
    @Column(name = "last_processed_date")
    public Timestamp getLastProcessedDate() {
        return lastProcessedDate;
    }

    public void setLastProcessedDate(Timestamp lastProcessedDate) {
        this.lastProcessedDate = lastProcessedDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pk")
    @Column(name = "data_source_id")
    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @Convert(converter = RecordTypeConverter.class)
    @Column(name = "record_type")
    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    @Convert(converter = PeriodicityConverter.class)
    @Column(name = "periodicity")
    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSource that = (DataSource) o;

        if (!Objects.equals(dataSourceId, that.dataSourceId)) return false;
        if (active != that.active) return false;
        if (lastProcessedDate != null ? !lastProcessedDate.equals(that.lastProcessedDate) : that.lastProcessedDate != null)
            return false;
        if (periodicity != null ? !periodicity.equals(that.periodicity) : that.periodicity != null) return false;
        if (recordType != null ? !recordType.equals(that.recordType) : that.recordType != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (handlingClass != null ? !handlingClass.equals(that.handlingClass) : that.handlingClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (active ? 1 : 0);
        result = 31 * result + (lastProcessedDate != null ? lastProcessedDate.hashCode() : 0);
        result = 31 * result + dataSourceId.intValue();
        result = 31 * result + (recordType != null ? recordType.hashCode() : 0);
        result = 31 * result + (periodicity != null ? periodicity.hashCode() : 0);
        result = 31 * result + (handlingClass != null ? handlingClass.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "dataSource", cascade = CascadeType.REMOVE)
    public Collection<DataInstance> getDataInstances() {
        return dataInstances;
    }

    public void setDataInstances(Collection<DataInstance> dataInstances) {
        this.dataInstances = dataInstances;
    }

    @ManyToOne
    @JoinColumn(name = "entity_id", referencedColumnName = "entity_id", nullable = false)
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
