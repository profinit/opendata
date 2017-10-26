package eu.profinit.opendata.control;

import eu.profinit.opendata.common.Util;
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
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A base implementation of a DataSourceHandler. All current DataSource implementations inherit from this class.
 */
public abstract class GenericDataSourceHandler implements DataSourceHandler {

    @Autowired
    private TransformDriver transformDriver;

    @PersistenceContext
    protected EntityManager em;

    private Logger log = LogManager.getLogger(getClass().getName());

    /**
     * First checks whether new DataInstances should be generated based on the time of last processing and periodicity.
     * If so, the method updating DataInstances is called (abstract). Then, DataInstances are marked for processing
     * based on their periodicity, time of last processing, expiration date and so on. A retrieval is initiated for
     * every marked DataInstance.
     *
     * @param ds The currently processed DataSource
     * @see DataInstance
     * @see TransformDriver#doRetrieval(DataInstance)
     * @see Periodicity
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDataSource(DataSource ds) {
        log.info("Processing of data source " + ds.getDataSourceId() + " started");

        if (!ds.isActive()) {
            log.info("Data source isn't active. Exiting.");
            return;
        }

        //Generate Data Instances if periodicity is different from APERIODIC and enough time has elapsed
        this.generateDataInstances(ds);

        //Get data instances for processing
        List<DataInstance> toProcess = getInstancesForProcessing(ds);

        //Run extraction
        for (DataInstance dataInstance : toProcess) {
            runExtractionOnDataInstance(dataInstance);
        }

        log.trace("Merging DataSource object");
        em.merge(ds);

    }

    /**
     * If enough time has elapsed since the specified DataSource was last processed and this DataSource is periodic,
     * invokes <code>updateDataInstances</code>.
     *
     * @param ds The currently processed DataSource
     * @see GenericDataSourceHandler#updateDataInstances(DataSource)
     */
    protected void generateDataInstances(DataSource ds) {
        log.info("Generating new data instances");
        if (ds.getPeriodicity().equals(Periodicity.APERIODIC)) {
            log.info("Data source is aperiodic. Nothing to generate.");
            return;
        }

        log.debug("Checking elapsed time since last processing");
        if (ds.getLastProcessedDate() == null ||
                Util.hasEnoughTimeElapsed(ds.getLastProcessedDate(), ds.getPeriodicity().getDuration())) {

            this.updateDataInstances(ds);
            ds.setLastProcessedDate(Timestamp.from(Instant.now()));
        } else {
            log.info("Not enough time has elapsed since the last processing. Nothing to generate.");
        }

    }

    /**
     * Checks each DataInstance belonging to the specified DataSource to see if the conditions are satisfied for a new
     * retrieval to be run. This happens if the DataInstance is periodic and enough time has elapsed since its last
     * processing, or the DataInstance hasn't been processed yet. Nothing is done if the DataInstance's expiry date
     * is in the past.
     * @param ds The currently processed DataSource
     * @return List of DataInstances on which the retrieval process should be run.
     */
    protected List<DataInstance> getInstancesForProcessing(DataSource ds) {

        log.info("Collecting data instances to be processed");
        Collection<DataInstance> dataInstances = ds.getDataInstances();
        List<DataInstance> result = new LinkedList<>();

        for (DataInstance dataInstance : dataInstances) {

            log.debug("Checking data instance " + dataInstance.getDataInstanceId());
            Periodicity p = dataInstance.getPeriodicity();
            Timestamp lpd = dataInstance.getLastProcessedDate();

            boolean isProcessed = lpd != null;
            boolean isPeriodic = !p.equals(Periodicity.APERIODIC);
            boolean hasExpired = dataInstance.hasExpired();
            log.debug("(isProcessed, isPeriodic, hasExpired) = " +
                    "(" + isProcessed + ", " + isPeriodic + ", " + hasExpired + ")");

            if (!hasExpired && (!isProcessed || (isPeriodic && Util.hasEnoughTimeElapsed(lpd, p.getDuration())))) {
                log.info("Marked data instance " + dataInstance.getDataInstanceId() + " for processing");
                result.add(dataInstance);
            }
        }

        return result;
    }

    /**
     * Runs the retrieval process on the specified DataInstance and deals with any errors. Creates and persists a new
     * Retrieval object to hold the results. Updates the DataInstance metadata accordingly.
     * @param dataInstance The DataInstance to be processed
     * @see TransformDriver#doRetrieval(DataInstance)
     */
    @Transactional
    protected void runExtractionOnDataInstance(DataInstance dataInstance) {
        log.info("Proceeding with extraction on data instance " + dataInstance.getDataInstanceId());
        Retrieval retrieval = transformDriver.doRetrieval(dataInstance);
        log.info("Retrieval finished.");

        try {
            em.persist(retrieval);
        } catch (Exception ex) {
            log.error("Error while persisting", ex);
        }

        log.debug("(success, numRecordsInserted, numBadRecords, failureReason) = " +
                "(" + retrieval.isSuccess() + ", " + retrieval.getNumRecordsInserted() + ", " +
                retrieval.getNumBadRecords() + ", " + retrieval.getFailureReason() + ")");


        if (retrieval.isSuccess()) {
            dataInstance.setLastProcessedDate(Timestamp.from(Instant.now()));
            em.merge(dataInstance);
        }

    }

    protected Logger getLogger() {
        return LogManager.getLogger(getClass().getName());
    }

    /**
     * Where possible, updates old DataInstances and generates new ones, either by using a fixed URL scheme or by calling a remote server.
     * Sometimes this is not possible and DataInstances need to be inserted into the database manually. See the data catalogue for details.
     * @param ds The currently processed DataSource
     */
    protected abstract void updateDataInstances(DataSource ds);

    public void setEm(EntityManager em) {
        this.em = em;
    }
}
