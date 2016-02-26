package eu.profinit.opendata.model;

import java.time.Duration;

/**
 * Represents a single set duration. This is used in many different areas by the application, usually to refer to how
 * often a data file is published or a data source updated.
 */
public enum Periodicity {
    DAILY(Duration.ofDays(1)),
    MONTHLY(Duration.ofDays(30)),
    YEARLY(Duration.ofDays(365)),
    WEEKLY(Duration.ofDays(7)),
    APERIODIC(Duration.ZERO),
    QUARTERLY(Duration.ofDays(90));

    /** The exact duration this Periodicity represents. */
    private Duration duration;

    Periodicity(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
