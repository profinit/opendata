package eu.profinit.opendata.model;

import eu.profinit.opendata.model.util.PeriodicityConverter;
import eu.profinit.opendata.model.util.RecordTypeConverter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

/**
 * Created by DM on 8. 11. 2015.
 */
@javax.persistence.Entity
@SequenceGenerator(name = "seq_pk", sequenceName = "data_source_data_source_id_seq")
@Table(name = "data_source", schema = "public", catalog = "opendata")
public class DataSource {
    private boolean isIncremental;
    private Timestamp lastProcessedDate;
    private Long dataSourceId;
    private RecordType recordType;
    private Periodicity periodicity;
    private Collection<DataInstance> dataInstances;
    private Entity entity;

    @Basic
    @Column(name = "is_incremental")
    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean isIncremental) {
        this.isIncremental = isIncremental;
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

        if (dataSourceId != that.dataSourceId) return false;
        if (isIncremental != that.isIncremental) return false;
        if (lastProcessedDate != null ? !lastProcessedDate.equals(that.lastProcessedDate) : that.lastProcessedDate != null)
            return false;
        if (periodicity != null ? !periodicity.equals(that.periodicity) : that.periodicity != null) return false;
        if (recordType != null ? !recordType.equals(that.recordType) : that.recordType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (isIncremental ? 1 : 0);
        result = 31 * result + (lastProcessedDate != null ? lastProcessedDate.hashCode() : 0);
        result = 31 * result + dataSourceId.intValue();
        result = 31 * result + (recordType != null ? recordType.hashCode() : 0);
        result = 31 * result + (periodicity != null ? periodicity.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "dataSource")
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
