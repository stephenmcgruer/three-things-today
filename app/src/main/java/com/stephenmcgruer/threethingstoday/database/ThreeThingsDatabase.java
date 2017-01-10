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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.stephenmcgruer.threethingstoday.database.ThreeThingsContract.ThreeThingsEntry;

import java.util.ArrayList;
import java.util.List;

public class ThreeThingsDatabase {
    private final ThreeThingsDbHelper mDbHelper;

    public ThreeThingsDatabase(Context context) {
        mDbHelper = new ThreeThingsDbHelper(context);
    }

    public synchronized void close() {
        mDbHelper.close();
    }

    public synchronized boolean writeContentValues(ContentValues values) {
        if (values == null) {
            return false;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.replace(ThreeThingsEntry.TABLE_NAME, null, values) != -1;
    }

    public synchronized String[] readThreeThings(int year, int month, int dayOfMonth) {
        // If there are no current results for the selection args, we just return empty things.
        String[] results = {
                "",
                "",
                ""
        };

        String[] projection = {
                ThreeThingsEntry.COLUMN_NAME_FIRST_THING,
                ThreeThingsEntry.COLUMN_NAME_SECOND_THING,
                ThreeThingsEntry.COLUMN_NAME_THIRD_THING
        };
        String selection = ThreeThingsEntry.COLUMN_NAME_YEAR + " = ? AND " +
                ThreeThingsEntry.COLUMN_NAME_MONTH + " = ? AND " +
                ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH + " = ?";
        String[] selectionArgs = {
                Integer.toString(year),
                Integer.toString(month),
                Integer.toString(dayOfMonth)
        };

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ThreeThingsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            results[0] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_FIRST_THING));
            results[1] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_SECOND_THING));
            results[2] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_THIRD_THING));
        }

        return results;
    }

    public synchronized String exportDatabaseToCsvString() {
        StringBuilder sb = new StringBuilder(
                TextUtils.join(",", ThreeThingsEntry.COLUMNS));
        sb.append("\n");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ThreeThingsEntry.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            List<String> data = new ArrayList(ThreeThingsEntry.COLUMNS.length);
            for (String column : ThreeThingsEntry.COLUMNS) {
                data.add(cursor.getString(cursor.getColumnIndexOrThrow(column)));
            }
            sb.append(TextUtils.join(",", data));
            sb.append("\n");
        }

        return sb.toString();
    }
}
