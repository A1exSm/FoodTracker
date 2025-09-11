package org.alexander;
import org.alexander.database.DatabaseManager;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
@Suite
@SelectClasses({
        FoodDaoTest.class,
        WeekDaoTest.class
})

public class AllTests {
    static {
        DatabaseManager.initialise();
    }
}
