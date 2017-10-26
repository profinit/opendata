package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import javax.persistence.EntityManager;

/**
 * The component responsible for the processing of a Workbook. Proceeds row-by-row, inserting and updating Records in
 * the database until the whole document has been read. The WorkbookProcessor requests its own database transaction so
 * that it can be rolled back safely in case of an error. During this transaction, no new Records are persisted -
 * instead, they are added to the Retrieval object that is being filled. The persist of the Retrieval will then cascade
 * to Records. However, Entities and other support objects may be inserted into the database directly during the course
 * of workbook processing.
 */
public interface WorkbookProcessor {

    /**
     * Processes a Workbook. Reads the loaded mapping and for each sheet that it defines, reads rows of the sheet. For
     * each row, it instantiates and calls components (filters, retrievers and converters) in the order defined by the
     * mapping. At the end of this process, a fully former Record corresponding to the row values is either saved or
     * updated. The processing of each sheet ends when the last row is reached. Empty rows are ignored.
     * @param workbook The Apache POI Workbook object to be processed.
     * @param mapping The mapping to be used.
     * @param retrieval The current retrieval metadata object.
     * @param log The transform logger
     * @throws TransformException Only FATAL exceptions will be thrown by this method.
     */
    void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval, Logger log) throws TransformException;

    /**
     * Verifies a Record by checking all mandatory attributes are non-null. Throws a RECORD_LOCAL exception if the Record
     * isn't valid.
     * @param record The record to be checked.
     * @throws TransformException
     */
    void checkRecordIntegrity(Record record) throws TransformException;

    //Testing purposes
    void setEm(EntityManager em);
    EntityManager getEm();
}
