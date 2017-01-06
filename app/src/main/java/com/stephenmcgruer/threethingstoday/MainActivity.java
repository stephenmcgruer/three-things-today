package com.stephenmcgruer.threethingstoday;

import android.app.DatePickerDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private int mSelectedYear = -1;
    private int mSelectedMonth = -1;
    private int mSelectedDayOfMonth = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Default to the current date.
        final Calendar c = Calendar.getInstance();
        mSelectedYear = c.get(Calendar.YEAR);
        mSelectedMonth = c.get(Calendar.MONTH);
        mSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        updateDateText();

        // TODO(smcgruer): Do an initial read from the database. (Should be async).
    }

    public void showDatePickerDialog(View v) {
        DialogFragment fragment = DatePickerFragment.newInstance(this, mSelectedYear,
                mSelectedMonth, mSelectedDayOfMonth);
        fragment.show(getSupportFragmentManager(), "DatePicker");
    }

    public void submitThreeThings(View v) {
        // TODO(smcgruer): Implement.
        Toast.makeText(getApplicationContext(), "submitThreeThings", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mSelectedYear = year;
        mSelectedMonth = month;
        mSelectedDayOfMonth = dayOfMonth;

        updateDateText();

        String firstThing = "";
        String secondThing = "";
        String thirdThing = "";

        // TODO(smcgruer): Read from database to get existing text for this date.

        EditText firstThingEditText = (EditText) findViewById(R.id.first_edit_text);
        EditText secondThingEditText = (EditText) findViewById(R.id.second_edit_text);
        EditText thirdThingEditText = (EditText) findViewById(R.id.third_edit_text);

        firstThingEditText.setText(firstThing);
        secondThingEditText.setText(secondThing);
        thirdThingEditText.setText(thirdThing);
    }

    private void updateDateText() {
        // TODO(smcgruer): Format the date properly.
        TextView dateTextView = (TextView) findViewById(R.id.date_text);
        dateTextView.setText(mSelectedYear + "/" + mSelectedMonth + "/" + mSelectedDayOfMonth);
    }
}
