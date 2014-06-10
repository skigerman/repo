package biz.cssconsulting.TripMileage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import biz.cssconsulting.TripMileage.SettingsActivity;
import biz.cssconsulting.TripMileage.data.TaskDbAdapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * An activity representing a list of Tasks. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link TaskDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TaskListFragment} and the item details (if present) is a
 * {@link TaskDetailFragment}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity implements
		TaskListFragment.Callbacks, TaskDetailFragment.Callbacks {
	
	String outFileName = "TripMileage.csv";
	private static final String TAG = "TaskListActivity";
	private TaskDbAdapter mDbHelper;
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	// key for adding DetailFragment to this Activity
		private static final String EDIT_TAG = "Edit";
		private static final String LIST_TAG = "List";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		if (findViewById(R.id.task_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
			
			TaskListFragment fragment = new TaskListFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_list_container, fragment, LIST_TAG).commit();

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			//((TaskListFragment) getSupportFragmentManager().findFragmentById(
			//		R.id.task_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link TaskListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(TaskDetailFragment.ARG_ITEM_ID, id);
			TaskDetailFragment fragment = new TaskDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_detail_container, fragment, EDIT_TAG).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, TaskDetailActivity.class);
			detailIntent.putExtra(TaskDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	/**
	 * Menu Functionality
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task_list_activity, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.email:
			backupDatabaseCSV(outFileName);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("static-access")
	private Boolean backupDatabaseCSV(String outFileName) {
		Log.d(TAG, "backupDatabaseCSV");
		Boolean returnCode = false;
		int i = 0;
		String csvHeader = "";
		String csvValues = "";
		for (i = 0; i < mDbHelper.TASK_COLUMN_NAMES.length; i++) {
			if (csvHeader.length() > 0) {
				csvHeader += ",";
			}
			csvHeader += "\"" + mDbHelper.TASK_COLUMN_NAMES[i] + "\"";
		}

		csvHeader += "\n";
		Log.d(TAG, "header=" + csvHeader);
		mDbHelper = new TaskDbAdapter(this);
		mDbHelper.open();
		try {
		    File path = Environment.getExternalStorageDirectory();
		    File file = new File(path, outFileName);
            FileWriter fileWriter = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fileWriter);
			Cursor c = mDbHelper.fetchAllTasks();
			if (c != null) {
				out.write(csvHeader);
				while (c.moveToNext()) {
					csvValues = "\"" + c.getString(c.getColumnIndex("title"))
							+ "\",";
					csvValues += "\"" + c.getString(c.getColumnIndex("date"))
							+ "\",";
					csvValues += "\""
							+ c.getString(c.getColumnIndex("startMileage"))
							+ "\",";
					csvValues += "\""
							+ c.getString(c.getColumnIndex("endMileage"))
							+ "\",";
					csvValues += "\""
							+ c.getString(c.getColumnIndex("reimbursement"))
							+ "\"" + "\n";
					out.write(csvValues);
				}
				c.close();
			}
			out.close();
			emailReport(file, getEmail());
			returnCode = true;
		} catch (IOException e) {
			returnCode = false;
			Log.d(TAG, "IOException: " + e.getMessage());
		}
		mDbHelper.close();
		return returnCode;
	}

	private void emailReport(File file, String emailAddress) {
		String to = emailAddress;
        String subject = "Mileage Reimbursement";
        String message = "Please see attached Mileage Reimbursement spreadsheet";

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("plain/text");
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(i, "E-mail"));
	}

	@Override
	public void onItemDeleted() {
		// remove the TaskDetailFragment after a deletion for two-pane
		if (mTwoPane) {
			TaskDetailFragment fragment = (TaskDetailFragment) getSupportFragmentManager()
					.findFragmentByTag(EDIT_TAG);
			getSupportFragmentManager().beginTransaction().remove(fragment)
					.commit();
		}
	}

	@Override
	public void onItemAdded(long longID) {
		// remove the TaskDetailFragment after a new task is added for two-pane
		if (mTwoPane) {
			Bundle arguments = new Bundle();
			String id = Long.toString(longID);
			arguments.putString(TaskDetailFragment.ARG_ITEM_ID, id);
			TaskDetailFragment fragment = new TaskDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_detail_container, fragment, EDIT_TAG)
					.commit();
		}

	}

	@Override
	public void onItemUpdated(String id) {
		if (mTwoPane) {
			TaskDetailFragment fragmentTD = (TaskDetailFragment) getSupportFragmentManager()
					.findFragmentByTag(EDIT_TAG);
			getSupportFragmentManager().beginTransaction().remove(fragmentTD)
					.commit();
			TaskListFragment fragmentTL = new TaskListFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_list_container, fragmentTL, LIST_TAG)
					.commit();
		}
	}
	
	public void setRate(float value) {
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat("rate", value);
		editor.commit();
	}
	
	public void setEmail(String value) {
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("email", value);
		editor.commit();
	}
	public String getEmail() {
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(this.getApplication());
		String email = settings.getString("email", "");
		return email;
	}

}
