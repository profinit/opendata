package eu.profinit.opendata.transform;

/**
 * Created by dm on 12/2/15.
 */
public class TransformException extends Exception {

    public enum Severity {
        PROPERTY_LOCAL,
        RECORD_LOCAL,
        FATAL;
    }

    private Severity severity;

    public TransformException(String message, Severity severity) {
        super(message);
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
    }
}
