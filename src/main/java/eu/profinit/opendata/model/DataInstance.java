package eu.profinit.opendata.model;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by DM on 8. 11. 2015.
 */
@javax.persistence.Entity
@Table(name = "data_instance", schema = "public", catalog = "opendata")
@SequenceGenerator(name = "seq_pk", sequenceName = "data_instance_data_instance_id_seq")
public class DataInstance {
    private String format;
    private boolean processed;
    private String url;
    private Long dataInstanceId;
    private DataSource dataSource;
    private Collection<Retrieval> retrievals;

    @Basic
    @Column(name = "format")
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Basic
    @Column(name = "processed")
    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        if (processed != that.processed) return false;
        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = format != null ? format.hashCode() : 0;
        result = 31 * result + (processed ? 1 : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + dataInstanceId.intValue();
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

    @OneToMany(mappedBy = "dataInstance")
    public Collection<Retrieval> getRetrievals() {
        return retrievals;
    }

    public void setRetrievals(Collection<Retrieval> retrievals) {
        this.retrievals = retrievals;
    }
}
