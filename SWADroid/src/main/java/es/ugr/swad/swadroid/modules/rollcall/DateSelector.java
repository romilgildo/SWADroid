package es.ugr.swad.swadroid.modules.rollcall;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.DatePicker;

/**
 * Created by Ruben Martín Hidalgo on 06/09/2016.
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class DateSelector extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    int year;
    int month;
    int day;


    public DateSelector(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
    }
}

