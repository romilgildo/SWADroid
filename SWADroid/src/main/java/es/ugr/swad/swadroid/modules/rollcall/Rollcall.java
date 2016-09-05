/*
 *  This file is part of SWADroid.
 *
 *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 *
 *  SWADroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SWADroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.ugr.swad.swadroid.modules.rollcall;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

import es.ugr.swad.swadroid.Constants;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.analytics.SWADroidTracker;
import es.ugr.swad.swadroid.gui.DialogFactory;
import es.ugr.swad.swadroid.gui.MenuExpandableListActivity;
import es.ugr.swad.swadroid.gui.ProgressScreen;
import es.ugr.swad.swadroid.modules.courses.Courses;

/**
 * Rollcall module.
 *
 * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 */
public class Rollcall extends MenuExpandableListActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
    * Rollcall tag name for Logcat
    */
    private static final String TAG = Constants.APP_TAG + " Rollcall";
    /**
    * ListView of events
    */
    private static ListView lvEvents;
    /**
    * Adapter for ListView of events
    */
    private static EventsCursorAdapter adapter;
    private final RefreshAdapterHandler mHandler = new RefreshAdapterHandler(this);
    private final Runnable mRunnable = new Runnable() {
    @Override
    public void run() {
        /*
        Database cursor for Adapter of events
        */
        Cursor dbCursor = dbHelper.getEventsCourseCursor(Courses.getSelectedCourseCode());
        startManagingCursor(dbCursor);


                /*
                 * If there aren't events to show, hide the events lvEvents
                 * and show the empty events message
                 */
        if ((dbCursor == null) || (dbCursor.getCount() == 0)) {
            Log.d(TAG, "Events list is empty");

            emptyEventsTextView.setText(R.string.eventsEmptyListMsg);
            emptyEventsTextView.setVisibility(View.VISIBLE);

            lvEvents.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Events list is not empty");

            emptyEventsTextView.setVisibility(View.GONE);
            lvEvents.setVisibility(View.VISIBLE);
        }

        adapter = new EventsCursorAdapter(getBaseContext(), dbCursor, dbHelper);
        lvEvents.setAdapter(adapter);

        mProgressScreen.hide();
    }
    };
    /**
    * TextView for the empty events message
    */
    private TextView emptyEventsTextView;
    /**
    * Layout with "Pull to refresh" function
    */
    private SwipeRefreshLayout refreshLayout;
    /**
    * Progress screen
    */
    private ProgressScreen mProgressScreen;

    /* (non-Javadoc)
    * @see es.ugr.swad.swadroid.MenuExpandableListActivity#onCreate(android.os.Bundle)
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_items_pulltorefresh);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container_list);
        emptyEventsTextView = (TextView) findViewById(R.id.list_item_title);

        View mProgressScreenView = findViewById(R.id.progress_screen);
        mProgressScreen = new ProgressScreen(mProgressScreenView, refreshLayout,
                getString(R.string.loadingMsg), this);

        lvEvents = (ListView) findViewById(R.id.list_pulltorefresh);

        lvEvents.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                               int visibleItemCount, int totalItemCount) {

            boolean enable = true;
            if ((lvEvents != null) && (lvEvents.getChildCount() > 0)) {
                // check if the first item of the list is visible
                boolean firstItemVisible = lvEvents.getFirstVisiblePosition() == 0;
                // check if the top of the first item is visible
                boolean topOfFirstItemVisible = lvEvents.getChildAt(0).getTop() == 0;
                // enabling or disabling the refresh layout
                enable = firstItemVisible && topOfFirstItemVisible;
            }
            refreshLayout.setEnabled(enable);
            }
        });

        refreshLayout.setOnRefreshListener(this);
        setAppearance();

        getSupportActionBar().setSubtitle(Courses.getSelectedCourseShortName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /* (non-Javadoc)
    * @see es.ugr.swad.swadroid.MenuExpandableListActivity#Override(android.os.Bundle)
    */
    @Override
    protected void onStart() {
        super.onStart();
        SWADroidTracker.sendScreenView(getApplicationContext(), TAG);

        //Refresh ListView of events
        refreshAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case Constants.ROLLCALL_EVENTS_DOWNLOAD_REQUEST_CODE:
                refreshAdapter();
                break;
            case Constants.EVENT_FORM_REQUEST_CODE:
                refreshEvents();
                Toast.makeText(this, getString(R.string.eventCreated), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_list_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newEvent:
                try {
                    Intent intent = new Intent (Rollcall.this, EventForm.class);
                    intent.putExtra("titleEventForm", getResources().getString(R.string.actionBarNewEvent));
                    startActivityForResult(intent, Constants.EVENT_FORM_REQUEST_CODE);
                } catch (Exception e) {
                    String errorMsg = getString(R.string.errorServerResponseMsg);
                    error(errorMsg, e, true);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAdapter() {
    mHandler.post(mRunnable);
    }

    private void refreshEvents() {
        mProgressScreen.show();
        Intent activity = new Intent(this, EventsDownload.class);
        startActivityForResult(activity, Constants.ROLLCALL_EVENTS_DOWNLOAD_REQUEST_CODE);
    }

    /**
    * It shows the SwipeRefreshLayout progress
    */
    private void showSwipeProgress() {
    refreshLayout.setRefreshing(true);
    }

    /**
    * It shows the SwipeRefreshLayout progress
    */
    private void hideSwipeProgress() {
    refreshLayout.setRefreshing(false);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setAppearance() {
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private boolean hasPendingEvents() {
        boolean hasPendingEvents = false;
        TextView sendingStateTextView;
        int i = 0;

        if ((lvEvents != null) && (lvEvents.getChildCount() > 0)) {
            while(!hasPendingEvents && (i<lvEvents.getChildCount())) {
                sendingStateTextView = (TextView) lvEvents.getChildAt(i).findViewById(R.id.sendingStateTextView);
                hasPendingEvents = sendingStateTextView.getText().equals(getString(R.string.sendingStatePending));
                i++;
            }
        }

        return hasPendingEvents;
    }

    private void updateEvents() {
        showSwipeProgress();
        refreshEvents();
        hideSwipeProgress();
    }

    /**
    * It must be overriden by parent classes if manual swipe is enabled.
    */
    @Override
    public void onRefresh() {
        if(!hasPendingEvents()) {
            updateEvents();
        } else {
            AlertDialog cleanEventsDialog = DialogFactory.createWarningDialog(this,
                    -1,
                    R.string.areYouSure,
                    R.string.updatePendingEventsMsg,
                    R.string.yesMsg,
                    R.string.noMsg,
                    true,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                            updateEvents();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    },
                    null);

            cleanEventsDialog.show();
        }

        hideSwipeProgress();
    }

    private static class RefreshAdapterHandler extends Handler {

        private final WeakReference<Rollcall> mActivity;

        public RefreshAdapterHandler(Rollcall activity) {
          mActivity = new WeakReference<>(activity);
        }
    }

    public void onClickEvent(View v) {
        int position = lvEvents.getPositionForView(v);

        Intent activity = new Intent(getApplicationContext(), UsersActivity.class);
        activity.putExtra("attendanceEventCode", (int) adapter.getItemId(position));
        startActivity(activity);
    }

    public void openOptions(View v) {
        int numEvents = lvEvents.getChildCount();
        int position = lvEvents.getPositionForView(v);
        View layoutView = (RelativeLayout)v.getParent().getParent();
        ImageButton openOptionsCurrent = (ImageButton) layoutView.findViewById(R.id.openEventOptions);
        ImageButton closeOptionsCurrent = (ImageButton) layoutView.findViewById(R.id.closeEventOptions);
        LinearLayout eventOptionsCurrent = (LinearLayout) layoutView.findViewById(R.id.optionsButtons);

        //recharge the listview to close all option buttons
        lvEvents.invalidateViews();

        for (int i=0; i < numEvents; i++){
            ImageButton openOptions = (ImageButton) lvEvents.getChildAt(i).findViewById(R.id.openEventOptions);
            ImageButton closeOptions = (ImageButton) lvEvents.getChildAt(i).findViewById(R.id.closeEventOptions);
            LinearLayout eventOptions = (LinearLayout) lvEvents.getChildAt(i).findViewById(R.id.optionsButtons);

            openOptions.setVisibility(View.VISIBLE);
            closeOptions.setVisibility(View.GONE);
            eventOptions.setVisibility(View.GONE);
        }

        openOptionsCurrent.setVisibility(View.GONE);
        closeOptionsCurrent.setVisibility(View.VISIBLE);
        eventOptionsCurrent.setVisibility(View.VISIBLE);

        //if it's the last event of list, screen scroll to the end
        if(position == lvEvents.getCount()-1)
            lvEvents.setSelection(adapter.getCount()-1);
    }

    public void closeOptions(View v) {
        View layoutView = (RelativeLayout) v.getParent().getParent();
        ImageButton openOptionsCurrent = (ImageButton) layoutView.findViewById(R.id.openEventOptions);
        ImageButton closeOptionsCurrent = (ImageButton) layoutView.findViewById(R.id.closeEventOptions);
        LinearLayout eventOptionsCurrent = (LinearLayout) layoutView.findViewById(R.id.optionsButtons);

        openOptionsCurrent.setVisibility(View.VISIBLE);
        closeOptionsCurrent.setVisibility(View.GONE);
        eventOptionsCurrent.setVisibility(View.GONE);
    }

    public void showDeleteDialog (View v){
        final int position = lvEvents.getPositionForView(v);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(Rollcall.this);
        builder.setTitle(R.string.areYouSure);

        View layoutView = (LinearLayout) v.getParent().getParent();
        TextView nameTextView = (TextView) layoutView.findViewById(R.id.toptext);
        final String nameEvent = nameTextView.getText().toString();

        String dialog = getResources().getString(R.string.removeEvent);
        dialog = dialog.replaceAll("#nameEvent#", "\"" + nameEvent + "\"");
        builder.setMessage(dialog);

        builder.setNegativeButton(getString(R.string.cancelMsg), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setPositiveButton(getString(R.string.acceptMsg), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteEvent((int) adapter.getItemId(position), nameEvent);
            }
        });

        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteEvent(int eventCode, String nameEvent) {
        String text = getResources().getString(R.string.eventRemoved);
        text = text.replaceAll("#nameEvent#", "\"" + nameEvent + "\"");
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void hideEvent(View v) {
        View layoutView = (LinearLayout) v.getParent();
        ImageButton showEventCurrent = (ImageButton) layoutView.findViewById(R.id.showEvent);
        ImageButton hideEventCurrent = (ImageButton) layoutView.findViewById(R.id.hideEvent);

        layoutView = (LinearLayout) v.getParent().getParent();
        TextView nameTextView = (TextView) layoutView.findViewById(R.id.toptext);

        showEventCurrent.setVisibility(View.GONE);
        hideEventCurrent.setVisibility(View.VISIBLE);
        nameTextView.setTextColor(Color.GRAY);
    }

    public void showEvent(View v) {
        View layoutView = (LinearLayout) v.getParent();
        ImageButton showEventCurrent = (ImageButton) layoutView.findViewById(R.id.showEvent);
        ImageButton hideEventCurrent = (ImageButton) layoutView.findViewById(R.id.hideEvent);

        layoutView = (LinearLayout) v.getParent().getParent();
        TextView nameTextView = (TextView) layoutView.findViewById(R.id.toptext);

        showEventCurrent.setVisibility(View.VISIBLE);
        hideEventCurrent.setVisibility(View.GONE);
        nameTextView.setTextColor(Color.BLACK);
    }

    public void editEvent(View v) {
        //crear actividad con formulario de evento y luego llamar a la función sendAttendanceEvent;
    }
}
