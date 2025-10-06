package org.alexander.database.tables.week;

import org.alexander.database.Entity;
import org.alexander.logging.CentralLogger;

import java.time.LocalDate;

public class Week extends Entity<Integer> {
    private final int id;
    private LocalDate startDate;
    private LocalDate endDate;

    public Week(int id, LocalDate startDate, LocalDate endDate) {
        super(id);
        if (startDate.getDayOfWeek().getValue() != 1 || endDate.getDayOfWeek().getValue() != 7) {
            String message = String.format("Invalid week range: start date '%s' is not a Monday or end date '%s' is not a Sunday. StartDate is a '%s' and EndDate is a '%s'.", startDate, endDate, startDate.getDayOfWeek(), endDate.getDayOfWeek());
            CentralLogger.getInstance().logError(new IllegalArgumentException(message));
        }
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Week week)) return false;
        return id == week.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
