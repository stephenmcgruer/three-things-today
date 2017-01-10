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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

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

        // Disallow future dates. For Lollipop, we need to set the timestamp to the last second
        // of the last allowed day, or that day will be visible but not selectable.
        // TODO(smcgruer): I don't think this actually works. Need to test on real device.
        final Calendar maxCal = Calendar.getInstance();
        maxCal.set(Calendar.HOUR, 23);
        maxCal.set(Calendar.MINUTE, 59);
        maxCal.set(Calendar.SECOND, 59);
        dialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());

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
