package es.ugr.swad.swadroid.modules.rollcall;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;

import java.util.Date;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.model.Event;
import es.ugr.swad.swadroid.modules.Module;
import es.ugr.swad.swadroid.modules.courses.Courses;
import es.ugr.swad.swadroid.modules.login.Login;
import es.ugr.swad.swadroid.webservices.SOAPClient;

/**
 * Created by Rubén Martín Hidalgo on 05/09/2016.
 */
public class EventForm extends Module {

    private static final String TAG = Constants.APP_TAG + " EventForm";

    String titleBar;
    EditText titleEditText;
    EditText descriptionEditText;
    EditText initialDateEditText;
    EditText finalDateEditText;
    EditText initialTimeEditText;
    EditText finalTimeEditText;

    int minute;
    int hour;
    int day;
    int month;
    int year;

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        titleBar = getIntent().getStringExtra("titleEventForm");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_form);
        setTitle(titleBar);

        titleEditText = (EditText) findViewById(R.id.name_text);
        descriptionEditText = (EditText) findViewById(R.id.description_text);
        initialDateEditText = (EditText) findViewById(R.id.initialDateText);
        finalDateEditText = (EditText) findViewById(R.id.finalDateText);
        initialTimeEditText = (EditText) findViewById(R.id.initialTimeText);
        finalTimeEditText = (EditText) findViewById(R.id.finalTimeText);

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        initialDateEditText.setText(day + "/" + month + "/" + year);
        finalDateEditText.setText(day + "/" + month + "/" + year);

        if(minute  < 10) {
            initialTimeEditText.setText(hour + ":" + "0" + minute);
            finalTimeEditText.setText(hour + ":" + "0" + minute);
        }
        else {
            initialTimeEditText.setText(hour + ":" + minute);
            finalTimeEditText.setText(hour + ":" + minute);
        }

        setMETHOD_NAME("sendAttendanceEvent");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_form_actions, menu);

        initialDateEditText.setInputType(InputType.TYPE_NULL);
        initialDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DialogFragment newFragment = new DateSelector() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            int finalMonth = month + 1;
                            initialDateEditText.setText(day + "/" + finalMonth + "/" + year);
                        }
                    };
                    newFragment.show(getFragmentManager(), "datePicker");
                    initialDateEditText.clearFocus();
                }
            }
        });

        finalDateEditText.setInputType(InputType.TYPE_NULL);
        finalDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DialogFragment newFragment = new DateSelector() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            int finalMonth = month + 1;
                            finalDateEditText.setText(day + "/" + finalMonth + "/" + year);
                        }
                    };
                    newFragment.show(getFragmentManager(), "datePicker");
                    finalDateEditText.clearFocus();
                }
            }
        });

        initialTimeEditText.setInputType(InputType.TYPE_NULL);
        initialTimeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DialogFragment newFragment = new TimeSelector() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if (minute < 10)
                                initialTimeEditText.setText(hourOfDay + ":" + "0" + minute);
                            else
                                initialTimeEditText.setText(hourOfDay + ":" + minute);
                        }
                    };
                    newFragment.show(getFragmentManager(), "timePicker");
                    initialTimeEditText.clearFocus();
                }
            }
        });

        finalTimeEditText.setInputType(InputType.TYPE_NULL);
        finalTimeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DialogFragment newFragment = new TimeSelector() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if(minute  < 10)
                                finalTimeEditText.setText(hourOfDay + ":" + "0" + minute);
                            else
                                finalTimeEditText.setText(hourOfDay + ":" + minute);
                        }
                    };
                    newFragment.show(getFragmentManager(), "timePicker");
                    finalTimeEditText.clearFocus();
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showCancelDialog();
                return true;
            case R.id.confirm_event:
                if(titleEditText.getText().length() == 0)
                    Toast.makeText(this, getString(R.string.noEventTitle), Toast.LENGTH_LONG).show();
                else
                    runConnection();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void requestService() throws Exception {
        createRequest(SOAPClient.CLIENT_TYPE);
        int attendanceEventCode = 0; //0 is new event

        DateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm");
        Date initialDate = formatter.parse(initialDateEditText.getText().toString() + " " + initialTimeEditText.getText().toString());
        Date finalDate = formatter.parse(finalDateEditText.getText().toString() + " " + finalTimeEditText.getText().toString());

        long startUnixTime = (long) initialDate.getTime() / 1000 - 7200;
        long endUnixTime = (long) finalDate.getTime() / 1000 - 7200;

        addParam("wsKey", Login.getLoggedUser().getWsKey());
        addParam("attendanceEventCode", attendanceEventCode);
        addParam("courseCode", Courses.getSelectedCourseCode());
        addParam("hidden", 0); //visible event
        addParam("startTime", startUnixTime);
        addParam("endTime", endUnixTime);
        addParam("commentsTeachersVisible", 0);
        addParam("title", titleEditText.getText().toString());
        addParam("text", descriptionEditText.getText().toString());
        addParam("groups", "");
        sendRequest(Event.class, true);

        if (result != null) {
            SoapObject soap = (SoapObject) result;
            attendanceEventCode = Integer.parseInt(soap.getProperty("attendanceEventCode").toString());
        }

        if(attendanceEventCode > 0)
            setResult(RESULT_OK);
        else
            setResult(RESULT_CANCELED);
    }

    @Override
    protected void connect() {
        startConnection();
    }

    @Override
    protected void postConnect() {
        Toast.makeText(this, getString(R.string.eventCreated), Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra("updateEvents", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onError() {
    }

    private void showCancelDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EventForm.this);
        builder.setTitle(R.string.areYouSure);
        if(titleBar.equals(R.string.actionBarNewEvent))
            builder.setMessage(R.string.cancelNewEventForm);
        else
            builder.setMessage(R.string.cancelEditEventForm);

        builder.setNegativeButton(getString(R.string.cancelMsg), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setPositiveButton(getString(R.string.discardMsg), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.putExtra("updateEvents", false);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

}
