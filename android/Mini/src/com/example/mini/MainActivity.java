package com.example.mini;



import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	

	static final String EXTRA_MESSAGE = "com.example.mini.mainactivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button bt=(Button) findViewById(R.id.button1);
		
		
		bt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SendMessage();
			}
			
		});
	}
	
	public void SendMessage(){
		Intent intent =new Intent(this,ShowStreams.class);
		EditText et=(EditText) findViewById(R.id.editText1);
		String message=et.getText().toString();
		EditText et1=(EditText) this.findViewById(R.id.editText2);
		String psd=et1.getText().toString();
		if(psd.length()==0){
			Toast.makeText(MainActivity.this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();;
			return;
		}
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);

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
}
