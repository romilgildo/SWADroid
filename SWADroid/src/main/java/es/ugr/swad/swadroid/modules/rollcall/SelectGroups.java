package es.ugr.swad.swadroid.modules.rollcall;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.analytics.SWADroidTracker;
import es.ugr.swad.swadroid.database.DataBaseHelper;
import es.ugr.swad.swadroid.gui.MenuExpandableListActivity;
import es.ugr.swad.swadroid.gui.ProgressScreen;
import es.ugr.swad.swadroid.model.Group;
import es.ugr.swad.swadroid.model.Model;
import es.ugr.swad.swadroid.modules.groups.EnrollmentExpandableListAdapter;
import es.ugr.swad.swadroid.modules.groups.GroupTypes;
import es.ugr.swad.swadroid.modules.groups.Groups;
import es.ugr.swad.swadroid.modules.login.Login;

public class SelectGroups extends MenuExpandableListActivity {

    /**
     * Tests tag name for Logcat
     */
    private static final String TAG = Constants.APP_TAG + " Groups Events";

    /**
     * Course code of current selected course
     */
    private long courseCode = -1;

    private ArrayList<Model> groupTypes;

    /**
     * ActionBar menu
     */
    private Menu menu;

    private DialogInterface.OnClickListener cancelClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
        }
    };

    private ExpandableListView mExpandableListView;

    /**
     * Progress screen
     */
    private ProgressScreen mProgressScreen;

    private boolean groupTypesRequested = false;

    @Override
    protected void onStart() {
        super.onStart();

        showProgressLoading();

        SWADroidTracker.sendScreenView(getApplicationContext(), TAG);

        List<Model> groupTypes = dbHelper.getAllRows(DataBaseHelper.DB_TABLE_GROUP_TYPES, "courseCode = " + courseCode, "groupTypeName");
        List<Group> groups = dbHelper.getGroups(courseCode);
        if ((!groupTypes.isEmpty()) && (!groups.isEmpty())) {
            setMenu();
        } else {
            if (groupTypes.size() != 0) {
                Intent activity = new Intent(this, Groups.class);
                activity.putExtra("courseCode", courseCode);
                startActivityForResult(activity, Constants.GROUPS_REQUEST_CODE);
            } else {
                if (!groupTypesRequested) {
                    Intent activity = new Intent(this, GroupTypes.class);
                    activity.putExtra("courseCode", courseCode);
                    startActivityForResult(activity, Constants.GROUPTYPES_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                showProgressLoading();

                Intent activity = new Intent(this, GroupTypes.class);
                activity.putExtra("courseCode", courseCode);
                startActivityForResult(activity, Constants.GROUPTYPES_REQUEST_CODE);

                return true;

            case R.id.action_save:
                String groups = ((EnrollmentExpandableListAdapter) mExpandableListView.getExpandableListAdapter()).getChosenGroupCodesAsString();
                Intent intent = new Intent();
                intent.putExtra("groups", groups);
                setResult(RESULT_OK, intent);
                finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setMenu() {
        groupTypes = (ArrayList<Model>) dbHelper.getAllRows(DataBaseHelper.DB_TABLE_GROUP_TYPES, "courseCode =" + String.valueOf(courseCode), "groupTypeName");
        LongSparseArray<ArrayList<Group>> children = getHashMapGroups(groupTypes);
        int currentRole = Login.getCurrentUserRole();
        EnrollmentExpandableListAdapter adapter = new EnrollmentExpandableListAdapter(this, groupTypes, children, R.layout.group_type_list_item, R.layout.group_list_item, currentRole);
        mExpandableListView.setAdapter(adapter);

        int collapsedGroups = mExpandableListView.getExpandableListAdapter().getGroupCount();
        for (int i = 0; i < collapsedGroups; ++i) {
            mExpandableListView.expandGroup(i);
        }

        mProgressScreen.hide();
    }

    private LongSparseArray<ArrayList<Group>> getHashMapGroups(ArrayList<Model> groupTypes) {
        LongSparseArray<ArrayList<Group>> children = new LongSparseArray<>();
        for (Model groupType : groupTypes) {
            long groupTypeCode = groupType.getId();
            ArrayList<Group> groups = (ArrayList<Group>) dbHelper.getGroupsOfType(groupTypeCode);
            children.put(groupTypeCode, groups);
        }
        return children;
    }

    private void showProgressLoading() {
        mProgressScreen.setMessage(getString(R.string.loadingMsg));
        mProgressScreen.show();
    }
}
