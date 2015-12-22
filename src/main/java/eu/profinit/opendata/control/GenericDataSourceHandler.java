package eu.profinit.opendata.control;

import eu.profinit.opendata.transform.TransformDriver;
import eu.profinit.opendata.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    private TransformDriver transformDriver;

    @PersistenceContext
    protected EntityManager em;

    private Logger log = LogManager.getLogger(getClass().getName());

    @Override
    @Transactional
    public void processDataSource(DataSource ds) {
        log.info("Processing of data source " + ds.getDataSourceId() + " started");

        if(!ds.isActive()) {
            log.info("Data source isn't active. Exiting.");
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

        log.trace("Merging DataSource object with new time of last processing");
        ds.setLastProcessedDate(Timestamp.from(Instant.now()));
        em.merge(ds);

    }

    protected void generateDataInstances(DataSource ds) {
        log.info("Generating new data instances");
        if(ds.getPeriodicity().equals(Periodicity.APERIODIC)) {
            log.info("Data source is aperiodic. Nothing to generate.");
            return;
        }

        log.debug("Checking elapsed time since last processing");
        if(ds.getLastProcessedDate() == null ||
                hasEnoughTimeElapsed(ds.getLastProcessedDate(), ds.getPeriodicity().getDuration())) {

            this.checkForNewDataInstance(ds);
        } else {
            log.info("Not enough time has elapsed since the last processing. Nothing to generate.");
        }

    }

    protected List<DataInstance> getInstancesForProcessing(DataSource ds) {

        log.info("Collecting data instances to be processed");
        Collection<DataInstance> dataInstances = ds.getDataInstances();
        List<DataInstance> result = new LinkedList<>();

        for(DataInstance dataInstance : dataInstances) {

            log.debug("Checking data instance " + dataInstance.getDataInstanceId());
            Periodicity p = dataInstance.getPeriodicity();
            Timestamp lpd = dataInstance.getLastProcessedDate();

            boolean isProcessed = lpd != null;
            boolean isPeriodic = !p.equals(Periodicity.APERIODIC);
            boolean hasExpired = dataInstance.hasExpired();
            log.debug("(isProcessed, isPeriodic, hasExpired) = " +
                      "(" + isProcessed + ", " + isPeriodic + ", " + hasExpired + ")");

            if(!hasExpired && (!isProcessed || (isPeriodic && hasEnoughTimeElapsed(lpd, p.getDuration())))) {
                log.info("Marked data instance " + dataInstance.getDataInstanceId() + " for processing");
                result.add(dataInstance);
            }
        }

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void runExtractionOnDataInstance(DataInstance dataInstance) {
        log.info("Proceeding with extraction on data instance " + dataInstance.getDataInstanceId());
        Retrieval retrieval = transformDriver.doRetrieval(dataInstance, getMappingFileForDataInstance(dataInstance));
        log.info("Retrieval finished.");
        log.debug("(success, numRecordsInserted, numBadRecords, failureReason) = " +
                      "(" + retrieval.isSuccess() + ", " + retrieval.getNumRecordsInserted() + ", " +
                            retrieval.getNumBadRecords() + ", " + retrieval.getFailureReason() + ")");

        em.persist(retrieval);
        if(retrieval.isSuccess()) {
            dataInstance.setLastProcessedDate(Timestamp.from(Instant.now()));
            em.merge(dataInstance);
        }

    }

    private boolean hasEnoughTimeElapsed(Timestamp from, Duration targetDuration) {
        Duration elapsed = Duration.ofMillis(System.currentTimeMillis())
                    .minus(Duration.ofMillis(from.getTime()));

        log.debug("Elapsed time calculated as " + elapsed + ", target is " + targetDuration);
        return elapsed.compareTo(targetDuration.dividedBy(2)) > 0;
    }

    protected Logger getLogger() {
        return LogManager.getLogger(getClass().getName());
    }

    protected abstract void checkForNewDataInstance(DataSource ds);
    protected abstract String getMappingFileForDataInstance(DataInstance di);

    public void setEm(EntityManager em) {
        this.em = em;
    }
}
