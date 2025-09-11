package org.alexander.database.week;

import org.alexander.database.Entity;

import java.time.LocalDate;

public class Week extends Entity<Integer> {
    private final int id;
    private LocalDate startDate;
    private LocalDate endDate;

    public Week(int id, LocalDate startDate, LocalDate endDate) {
        super(id);
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
}
