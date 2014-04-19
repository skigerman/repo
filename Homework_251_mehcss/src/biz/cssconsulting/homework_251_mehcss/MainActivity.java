package biz.cssconsulting.homework_251_mehcss;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		android.app.ActionBar actionBar = getActionBar();
		actionBar.hide();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	/** Called when the user clicks the Sign In button */
	public void signIn(View view) {
		// Do something in response to button
		EditText t1 = (EditText)findViewById(R.id.editText1);
		String v1 = t1.getText().toString();
		EditText t2 = (EditText)findViewById(R.id.editText2);
		String v2 = t2.getText().toString();

		if(v1.equals("")||v2.equals("")){
			// e-mail or password is blank
			Toast toast = Toast.makeText(getApplicationContext(), "E-mail and Password cannot be blank", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 150);
			toast.show();
		}
		else{
			if ( ! Patterns.EMAIL_ADDRESS.matcher(v1).matches()){
				// e-mail is invalid
				Toast toast = Toast.makeText(getApplicationContext(), "E-mail is invalid", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP, 0, 150);
				toast.show();
			}
			else{
				Intent intent = new Intent(this, DisplayMessageActivity.class);
				intent.putExtra(getString(R.string.email), v1);
				intent.putExtra(getString(R.string.password), v2);
				startActivity(intent);	
			}

		}
	}
}

