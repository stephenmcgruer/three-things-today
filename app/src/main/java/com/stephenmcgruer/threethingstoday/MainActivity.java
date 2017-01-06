package com.stephenmcgruer.threethingstoday;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.stephenmcgruer.threethingstoday.database.ThreeThingsContract.ThreeThingsEntry;
import com.stephenmcgruer.threethingstoday.database.ThreeThingsDbHelper;

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

        // TODO(smcgruer): Should be async?
        updateThreeThingTexts();
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

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ThreeThingsEntry.COLUMN_NAME_YEAR, mSelectedYear);
        values.put(ThreeThingsEntry.COLUMN_NAME_MONTH, mSelectedMonth);
        values.put(ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH, mSelectedDayOfMonth);
        values.put(ThreeThingsEntry.COLUMN_NAME_FIRST_THING, firstThingText);
        values.put(ThreeThingsEntry.COLUMN_NAME_SECOND_THING, secondThingText);
        values.put(ThreeThingsEntry.COLUMN_NAME_THIRD_THING, thirdThingText);

        db.replace(ThreeThingsEntry.TABLE_NAME, null, values);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mSelectedYear = year;
        mSelectedMonth = month;
        mSelectedDayOfMonth = dayOfMonth;

        updateDateText();
        updateThreeThingTexts();
    }

    private void updateDateText() {
        // TODO(smcgruer): Format the date properly.
        TextView dateTextView = (TextView) findViewById(R.id.date_text);
        dateTextView.setText(mSelectedYear + "/" + mSelectedMonth + "/" + mSelectedDayOfMonth);
    }

    private void updateThreeThingTexts() {
        String firstThing = "";
        String secondThing = "";
        String thirdThing = "";

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                ThreeThingsEntry.COLUMN_NAME_FIRST_THING,
                ThreeThingsEntry.COLUMN_NAME_SECOND_THING,
                ThreeThingsEntry.COLUMN_NAME_THIRD_THING
        };
        String selection = ThreeThingsEntry.COLUMN_NAME_YEAR + " = ? AND " +
                ThreeThingsEntry.COLUMN_NAME_MONTH + " = ? AND " +
                ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH + " = ?";
        String[] selectionArgs = {
                Integer.toString(mSelectedYear),
                Integer.toString(mSelectedMonth),
                Integer.toString(mSelectedDayOfMonth)
        };

        Cursor cursor = db.query(
                ThreeThingsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);

        if (cursor.moveToNext()) {
            firstThing = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_FIRST_THING));
            secondThing = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_SECOND_THING));
            thirdThing = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_THIRD_THING));
        }

        EditText firstThingEditText = (EditText) findViewById(R.id.first_edit_text);
        EditText secondThingEditText = (EditText) findViewById(R.id.second_edit_text);
        EditText thirdThingEditText = (EditText) findViewById(R.id.third_edit_text);

        firstThingEditText.setText(firstThing);
        secondThingEditText.setText(secondThing);
        thirdThingEditText.setText(thirdThing);
    }

    private String getTextFromEditText(int resourceId) {
        EditText editText = (EditText) findViewById(resourceId);
        return editText.getText().toString();
    }
}
