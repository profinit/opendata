package eu.profinit.opendata.model;

import java.time.Duration;

/**
 * Created by DM on 13. 11. 2015.
 */
public enum Periodicity {
    MONTHLY(Duration.ofDays(30)),
    YEARLY(Duration.ofDays(365)),
    WEEKLY(Duration.ofDays(7)),
    APERIODIC(Duration.ZERO),
    QUARTERLY(Duration.ofDays(90));

    private Duration duration;

    Periodicity(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
