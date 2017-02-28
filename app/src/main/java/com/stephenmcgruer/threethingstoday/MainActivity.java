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

package com.stephenmcgruer.threethingstoday;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stephenmcgruer.threethingstoday.database.ThreeThingsContract.ThreeThingsEntry;
import com.stephenmcgruer.threethingstoday.database.ThreeThingsDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        View.OnFocusChangeListener, TextWatcher {

    private static final int DATABASE_IMPORT_CODE = 0;

    private static final boolean DEBUG_MODE = false;

    private ThreeThingsDatabase mThreeThingsDatabase = null;

    private final Timer mWriteDatabaseTimer = new Timer();
    private TimerTask mWriteDatabaseTask = null;

    private int mSelectedYear = -1;
    private int mSelectedMonth = -1;
    private int mSelectedDayOfMonth = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThreeThingsDatabase = new ThreeThingsDatabase(getApplicationContext());

        setupDailyNotification();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText firstThingEditText = (EditText) findViewById(R.id.first_edit_text);
        EditText secondThingEditText = (EditText) findViewById(R.id.second_edit_text);
        EditText thirdThingEditText = (EditText) findViewById(R.id.third_edit_text);

        firstThingEditText.setOnFocusChangeListener(this);
        secondThingEditText.setOnFocusChangeListener(this);
        thirdThingEditText.setOnFocusChangeListener(this);

        firstThingEditText.addTextChangedListener(this);
        secondThingEditText.addTextChangedListener(this);
        thirdThingEditText.addTextChangedListener(this);

        // Default to the current date.
        final Calendar c = Calendar.getInstance();
        mSelectedYear = c.get(Calendar.YEAR);
        mSelectedMonth = c.get(Calendar.MONTH);
        mSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        updateDateText();

        updateThreeThingsText();
    }

    private void setupDailyNotification() {
        Intent notifyIntent = new Intent(this, ThreeThingsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO(smcgruer): Allow user to configure the time.
        final Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) >= 20)
            cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                1000 * 60 * 60 * 24, pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        if (DEBUG_MODE) {
            menu.findItem(R.id.mi_test_notification).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_export_database:
                new DatabaseExportTask(this, mThreeThingsDatabase).execute();
                return true;
            case R.id.mi_import_database:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a file to open"),
                            DATABASE_IMPORT_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Please install a File Manager",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.mi_test_notification:
                Intent notificationIntent = new Intent(this, ThreeThingsNotificationIntentService.class);
                startService(notificationIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        mThreeThingsDatabase.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DATABASE_IMPORT_CODE:
                if (resultCode == RESULT_OK) {
                    final Uri uri = data.getData();
                    new AlertDialog.Builder(this)
                            .setTitle("Import Database CSV")
                            .setMessage("WARNING: Imported dates will override existing ones!")
                            .setPositiveButton("Import", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new DatabaseImportTask(
                                            MainActivity.this, mThreeThingsDatabase).execute(uri);
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment fragment = DatePickerFragment.newInstance(this, mSelectedYear,
                mSelectedMonth, mSelectedDayOfMonth);
        fragment.show(getSupportFragmentManager(), "DatePicker");
    }

    public void submitThreeThings() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

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

        if (mWriteDatabaseTask != null) {
            mWriteDatabaseTask.cancel();
        }
        mWriteDatabaseTask = new DatabaseWriteTask(progressBar, mThreeThingsDatabase, values);
        mWriteDatabaseTimer.schedule(mWriteDatabaseTask, 500l);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mSelectedYear = year;
        mSelectedMonth = month;
        mSelectedDayOfMonth = dayOfMonth;

        updateDateText();
        updateThreeThingsText();
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

    private void updateThreeThingsText() {
        EditText firstThingEditText = (EditText) findViewById(R.id.first_edit_text);
        EditText secondThingEditText = (EditText) findViewById(R.id.second_edit_text);
        EditText thirdThingEditText = (EditText) findViewById(R.id.third_edit_text);

        DatabaseReadTask readerTask = new DatabaseReadTask(
                mThreeThingsDatabase, firstThingEditText, secondThingEditText, thirdThingEditText);
        readerTask.execute(mSelectedYear, mSelectedMonth, mSelectedDayOfMonth);
    }

    private String getTextFromEditText(int resourceId) {
        EditText editText = (EditText) findViewById(resourceId);
        return editText.getText().toString();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            // Focus changed, schedule a write.
            submitThreeThings();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Not implemented.
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Not implemented.
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Text changed, schedule a write.
        submitThreeThings();
    }

    private static class DatabaseWriteTask extends TimerTask {

        private final ProgressBar mProgressBar;
        private final ThreeThingsDatabase mDatabase;
        private final ContentValues mValues;

        public DatabaseWriteTask(ProgressBar progressBar,
                                 ThreeThingsDatabase database,
                                 ContentValues values) {
            mProgressBar = progressBar;
            mDatabase = database;
            mValues = values;
        }

        @Override
        public void run() {
            mDatabase.writeContentValues(mValues);
            mProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private static class DatabaseReadTask extends AsyncTask<Integer, Void, String[]> {

        private final ThreeThingsDatabase mDatabase;

        private final EditText mFirstThingEditText;
        private final EditText mSecondThingEditText;
        private final EditText mThirdThingEditText;

        public DatabaseReadTask(ThreeThingsDatabase database, EditText firstThingEditText,
                                EditText secondThingEditText, EditText thirdThingEditText) {
            mDatabase = database;

            mFirstThingEditText = firstThingEditText;
            mSecondThingEditText = secondThingEditText;
            mThirdThingEditText = thirdThingEditText;
        }

        @Override
        protected String[] doInBackground(Integer... dateComponents) {
            if (dateComponents.length < 3) {
                return null;
            }

            return mDatabase.readThreeThings(
                    dateComponents[0], dateComponents[1], dateComponents[2]);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if (strings == null || strings.length < 3) {
                return;
            }

            mFirstThingEditText.setText(strings[0]);
            mSecondThingEditText.setText(strings[1]);
            mThirdThingEditText.setText(strings[2]);
        }
    }

    private static class DatabaseImportTask extends AsyncTask<Uri, Void, Boolean> {

        private final MainActivity mMainActivity;
        private final ThreeThingsDatabase mThreeThingsDatabase;

        public DatabaseImportTask(MainActivity mainActivity,
                                  ThreeThingsDatabase threeThingsDatabase) {
            mMainActivity = mainActivity;
            mThreeThingsDatabase = threeThingsDatabase;
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            return mThreeThingsDatabase.importDatabase(
                    mMainActivity.getContentResolver(), params[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mMainActivity.updateThreeThingsText();
            } else {
                Toast.makeText(mMainActivity, "Unable to import!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class DatabaseExportTask extends AsyncTask<Void, Void, File> {

        private static final String TAG = "DatabaseExportTask";

        private final Context mContext;
        private final ThreeThingsDatabase mDatabase;

        public DatabaseExportTask(Context context, ThreeThingsDatabase database) {
            mContext = context;
            mDatabase = database;
        }

        @Override
        protected File doInBackground(Void... unused) {
            // NOTE(smcgruer): Folder must be kept in sync with the FileProvider xml
            File tempDir = new File(mContext.getCacheDir() + File.separator + "database_exports");
            if (!tempDir.exists() && !tempDir.mkdir()) {
                Log.e(TAG, "doInBackground: unable to create temporary directory");
                return null;
            }

            try {
                File tempFile = File.createTempFile("three-things-today-data", ".csv", tempDir);
                FileWriter fileWriter = new FileWriter(tempFile);
                fileWriter.append(mDatabase.exportDatabaseToCsvString());
                fileWriter.close();
                return tempFile;
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: Error whilst writing temporary file", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(File tempFile) {
            super.onPostExecute(tempFile);

            if (tempFile == null) {
                Toast.makeText(mContext, "Unable to export database, please try again later",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    mContext, "com.stephenmcgruer.threethingstoday.fileprovider", tempFile));
            sendIntent.setType("text/csv");
            mContext.startActivity(
                    Intent.createChooser(sendIntent, "Export database to ..."));
        }
    }
}
