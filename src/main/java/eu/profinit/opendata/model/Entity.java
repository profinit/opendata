package eu.profinit.opendata.model;

import javax.persistence.*;
import java.util.Collection;
import eu.profinit.opendata.model.util.EntityTypeConverter;

/**
 * Created by DM on 8. 11. 2015.
 */
@javax.persistence.Entity
@SequenceGenerator(name = "seq_pk", sequenceName = "entity_entity_id_seq")
@Table(name = "entity", schema = "public", catalog = "opendata")
public class Entity {
    private String dic;
    private String ico;
    private boolean isPublic;

    private String name;
    private int entityId;
    private EntityType entityType;
    private Collection<DataSource> dataSources;

    @Basic
    @Column(name = "dic")
    public String getDic() {
        return dic;
    }

    public void setDic(String dic) {
        this.dic = dic;
    }

    @Basic
    @Column(name = "ico")
    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    @Basic
    @Column(name = "is_public")
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pk")
    @Column(name = "entity_id")
    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Column(name = "entity_type")
    @Convert(converter = EntityTypeConverter.class)
    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (entityId != entity.entityId) return false;
        if (isPublic != entity.isPublic) return false;
        if (dic != null ? !dic.equals(entity.dic) : entity.dic != null) return false;
        if (entityType != null ? !entityType.equals(entity.entityType) : entity.entityType != null) return false;
        if (ico != null ? !ico.equals(entity.ico) : entity.ico != null) return false;
        if (name != null ? !name.equals(entity.name) : entity.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dic != null ? dic.hashCode() : 0;
        result = 31 * result + (ico != null ? ico.hashCode() : 0);
        result = 31 * result + (isPublic ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + entityId;
        result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "entity")
    public Collection<DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Collection<DataSource> dataSources) {
        this.dataSources = dataSources;
    }
}
