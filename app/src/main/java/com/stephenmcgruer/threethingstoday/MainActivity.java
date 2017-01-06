package com.stephenmcgruer.threethingstoday;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.stephenmcgruer.threethingstoday.database.ThreeThingsContract.ThreeThingsEntry;
import com.stephenmcgruer.threethingstoday.database.ThreeThingsDbHelper;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private ThreeThingsDbHelper mDbHelper = null;

    private int mSelectedYear = -1;
    private int mSelectedMonth = -1;
    private int mSelectedDayOfMonth = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new ThreeThingsDbHelper(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Default to the current date.
        final Calendar c = Calendar.getInstance();
        mSelectedYear = c.get(Calendar.YEAR);
        mSelectedMonth = c.get(Calendar.MONTH);
        mSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        updateDateText();

        saveThreeThingsToDatabase();
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    public void showDatePickerDialog(View v) {
        DialogFragment fragment = DatePickerFragment.newInstance(this, mSelectedYear,
                mSelectedMonth, mSelectedDayOfMonth);
        fragment.show(getSupportFragmentManager(), "DatePicker");
    }

    public void submitThreeThings(View v) {
        String firstThingText = getTextFromEditText(R.id.first_edit_text);
        String secondThingText = getTextFromEditText(R.id.second_edit_text);
        String thirdThingText = getTextFromEditText(R.id.third_edit_text);

        ContentValues values = new ContentValues();
        values.put(ThreeThingsEntry.COLUMN_NAME_YEAR, mSelectedYear);
        values.put(ThreeThingsEntry.COLUMN_NAME_MONTH, mSelectedMonth);
        values.put(ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH, mSelectedDayOfMonth);
        values.put(ThreeThingsEntry.COLUMN_NAME_FIRST_THING, firstThingText);
        values.put(ThreeThingsEntry.COLUMN_NAME_SECOND_THING, secondThingText);
        values.put(ThreeThingsEntry.COLUMN_NAME_THIRD_THING, thirdThingText);

        new DatabaseWriteTask(mDbHelper).execute(values);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mSelectedYear = year;
        mSelectedMonth = month;
        mSelectedDayOfMonth = dayOfMonth;

        updateDateText();
        saveThreeThingsToDatabase();
    }

    private void updateDateText() {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, mSelectedYear);
        c.set(Calendar.MONTH, mSelectedMonth);
        c.set(Calendar.DAY_OF_MONTH, mSelectedDayOfMonth);

        TextView dateTextView = (TextView) findViewById(R.id.date_text);
        DateFormat df = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        dateTextView.setText("On " + df.format(c.getTime()) + ", I ...");
    }

    private void saveThreeThingsToDatabase() {
        EditText firstThingEditText = (EditText) findViewById(R.id.first_edit_text);
        EditText secondThingEditText = (EditText) findViewById(R.id.second_edit_text);
        EditText thirdThingEditText = (EditText) findViewById(R.id.third_edit_text);

        DatabaseReadTask readerTask = new DatabaseReadTask(
                mDbHelper, firstThingEditText, secondThingEditText, thirdThingEditText);
        readerTask.execute(
                Integer.toString(mSelectedYear),
                Integer.toString(mSelectedMonth),
                Integer.toString(mSelectedDayOfMonth));
    }

    private String getTextFromEditText(int resourceId) {
        EditText editText = (EditText) findViewById(resourceId);
        return editText.getText().toString();
    }

    private static class DatabaseWriteTask extends AsyncTask<ContentValues, Void, Long> {

        private final ThreeThingsDbHelper mDbHelper;

        public DatabaseWriteTask(ThreeThingsDbHelper dbHelper) {
            mDbHelper = dbHelper;
        }

        @Override
        protected Long doInBackground(ContentValues... values) {
            if (values.length < 1) {
                return -1L;
            }

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            return db.replace(ThreeThingsEntry.TABLE_NAME, null, values[0]);
        }
    }

    private static class DatabaseReadTask extends AsyncTask<String, Void, String[]> {

        private final ThreeThingsDbHelper mDbHelper;

        private final EditText mFirstThingEditText;
        private final EditText mSecondThingEditText;
        private final EditText mThirdThingEditText;

        public DatabaseReadTask(ThreeThingsDbHelper dbHelper, EditText firstThingEditText,
                                EditText secondThingEditText, EditText thirdThingEditText) {
            mDbHelper = dbHelper;

            mFirstThingEditText = firstThingEditText;
            mSecondThingEditText = secondThingEditText;
            mThirdThingEditText = thirdThingEditText;
        }

        @Override
        protected String[] doInBackground(String... selectionArgs) {
            // If there is no current results for the selection args, we just return empty things.
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

            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    ThreeThingsEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (cursor.moveToNext()) {
                results[0] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_FIRST_THING));
                results[1] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_SECOND_THING));
                results[2] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_THIRD_THING));
            }

            return results;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if (strings.length < 3) {
                return;
            }

            mFirstThingEditText.setText(strings[0]);
            mSecondThingEditText.setText(strings[1]);
            mThirdThingEditText.setText(strings[2]);
        }
    }
}
