package biz.cssconsulting.TripMileage;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import biz.cssconsulting.TripMileage.data.TaskDbAdapter;
import biz.cssconsulting.TripMileage.R;

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
	String mDate;
	String mStartMileage;
	String mEndMileage;
	String mReimbursement;
	EditText detailsText;
	EditText detailsText2, detailsText3;
	TextView detailsText1, detailsText4;
	
	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;
	
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {

		/**
		 * Callback for when an item has been updated.
		 */
		public void onItemUpdated(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		
		@Override
		public void onItemUpdated(String id) {
			// TODO Auto-generated method stub
			
		}

	};


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new TaskDbAdapter(getActivity());
        mDbHelper.open();

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the task content specified by the fragment
			// arguments. 
			Cursor c = mDbHelper.fetchTask(Long.valueOf(getArguments().getString(
					ARG_ITEM_ID)));
			if (c.moveToFirst()) {
			    mItem = c.getString(c.getColumnIndex("title")); //could use body logic here
			    mDate = c.getString(c.getColumnIndex("date"));
			    mStartMileage = c.getString(c.getColumnIndex("startMileage"));
			    mEndMileage = c.getString(c.getColumnIndex("endMileage"));
			    mReimbursement = c.getString(c.getColumnIndex("reimbursement"));
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
			detailsText = (EditText) rootView.findViewById(R.id.detailstext);
			detailsText.setText(mItem);
		}
		if (mDate != null) {
			detailsText1 = (TextView) rootView.findViewById(R.id.detailstext1);
			detailsText1.setText(mDate);
		}
		if (mStartMileage != null) {
			detailsText2 = (EditText) rootView.findViewById(R.id.detailstext2);
			detailsText2.setText(mStartMileage);
		}
		if (mEndMileage != null) {
			detailsText3 = (EditText) rootView.findViewById(R.id.detailstext3);
			detailsText3.setText(mEndMileage);
		}
		if (mReimbursement != null) {
			detailsText4 = (TextView) rootView.findViewById(R.id.detailstext4);
			detailsText4.setText(mReimbursement);
		}
		
		Button mButton = (Button) rootView.findViewById(R.id.save);
		mButton.setOnClickListener(new OnClickListener() {
			/**
			 * Callback method for click event on Button
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			    @Override
			    public void onClick(View view) {
			    	mItem = detailsText.getText().toString();
			    	mStartMileage = detailsText2.getText().toString();
			    	mEndMileage = detailsText3.getText().toString();
			    	if ( ! mEndMileage.equals("0"))
	                {
	                	mReimbursement = ("$" + ( new BigDecimal(mEndMileage).subtract(new BigDecimal (mStartMileage)))
			    		.multiply(new BigDecimal(getRate())).toString());
	                	detailsText4.setText(mReimbursement);
	                }
			        mDbHelper.updateTask(Long.valueOf(getArguments().getString(
					ARG_ITEM_ID)), mItem, mDate, mStartMileage, mEndMileage, mReimbursement);
			        
			     // show a toast confirmation
					Toast.makeText(getActivity(),
							R.string.trip_updated,
							Toast.LENGTH_SHORT).show();
					
					mCallbacks.onItemUpdated(getArguments().getString(ARG_ITEM_ID));		
			    }
			});

		return rootView;
		
	}
	
	public String getRate() {
		SharedPreferences settings = 
				PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		String rate = settings.getString("rate", "0");
		return rate;
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

	
	
}
