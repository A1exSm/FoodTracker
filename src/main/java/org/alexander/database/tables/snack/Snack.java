package org.alexander.database.tables.snack;

import org.alexander.database.Entity;
import java.time.LocalDate;
import java.time.LocalTime;

public class Snack extends Entity<Integer> {
    private Integer id; // Primary Key
    private LocalDate date;
    private LocalTime time;


    /**
     * Creates a new Snack entity with the given id.
     * @param id can be null for new entities not yet in the database
     * @param date the date which references the day table
     * @param time the time of the snack
     */
    public Snack(Integer id, LocalDate date, LocalTime time) {
        super(id);
        this.id = id;
        this.date = date;
        this.time = time;
    }

    /**
     * Creates a new Snack entity, initialising the id to null.
     * Should be used for new entities not yet in the database.
     * @param date the date which references the day table
     * @param time the time of the snack
     */
    public Snack(LocalDate date, LocalTime time) {
        this(null, date, time);
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setId(Integer id) {
        this.id = id;
        this.setKey(id);
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
