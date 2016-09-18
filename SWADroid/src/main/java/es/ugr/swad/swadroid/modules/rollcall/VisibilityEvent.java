package es.ugr.swad.swadroid.modules.rollcall;

import android.os.Bundle;

import org.ksoap2.serialization.SoapPrimitive;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.analytics.SWADroidTracker;
import es.ugr.swad.swadroid.model.Event;
import es.ugr.swad.swadroid.model.User;
import es.ugr.swad.swadroid.modules.Module;
import es.ugr.swad.swadroid.modules.courses.Courses;
import es.ugr.swad.swadroid.modules.login.Login;
import es.ugr.swad.swadroid.webservices.SOAPClient;

/*
 * @author Rubén Martín Hidalgo
 */
public class VisibilityEvent extends Module {
    /**
     * Remove Event tag name for Logcat
     */
    private static final String TAG = Constants.APP_TAG + " VisibilityEvent";
    private int attendanceEventCode;
    private int hidden;
    private long startUnixTime;
    private long endUnixTime;
    private String title;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //recipe date of event

        setMETHOD_NAME("sendAttendanceEvent");
        getSupportActionBar().hide();
        runConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SWADroidTracker.sendScreenView(getApplicationContext(), TAG);
    }

    @Override
    protected void requestService() throws Exception {
        if (hidden == 0)
            hidden = 1;
        else
            hidden = 0;
        createRequest(SOAPClient.CLIENT_TYPE);
        addParam("wsKey", Login.getLoggedUser().getWsKey());
        addParam("attendanceEventCode", attendanceEventCode);
        addParam("courseCode", Courses.getSelectedCourseCode());
        addParam("hidden", hidden); //visibility event
        addParam("startTime", startUnixTime);
        addParam("endTime", endUnixTime);
        addParam("commentsTeachersVisible", 0);
        addParam("title", title);
        addParam("text", description);
        addParam("groups", "");
        sendRequest(Event.class, true);
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
}