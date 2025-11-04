package org.alexander.database.tables.meal;

import java.time.LocalTime;

public enum MealTypes {
    BREAKFAST,
    LUNCH,
    DINNER;

    public LocalTime defaultTime() {
        return switch (this) {
            case BREAKFAST -> LocalTime.of(9, 0);
            case LUNCH -> LocalTime.of(13, 0);
            case DINNER -> LocalTime.of(18, 0);
        };
    }

    public LocalTime getStartTime() {
        return switch (this) {
            case BREAKFAST -> LocalTime.of(4, 0);
            case LUNCH -> LocalTime.of(11, 0);
            case DINNER -> LocalTime.of(16, 0);
        };
    }

    public LocalTime getEndTime() {
        return switch (this) {
            case BREAKFAST -> LocalTime.of(11, 0);
            case LUNCH -> LocalTime.of(16, 0);
            case DINNER -> LocalTime.of(23, 0);
        };
    }
}
