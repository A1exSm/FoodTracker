package org.alexander.database.tables.day;

import org.alexander.database.Entity;

import java.time.LocalDate;

public class Day extends Entity<LocalDate> {
    private LocalDate date;
    private int week_id;
    private Double bodyWeight; // can be null

    /**
     * Constructor for Day
     * @param date the date of the day
     * @param week_id the id of the week the day belongs to
     * @param bodyWeight the body weight for the day, can be null
     */
    public Day(LocalDate date, int week_id,  Double bodyWeight) {
        super(date);
        this.date = date;
        this.week_id = week_id;
        this.bodyWeight = bodyWeight;
    }

    /**
     * Constructor for Day which sets bodyWeight to null
     * @param date the date of the day
     * @param weed_id the id of the week the day belongs to
     */
    public Day(LocalDate date, int weed_id) {
        this(date, weed_id, null);
    }

    public LocalDate getDate() {
        return date;
    }
    public int getWeek_id() {
        return week_id;
    }

    public Double getBodyWeight() {
        return bodyWeight;
    }

    public void setDate(LocalDate date) {
        this.date = date;
        setKey(date);
    }

    public void setWeek_id(int week_id) {
        this.week_id = week_id;
    }

    public void setBodyWeight(Double bodyWeight) {
        this.bodyWeight = bodyWeight;
    }
}
