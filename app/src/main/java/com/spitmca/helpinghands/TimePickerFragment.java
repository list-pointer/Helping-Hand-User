package com.spitmca.helpinghands;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    public TextView textView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c= Calendar.getInstance();
        int hour=c.get(Calendar.HOUR_OF_DAY);
        int minute =c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT,(TimePickerDialog.OnTimeSetListener)getActivity(),hour,minute,false);
    }

    public TimePickerFragment(TextView tv)
    {
        textView=tv;
    }

    public TextView getTextView()
    {
        return textView;
    }
}
