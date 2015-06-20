package es.ugr.swad.swadroid.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.Preferences;
import es.ugr.swad.swadroid.database.DataBaseHelper;
import es.ugr.swad.swadroid.utils.Utils;
import es.ugr.swad.swadroid.webservices.IWebserviceClient;

/**
 * A {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public abstract class AbstractService extends Service {
    /**
     * Tag name for Logcat
     */
    public static final String TAG = Constants.APP_TAG + " AbstractService";

    /**
     * Application debuggable flag
     */
    protected static boolean isDebuggable;

    /**
     * Database Helper.
     */
    protected static DataBaseHelper dbHelper;

    /**
     * Application preferences
     */
    protected static Preferences prefs;

    /**
     * Client for SWAD webservices
     */
    protected IWebserviceClient webserviceClient;

    /**
     * Webservice result.
     */
    protected Object result;

    public AbstractService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            //Initialize preferences
            prefs = new Preferences(this);

            //Initialize database
            dbHelper = new DataBaseHelper(this);

            getPackageManager().getApplicationInfo(
                    getPackageName(), 0);
            isDebuggable = (ApplicationInfo.FLAG_DEBUGGABLE != 0);
        } catch (Exception e) {
            Utils.error(this, TAG, e.getMessage(), e, true, isDebuggable);
        }
    }

    /**
     * Gets service parameters from intent
     * @param intent Intent containing service parameters
     */
    protected abstract void getParams(Intent intent);
    /**
     * Connects to SWAD and gets user data.
     */
    protected abstract void requestService() throws Exception;

    /**
     * Returns result of service to caller
     */
    protected abstract void returnResult();

    protected void onError() {
        // Launch database rollback
        if (dbHelper.isDbInTransaction()) {
            Log.e(TAG, "Stopping pending database transaction");
            dbHelper.endTransaction(false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            getParams(intent);
            requestService();
        } catch (Exception e) {
            onError();
            Utils.error(this, TAG, e.getMessage(), e, true, isDebuggable);
        }

        returnResult();
        return super.onStartCommand(intent, flags, startId);
    }
}