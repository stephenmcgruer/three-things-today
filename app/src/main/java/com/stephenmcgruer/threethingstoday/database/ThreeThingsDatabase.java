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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.stephenmcgruer.threethingstoday.database.ThreeThingsContract.ThreeThingsEntry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ThreeThingsDatabase {
    private static final String TAG = "ThreeThingsDatabase";

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

        Log.d("ThreeThingsDatabase", "writeContentValues: writing: " + values.toString());
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
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);
        csvWriter.writeNext(ThreeThingsEntry.COLUMNS);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ThreeThingsEntry.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            List<String> data = new ArrayList(ThreeThingsEntry.COLUMNS.length);
            for (String column : ThreeThingsEntry.COLUMNS) {
                data.add(cursor.getString(cursor.getColumnIndexOrThrow(column)));
            }
            csvWriter.writeNext(data.toArray(new String[0]));
        }

        try {
            csvWriter.close();
        } catch (IOException e) {
            // Ignore.
        }

        return stringWriter.toString();
    }

    public synchronized boolean importDatabase(ContentResolver contentResolver, Uri uri) {
        List<String[]> lines;
        try {
            CSVReader csvReader = new CSVReader(
                    new InputStreamReader(contentResolver.openInputStream(uri)));
            lines = csvReader.readAll();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "importDatabase: FileNotFoundException", e);
            return false;
        } catch (IOException e) {
            Log.d(TAG, "importDatabase: IOException", e);
            return false;
        }

        if (lines.size() < 1) {
            // Cannot import, not enough data.
            Log.d(TAG, "importDatabase: results.size() < 1");
            return false;
        }

        // We check the entire file first to make sure that it is good before doing any database
        // operations.
        //
        // The first line is column headers so we skip it.

        List<ContentValues> entries = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i);
            if (parts.length != ThreeThingsEntry.COLUMNS.length) {
                // Bad file.
                Log.d(TAG, "importDatabase: parts.length != ThreeThingsEntry.COLUMNS.length"
                        + ", " + parts.length + " != " + ThreeThingsEntry.COLUMNS.length);
                Log.d(TAG, "importDatabase: " + Arrays.toString(parts));
                return false;
            }

            // Three integers followed by three strings.
            int year, month, dayOfMonth;
            try {
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                dayOfMonth = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                Log.d(TAG, "importDatabase: NumberFormatException");
                return false;
            }
            String firstThing = parts[3];
            String secondThing = parts[4];
            String thirdThing = parts[5];

            if (firstThing.isEmpty() && secondThing.isEmpty() && thirdThing.isEmpty()) {
                // Skip empty entry.
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(ThreeThingsEntry.COLUMN_NAME_YEAR, year);
            values.put(ThreeThingsEntry.COLUMN_NAME_MONTH, month);
            values.put(ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH, dayOfMonth);
            values.put(ThreeThingsEntry.COLUMN_NAME_FIRST_THING, firstThing);
            values.put(ThreeThingsEntry.COLUMN_NAME_SECOND_THING, secondThing);
            values.put(ThreeThingsEntry.COLUMN_NAME_THIRD_THING, thirdThing);

            entries.add(values);
        }

        for (ContentValues values : entries) {
            writeContentValues(values);
        }

        return true;
    }
}
