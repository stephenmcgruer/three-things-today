package com.stephenmcgruer.threethingstoday;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener mOnDateSetListener = null;
    private int mInitialYear = -1;
    private int mInitialMonth = -1;
    private int mInitialDayOfMonth = -1;

    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener,
                                                 int initialYear, int initialMonth,
                                                 int initialDayOfMonth) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setOnDateSetListener(listener);
        fragment.setInitialDate(initialYear, initialMonth, initialDayOfMonth);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), mOnDateSetListener,
                mInitialYear, mInitialMonth, mInitialDayOfMonth);

        // Disallow future dates.
        // TODO(smcgruer): Confirm that there are no edge cases to this approach.
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        return dialog;
    }

    private void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        mOnDateSetListener = listener;
    }

    private void setInitialDate(int initialYear, int initialMonth, int initialDayOfMonth) {
        mInitialYear = initialYear;
        mInitialMonth = initialMonth;
        mInitialDayOfMonth = initialDayOfMonth;
    }
}
