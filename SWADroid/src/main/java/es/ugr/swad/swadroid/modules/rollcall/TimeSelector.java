package es.ugr.swad.swadroid.modules.rollcall;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.TimePicker;

/**
 * Created by Rubén Martín Hidalgo on 07/09/2016.
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class TimeSelector extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    int hour;
    int minute;

    public TimeSelector(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {

    }
}
