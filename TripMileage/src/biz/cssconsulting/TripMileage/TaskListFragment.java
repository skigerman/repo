package biz.cssconsulting.TripMileage;

import java.text.SimpleDateFormat;
import java.util.Date;

import biz.cssconsulting.TripMileage.data.TaskDbAdapter;

import biz.cssconsulting.TripMileage.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * A list fragment representing a list of Tasks. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link TaskDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TaskListFragment extends ListFragment {
	
	private static final int DELETE_ID = Menu.FIRST + 1;
	private TaskDbAdapter mDbHelper;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
		/**
		 * Callback for when an item has been added.
		 */
		public void onItemAdded(long id);
		/**
		 * Callback for when an item has been deleted.
		 */
		public void onItemDeleted();
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}

		@Override
		public void onItemDeleted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onItemAdded(long id) {
			// TODO Auto-generated method stub
			
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mDbHelper = new TaskDbAdapter(getActivity());
        mDbHelper.open();
        fillData();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
		
		registerForContextMenu(getListView());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(String.valueOf(id));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	/**
	 * Menu Functionality to add a new task
	 */
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater ) {
	    inflater.inflate(R.menu.task_list_fragment, menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.add:
            mCallbacks.onItemAdded(createTask());
            return true;
        }
       
        return super.onOptionsItemSelected(item);
    }
	
    public void onCreateContextMenu(ContextMenu menu, View v,
          ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.add(0, DELETE_ID, 0, R.string.delete);
  }

    public boolean onContextItemSelected(MenuItem item) {
      switch(item.getItemId()) {
          case DELETE_ID:
              AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
              mDbHelper.deleteTask(info.id);
              mCallbacks.onItemDeleted();
              fillData();
      }
      return super.onContextItemSelected(item);
  }

	@SuppressLint("SimpleDateFormat")
	private long createTask() {
		long id;
		String taskName = "New Trip";
		Date date = new Date();  
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        id = mDbHelper.createTask(taskName, sdf.format(date) , "0" , "0" , "$0.00");
        fillData();
        return id;
	}
	
	@SuppressWarnings("deprecation")
	private void fillData() {
        // Get all of the notes from the database and create the item list
		// Future enhancement would use LoaderManager and CursorLoader
        Cursor c = mDbHelper.fetchAllTasks();
        getActivity().startManagingCursor(c);

        String[] from = new String[] { TaskDbAdapter.KEY_TITLE };
        int[] to = new int[] { android.R.id.text1 };
        
        // Now create an array adapter and set it to display using our row
		SimpleCursorAdapter tasks =
            new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, c, from, to);
        setListAdapter(tasks);      
    }
	

}
