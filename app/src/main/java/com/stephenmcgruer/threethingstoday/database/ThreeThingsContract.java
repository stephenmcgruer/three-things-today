package com.stephenmcgruer.threethingstoday.database;

public class ThreeThingsContract {
    private ThreeThingsContract() {}

    public static class ThreeThingsEntry {
        public static final String TABLE_NAME = "threethings";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY_OF_MONTH = "dayOfMonth";
        public static final String COLUMN_NAME_FIRST_THING = "firstThing";
        public static final String COLUMN_NAME_SECOND_THING = "secondThing";
        public static final String COLUMN_NAME_THIRD_THING = "thirdThing";
    }
}
