package eu.profinit.opendata.control;

import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dm on 11/24/15.
 */
public abstract class GenericDataSourceHandler implements DataSourceHandler {

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private TransformDriver transformDriver;

    @PersistenceContext(unitName = "postgres")
    private EntityManager em;

    @Override
    public void processDataSource(DataSource ds) {
        if(!ds.isActive()) {
            return;
        }

        //Generate Data Instances if periodicity is different from APERIODIC and enough time has elapsed
        this.generateDataInstances(ds);

        //Get data instances for processing
        List<DataInstance> toProcess = getInstancesForProcessing(ds);

        //Run extraction
        for(DataInstance dataInstance : toProcess) {
            runExtractionOnDataInstance(dataInstance);
        }

        em.getTransaction().begin();
        ds.setLastProcessedDate(Timestamp.from(Instant.now()));
        em.merge(ds);
        em.getTransaction().commit();

    }

    protected void generateDataInstances(DataSource ds) {
        if(ds.getPeriodicity().equals(Periodicity.APERIODIC)) {
            return;
        }

        if(ds.getLastProcessedDate() == null ||
                hasEnoughTimeElapsed(ds.getLastProcessedDate(), ds.getPeriodicity().getDuration())) {

            this.checkForNewDataInstance(ds);
        }

    }

    protected List<DataInstance> getInstancesForProcessing(DataSource ds) {
        Collection<DataInstance> dataInstances = ds.getDataInstances();
        List<DataInstance> result = new LinkedList<>();

        for(DataInstance dataInstance : dataInstances) {

            Periodicity p = dataInstance.getPeriodicity();
            Timestamp lpd = dataInstance.getLastProcessedDate();

            boolean isProcessed = lpd != null;
            boolean isPeriodic = !p.equals(Periodicity.APERIODIC);
            boolean hasExpired = dataInstance.hasExpired();

            if(!hasExpired && (!isProcessed || (isPeriodic && hasEnoughTimeElapsed(lpd, p.getDuration())))) {
                result.add(dataInstance);
            }
        }

        return result;
    }

    protected void runExtractionOnDataInstance(DataInstance dataInstance) {
        try {
            InputStream inputStream = downloadService.downloadDataFile(dataInstance);
            Retrieval retrieval = transformDriver.doRetrieval(dataInstance, inputStream,
                    getMappingFileForDataInstance(dataInstance));

            em.getTransaction().begin();
            em.persist(retrieval);
            if(retrieval.isSuccess()) {
                dataInstance.setLastProcessedDate(Timestamp.from(Instant.now()));
                em.merge(dataInstance);
            }
            em.getTransaction().commit();

        } catch (IOException e) {
            Logger.getLogger(this.getClass()).error("Could not download data file", e);
        }


    }

    private boolean hasEnoughTimeElapsed(Timestamp from, Duration targetDuration) {
        Duration elapsed = Duration.ofMillis(System.currentTimeMillis())
                    .minus(Duration.ofMillis(from.getTime()));

        return elapsed.dividedBy(2).compareTo(targetDuration) > 0;
    }

    protected abstract void checkForNewDataInstance(DataSource ds);
    protected abstract String getMappingFileForDataInstance(DataInstance di);
}
