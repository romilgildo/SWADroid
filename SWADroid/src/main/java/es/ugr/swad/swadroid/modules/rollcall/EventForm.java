package es.ugr.swad.swadroid.modules.rollcall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Vector;

import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.model.User;
import es.ugr.swad.swadroid.modules.Module;
import es.ugr.swad.swadroid.modules.courses.Courses;
import es.ugr.swad.swadroid.modules.login.Login;
import es.ugr.swad.swadroid.webservices.SOAPClient;

/**
 * Created by Rubén Martín Hidalgo on 05/09/2016.
 */
public class EventForm extends Module {
    String titleBar;
    EditText titleEditText;
    EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        titleBar = getIntent().getStringExtra("titleEventForm");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_form);
        setTitle(titleBar);

        setMETHOD_NAME("sendAttendanceEvent");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_form_actions, menu);

        titleEditText = (EditText) findViewById(R.id.name_text);
        descriptionEditText = (EditText) findViewById(R.id.description_text);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case android.R.id.home:
                showCancelDialog();
                return true;
            case R.id.confirm_event:
                runConnection();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void requestService() throws Exception {
        createRequest(SOAPClient.CLIENT_TYPE);
        int attendanceEventCode = 0; //0 is new event
        addParam("wsKey", Login.getLoggedUser().getWsKey());
        addParam("attendanceEventCode", attendanceEventCode);
        addParam("courseCode", Courses.getSelectedCourseCode());
        addParam("hidden", 0); //visible event
        addParam("startTime", 0);
        addParam("endTime", 0);
        addParam("commentsTeachersVisible", 0);
        addParam("title", titleEditText.getText().toString());
        addParam("text", descriptionEditText.getText().toString());
        addParam("groups", "");
        sendRequest(User.class, true);

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
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

}
