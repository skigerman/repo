package com.example.homework_252_mehcss;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.homework_252_mehcss.data.TaskDbAdapter;

/**
 * A fragment representing a single Task detail screen. This fragment is either
 * contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 */
public class TaskDetailFragment extends Fragment {
	
	private TaskDbAdapter mDbHelper;
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The task content this fragment is presenting.
	 */
	String mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mDbHelper = new TaskDbAdapter(getActivity());
        mDbHelper.open();

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the task content specified by the fragment
			// arguments. 
			Cursor c = mDbHelper.fetchTask(Long.valueOf(getArguments().getString(
					ARG_ITEM_ID)));
			if (c.moveToFirst()) {
			    mItem = c.getString(c.getColumnIndex("title")); //could use body logic here
			} 
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_task_detail,
				container, false);

		// Show the task content as text in a TextView.
		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.task_detail))
					.setText(mItem);
		}

		return rootView;
	}
	
	/**
	 * Menu Functionality to delete a task
	 */
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater ) {
	    inflater.inflate(R.menu.task_detail_fragment, menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.delete:
            deleteTask();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager(); 
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(this).commit();
            return true;
        }
       
        return super.onOptionsItemSelected(item);
    }

	private void deleteTask() {
		mDbHelper.deleteTask(Long.valueOf(getArguments().getString(
				ARG_ITEM_ID)));	
	}
}
