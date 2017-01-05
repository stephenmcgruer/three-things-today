package com.stephenmcgruer.threethingstoday;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showDatePickerDialog(View v) {
        // TODO(smcgruer): Implement.
        Toast.makeText(getApplicationContext(), "showDatePickerDialog", Toast.LENGTH_SHORT).show();
    }

    public void submitThreeThings(View v) {
        // TODO(smcgruer): Implement.
        Toast.makeText(getApplicationContext(), "submitThreeThings", Toast.LENGTH_SHORT).show();
    }
}
