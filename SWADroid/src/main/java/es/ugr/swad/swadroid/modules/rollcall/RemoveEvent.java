package es.ugr.swad.swadroid.modules.rollcall;

import android.os.Bundle;

import org.ksoap2.serialization.SoapPrimitive;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.analytics.SWADroidTracker;
import es.ugr.swad.swadroid.model.Event;
import es.ugr.swad.swadroid.modules.Module;
import es.ugr.swad.swadroid.modules.login.Login;
import es.ugr.swad.swadroid.webservices.SOAPClient;

/*
 * @author Rubén Martín Hidalgo
 */
public class RemoveEvent extends Module {
    /**
     * Remove Event tag name for Logcat
     */
    private static final String TAG = Constants.APP_TAG + " RemoveEvent";

    private int eventCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventCode = this.getIntent().getIntExtra("eventCode", 0);

        setMETHOD_NAME("removeAttendanceEvent");
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
        createRequest(SOAPClient.CLIENT_TYPE);
        addParam("wsKey", Login.getLoggedUser().getWsKey());
        addParam("attendanceEventCode", eventCode);
        sendRequest(Event.class, false);

        if (result != null) {
            SoapPrimitive soap = (SoapPrimitive) result;
            int eventCodeRemoved = Integer.parseInt(soap.toString());

            if (eventCode == eventCodeRemoved)
                setResult(RESULT_OK);
            else
                setResult(RESULT_CANCELED);
        }
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