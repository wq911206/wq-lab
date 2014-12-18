package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.term.CreateNewCard.SendData;
import com.google.android.gms.maps.model.LatLng;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class CreateNewEvent extends ActionBarActivity {

	Calendar calendar;
	int year;
	int month;
	int day;
	int hour;
	int minute;
	
	double latitude=0;
	double longitude=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_event);
		
		LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener listener=new LocationListener(){
			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				latitude=location.getLatitude();
				longitude=location.getLongitude();
			}
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				
			}
			@Override
			public void onProviderEnabled(String provider) {
				
			}
			@Override
			public void onProviderDisabled(String provider) {
				
			}
		};
		
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, (float) 5, listener);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_new_event, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_cancel) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){
		NavUtils.navigateUpFromSameTask(this);
	}
	
	public void CreateEvent(View view){
		String title=((EditText) this.findViewById(R.id.editText1)).getText().toString();
		String date=((TextView) this.findViewById(R.id.textView3)).getText().toString();
		String location=((EditText) this.findViewById(R.id.editText2)).getText().toString();
		String organizer=((EditText) this.findViewById(R.id.editText3)).getText().toString();
		String description=((EditText) this.findViewById(R.id.editText4)).getText().toString();
		
		Log.e("wq",date);
		if(!date.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}")) {
			Toast.makeText(this, "No date selected", Toast.LENGTH_SHORT).show();
			return;
		}
			
		
		JSONObject json=new JSONObject();
		try {
			json.put("author", Global.Username);
			json.put("title", title);
			json.put("date", date);
			json.put("location", location);
			json.put("organizer", organizer);
			json.put("description", description);
			
			
			json.put("latitude", latitude);
			json.put("longitude", longitude);
		
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Input Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
		
		if(isDeviceOnline()){
			new SendData().execute("http://wq91120602.appspot.com/createevent",json.toString());
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	class SendData extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			int responsecode=0;
			String str=null;
			try {
				HttpClient client=new DefaultHttpClient();
				URI website =new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				StringEntity data=new StringEntity(params[1]);
				request.setEntity(data);
				HttpResponse response=client.execute(request);
				
				//get response and get the create in order to create a new Card instance
				responsecode=response.getStatusLine().getStatusCode();
				if(responsecode==200){
					JSONObject js=new JSONObject(params[1]);
					JSONObject temp=new JSONObject(EntityUtils.toString(response.getEntity()));
					js.put("createtime", temp.get("createtime"));
					js.put("subscribers", temp.getInt("subscribers"));
					str=js.toString();
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewEvent.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewEvent.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			
			Object[] result=new Object[2];
			result[0]=str;
			//result[1]=str;
			return result;
		}
		
		@Override
		protected void onPostExecute(Object[] result){
			String message=(String) result[0];
			if(message!=null){
				try {
					Log.e("wq",message);
					JSONObject json=new JSONObject(message);
					Global.MyEvents.add(new Event(json));
					Toast.makeText(CreateNewEvent.this, "New Event Created Successfully!", Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				Toast.makeText(CreateNewEvent.this, "Failure!", Toast.LENGTH_SHORT).show();
			}
			NavUtils.navigateUpFromSameTask(CreateNewEvent.this);
		}
		
	}
	
	
	public void addDate(View view){
		calendar=Calendar.getInstance();
		new DatePickerDialog(CreateNewEvent.this,new ListenerDate(),calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
	}
	
	private class ListenerDate implements DatePickerDialog.OnDateSetListener{

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			// TODO Auto-generated method stub
			CreateNewEvent.this.year=year;
			CreateNewEvent.this.month=monthOfYear;
			CreateNewEvent.this.day=dayOfMonth;
			new TimePickerDialog(CreateNewEvent.this, new ListenerTime(), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
		}
		
	}
	
	private class ListenerTime implements TimePickerDialog.OnTimeSetListener{

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			CreateNewEvent.this.hour=hourOfDay;
			CreateNewEvent.this.minute=minute;
			
			TextView textview=(TextView) CreateNewEvent.this.findViewById(R.id.textView3);
			Time time=new Time();
			time.set(0,CreateNewEvent.this.minute,CreateNewEvent.this.hour,CreateNewEvent.this.day,CreateNewEvent.this.month,CreateNewEvent.this.year);
			textview.setText(time.format("%Y-%m-%d %H:%M"));
		}
		
	}
	
	private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
