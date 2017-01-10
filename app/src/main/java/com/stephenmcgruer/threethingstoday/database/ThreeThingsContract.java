// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

        public static final String[] COLUMNS = {
                COLUMN_NAME_YEAR,
                COLUMN_NAME_MONTH,
                COLUMN_NAME_DAY_OF_MONTH,
                COLUMN_NAME_FIRST_THING,
                COLUMN_NAME_SECOND_THING,
                COLUMN_NAME_THIRD_THING
        };
    }
}
