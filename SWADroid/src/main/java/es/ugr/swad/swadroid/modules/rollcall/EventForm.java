package es.ugr.swad.swadroid.modules.rollcall;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.ksoap2.serialization.SoapObject;
import java.text.ParseException;
import java.util.Date;
import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.model.Event;
import es.ugr.swad.swadroid.modules.Module;
import es.ugr.swad.swadroid.modules.courses.Courses;
import es.ugr.swad.swadroid.modules.login.Login;
import es.ugr.swad.swadroid.utils.Utils;
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
    TextView allGroupsTitle;
    CheckBox allGroupsCheckbox;
    TextView hideCommentsTitle;
    CheckBox hideCommentsCheckbox;
    Button selectGroupsButton;

    int attendanceEventCode;
    int hidden;
    int comments;
    String groups;

    int minute;
    int hour;
    int day;
    int month;
    int year;

    String messageEvent;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        titleBar = getIntent().getStringExtra("titleEventForm");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_form);
        setTitle(titleBar);

        attendanceEventCode = getIntent().getIntExtra("attendanceEventCode", 0); //0 is new event

        titleEditText = (EditText) findViewById(R.id.name_text);
        descriptionEditText = (EditText) findViewById(R.id.description_text);
        initialDateEditText = (EditText) findViewById(R.id.initialDateText);
        initialTimeEditText = (EditText) findViewById(R.id.initialTimeText);
        finalDateEditText = (EditText) findViewById(R.id.finalDateText);
        finalTimeEditText = (EditText) findViewById(R.id.finalTimeText);
        allGroupsTitle = (TextView) findViewById(R.id.text_allGroups);
        allGroupsCheckbox = (CheckBox) findViewById(R.id.check_allGroups);
        hideCommentsTitle = (TextView) findViewById(R.id.text_hideComments);
        hideCommentsCheckbox = (CheckBox) findViewById(R.id.check_hideComments);
        selectGroupsButton = (Button) findViewById(R.id.groups_button);

        groups = "";

        if(attendanceEventCode != 0){
            titleEditText.setText(getIntent().getStringExtra("title"));
            descriptionEditText.setText(getIntent().getStringExtra("text"));
            initialDateEditText.setText(getIntent().getStringExtra("startDate"));
            initialTimeEditText.setText(getIntent().getStringExtra("startTime"));
            finalDateEditText.setText(getIntent().getStringExtra("endDate"));
            finalTimeEditText.setText(getIntent().getStringExtra("endTime"));

            hidden = getIntent().getIntExtra("hidden", 0);
            comments = getIntent().getIntExtra("commentsVisible", 0);
            groups = getIntent().getStringExtra("groups");

        }else{ //new event
            Calendar startTimeCalendar = Calendar.getInstance();
            startTimeCalendar.setTimeInMillis(System.currentTimeMillis());
            Calendar endTimeCalendar = Calendar.getInstance();
            endTimeCalendar.setTimeInMillis(System.currentTimeMillis() + 7200 * 1000L); //two hours later

            SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

            String startDate = formatDate.format(startTimeCalendar.getTime());
            String startTime = formatTime.format(startTimeCalendar.getTime());
            String endDate = formatDate.format(endTimeCalendar.getTime());
            String endTime = formatTime.format(endTimeCalendar.getTime());

            initialDateEditText.setText(startDate);
            initialTimeEditText.setText(startTime);
            finalDateEditText.setText(endDate);
            finalTimeEditText.setText(endTime);
        }

        setMETHOD_NAME("sendAttendanceEvent");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_form_actions, menu);

        initialDateEditText.setInputType(InputType.TYPE_NULL);
        initialDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    try {
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
                        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(initialDateEditText.getText().toString());
                        c.setTime(date);
                        day = c.get(Calendar.DAY_OF_MONTH) + 1;
                        month = c.get(Calendar.MONTH);
                        year = c.get(Calendar.YEAR);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DialogFragment newFragment = new DateSelector(day, month, year) {
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    try {
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
                        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(finalDateEditText.getText().toString());
                        c.setTime(date);
                        day = c.get(Calendar.DAY_OF_MONTH) + 1;
                        month = c.get(Calendar.MONTH);
                        year = c.get(Calendar.YEAR);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DialogFragment newFragment = new DateSelector(day, month, year) {
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
            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    try {
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
                        Date time = new SimpleDateFormat("HH:mm").parse(initialTimeEditText.getText().toString());
                        c.setTime(time);
                        hour = c.get(Calendar.HOUR_OF_DAY);
                        minute = c.get(Calendar.MINUTE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DialogFragment newFragment = new TimeSelector(hour, minute) {
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
            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    try {
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
                        Date time = new SimpleDateFormat("HH:mm").parse(finalTimeEditText.getText().toString());
                        c.setTime(time);
                        hour = c.get(Calendar.HOUR_OF_DAY);
                        minute = c.get(Calendar.MINUTE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DialogFragment newFragment = new TimeSelector(hour, minute) {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if (minute < 10)
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

        if(comments == 0)
            hideCommentsCheckbox.setChecked(Boolean.TRUE);
        else
            hideCommentsCheckbox.setChecked(Boolean.FALSE);
        hideCommentsTitle.setText(getResources().getString(R.string.hideComments));

        hideCommentsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                comments = Utils.parseBoolInt(!isChecked);
            }
        });

        if(groups.isEmpty()) {
            allGroupsCheckbox.setChecked(Boolean.TRUE);
            selectGroupsButton.setVisibility(View.GONE);
        }else {
            allGroupsCheckbox.setChecked(Boolean.FALSE);
            selectGroupsButton.setVisibility(View.VISIBLE);
        }

        String text = getResources().getString(R.string.allGroups).toString().replace("##subjectName##", Courses.getSelectedCourseShortName());
        allGroupsTitle.setText(text);

        allGroupsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    selectGroupsButton.setVisibility(View.GONE);
                else
                    selectGroupsButton.setVisibility(View.VISIBLE);
            }
        });

        selectGroupsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activity = new Intent(EventForm.this, SelectGroups.class);
                activity.putExtra("courseCode", Courses.getSelectedCourseCode());
                activity.putExtra("groups", groups);
                startActivityForResult(activity, Constants.SELECT_GROUPS_REQUEST_CODE);
            }
        });

        //hide keyboard when activity starts
        hideSoftKeyboard();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showCancelDialog();
                return true;
            case R.id.confirm_event:
                if (titleEditText.getText().length() == 0)
                    Toast.makeText(this, getString(R.string.noEventTitle), Toast.LENGTH_LONG).show();
                else
                    runConnection();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.SELECT_GROUPS_REQUEST_CODE:
                    groups = intent.getStringExtra("groups");
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void requestService() throws Exception {
        createRequest(SOAPClient.CLIENT_TYPE);

        DateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm");
        Date initialDate = formatter.parse(initialDateEditText.getText().toString() + " " + initialTimeEditText.getText().toString());
        Date finalDate = formatter.parse(finalDateEditText.getText().toString() + " " + finalTimeEditText.getText().toString());

        long startUnixTime = (long) initialDate.getTime() / 1000;
        long endUnixTime = (long) finalDate.getTime() / 1000;

        addParam("wsKey", Login.getLoggedUser().getWsKey());
        addParam("attendanceEventCode", attendanceEventCode);
        addParam("courseCode", Courses.getSelectedCourseCode());
        addParam("hidden", hidden);
        addParam("startTime", startUnixTime);
        addParam("endTime", endUnixTime);
        addParam("commentsTeachersVisible", comments);
        addParam("title", titleEditText.getText().toString());
        addParam("text", descriptionEditText.getText().toString());
        addParam("groups", groups);
        sendRequest(Event.class, true);

        if (result != null) {
            SoapObject soap = (SoapObject) result;
            if (attendanceEventCode == 0)
                messageEvent = getString(R.string.eventCreated);
            else
                messageEvent = getString(R.string.eventEdited);
            attendanceEventCode = Integer.parseInt(soap.getProperty("attendanceEventCode").toString());
        }

        if (attendanceEventCode > 0)
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
        Toast.makeText(this, messageEvent, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra("updateEvents", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onError() {
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventForm.this);
        builder.setTitle(R.string.areYouSure);
        if (titleBar.equals(R.string.actionBarNewEvent))
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        showCancelDialog();
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }
}
