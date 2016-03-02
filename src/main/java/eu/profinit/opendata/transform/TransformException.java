package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Represents an error thrown during the course of a retrieval. An uncaught TransformException will cause the whole
 * retrieval to fail and its transaction to be rolled back. There are three severity levels of a TransformException,
 * each with different consequences for the current retrieval.
 * @see WorkbookProcessor#processWorkbook(Workbook, Mapping, Retrieval, Logger)
 */
public class TransformException extends Exception {

    /**
     * Enumerates the possible severities of a TransformException.
     */
    public enum Severity {
        /**
         * Only affects the current property being set. It will most likely be set to null and the retrieval will
         * continue with the next property.
         */
        PROPERTY_LOCAL,

        /**
         * Affects the current Record. The error prevents it from being saved successfull. The retrieval will continue
         * with the next Record.
         */
        RECORD_LOCAL,

        /**
         * Fails the entire retrieval. Usually due to a record local exception that will occur for
         * single record. The cause may be an error in the mapping or a malformed source document.
         */
        FATAL;
    }

    private Severity severity;

    public TransformException(String message, Severity severity) {
        super(message);
        this.severity = severity;
    }

    public TransformException(String message, Throwable cause, Severity severity) {
        super(message, cause);
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
    }
}
