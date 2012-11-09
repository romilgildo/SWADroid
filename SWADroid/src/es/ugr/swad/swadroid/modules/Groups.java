package es.ugr.swad.swadroid.modules;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import es.ugr.swad.swadroid.Global;
import es.ugr.swad.swadroid.model.Course;
import es.ugr.swad.swadroid.model.Group;
import es.ugr.swad.swadroid.model.GroupType;
import es.ugr.swad.swadroid.model.Model;
import es.ugr.swad.swadroid.R;

/**
 * Groups module gets user's groups inside the current course
 * and stores them in the database.
 * It needs as extra data:
 * - (long) courseCode course code . It indicates the course to which the groups belong
 * @author Helena Rodriguez Gijon <hrgijon@gmail.com>
 * @author Antonio Aguilera Malagon <aguilerin@gmail.com>
 */
public class Groups extends Module {
	/**
	 * Groups counter
	 */
	private int numGroups;
	/**
	 * Course code
	 * */
	private long courseCode;
	/**
	 * Groups tag name for Logcat
	 */
	public static final String TAG = Global.APP_TAG + " Groups";
	@Override
	protected void runConnection() {
		super.runConnection();
		if (!isConnected) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		courseCode = getIntent().getLongExtra("courseCode", -1);
		setMETHOD_NAME("getGroups");
	}

	@Override
	protected void onStart() {
		super.onStart(); 
			try {
				runConnection();
			} catch (Exception ex) {
				String errorMsg = getString(R.string.errorServerResponseMsg);
				error(errorMsg);
	
				if(isDebuggable) {
					Log.e(ex.getClass().getSimpleName(), errorMsg);        		
					ex.printStackTrace();
				}
			}
	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_CANCELED) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	@Override
	protected void connect() {
		String progressDescription = getString(R.string.groupsProgressDescription);
		int progressTitle = R.string.groupsProgressTitle;

		new Connect(true, progressDescription, progressTitle).execute();
	}


	@Override
	protected void requestService() throws NoSuchAlgorithmException,
			IOException, XmlPullParserException, SoapFault,
			IllegalAccessException, InstantiationException {
		createRequest();
		addParam("wsKey", Global.getLoggedUser().getWsKey());		
		addParam("courseCode", (int) courseCode);
		sendRequest(Group.class, false);

		if(result != null) {			
			//Stores groups data returned by webservice response
			List<Model> groupsSWAD = new ArrayList<Model>();
			
			Vector<?> res = (Vector <?>) result;
			SoapObject soap = (SoapObject) res.get(1);	
			numGroups = soap.getPropertyCount();			

			for (int i = 0; i < numGroups; i++) {
				SoapObject pii = (SoapObject)soap.getProperty(i);
				long id = Long.parseLong(pii.getProperty("groupCode").toString());
				String groupName = pii.getProperty("groupName").toString();
				long groupTypeCode = Integer.parseInt(pii.getProperty("groupTypeCode").toString());
				int maxStudents = Integer.parseInt(pii.getProperty("maxStudents").toString());
				int open = Integer.parseInt(pii.getProperty("open").toString());
				int numStudents = Integer.parseInt(pii.getProperty("numStudents").toString());
				int fileZones = Integer.parseInt(pii.getProperty("fileZones").toString());
				int member = Integer.parseInt(pii.getProperty("member").toString());
				Group g = new Group(id,groupName,groupTypeCode,maxStudents,open,numStudents,fileZones,member);
				
				groupsSWAD.add(g);
				
				if(isDebuggable){
					Log.i(TAG, g.toString());
        		}
			}

			dbHelper.insertCollection(Global.DB_TABLE_GROUPS, groupsSWAD, courseCode);
			//TODO remove obsolete groups
			/*for(int i = 0; i < groupsSWAD.size(); ++i){
				Group g = (Group) groupsSWAD.get(i);
				//boolean isAdded = dbHelper.insertGroup(g,Global.getSelectedCourseCode());
				//if(!isAdded){
				if(!dbHelper.insertGroup(g,Global.getSelectedCourseCode())){
					dbHelper.updateGroup(g.getId(), Global.getSelectedCourseCode(), g);
				}
			}*/
			//Request finalized without errors
			setResult(RESULT_OK);
		}

	}


	@Override
	protected void postConnect() {
		finish();
	}

	@Override
	protected void onError() {
		// TODO Auto-generated method stub

	}
}
