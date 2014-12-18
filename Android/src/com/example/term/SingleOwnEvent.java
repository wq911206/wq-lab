package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.term.CreateNewEvent.SendData;
import com.example.term.SingleCard.DeleteTask;
import com.example.term.SingleContact.SelAdapter;
import com.google.common.base.Joiner;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.format.Time;
import android.util.Log;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SingleOwnEvent extends ActionBarActivity {

	Calendar calendar;
	int year;
	int month;
	int day;
	int hour;
	int minute;
	
	Menu optmenu;
	int position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_own_event);
		position=this.getIntent().getIntExtra("OwnEvent", 0);
		LoadEvent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.optmenu=menu;
		getMenuInflater().inflate(R.menu.single_own_event, menu);
		EditMode(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_edit) {
			EditMode(true);
			return true;
		}
		if(id==R.id.action_share){
			ShareEvent();
			return true;
		}
		if(id==R.id.action_save){
			EditMode(false);
			SaveEvent();
			return true;
		}
		if(id==R.id.action_cancel){
			LoadEvent();
			EditMode(false);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void SaveEvent(){
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
			json.put("createtime", Global.MyEvents.get(position).createtime);
			json.put("subscribers", Global.MyEvents.get(position).subscribers);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Input Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
		
		if(isDeviceOnline()){
			new SaveTask().execute("http://wq91120602.appspot.com/modifyevent",json.toString());
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
	}
	
	class SaveTask extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				HttpClient client=new DefaultHttpClient();
				URI website =new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				StringEntity data=new StringEntity(params[1]);
				request.setEntity(data);
				HttpResponse response=client.execute(request);
				if(response.getStatusLine().getStatusCode()==200){
					return new JSONObject(params[1]);
				}
				else{
					show("Server Error!");
				}
				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			if(result!=null){
				try {
					Global.MyEvents.set(position, new Event((JSONObject) result));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					show("Data format error!");
					e.printStackTrace();
				}
				LoadEvent();
			}
		}
		
	}
	
	public void deleteEvent(View view){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("Delete this event?");
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if(isDeviceOnline()){
					new DeleteTask().execute("http://wq91120602.appspot.com/deleteevent");
				}
				else{
					Toast.makeText(SingleOwnEvent.this, "No network connection available", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		
		builder.create().show();
	}
	
	class DeleteTask extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			try {
				HttpClient client=new DefaultHttpClient();
				URI website = new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				JSONObject json=new JSONObject();
				Event event=Global.MyEvents.get(position);
				json.put("author", event.author);
				json.put("createtime",event.createtime);
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				HttpResponse response=client.execute(request);
				if(response.getStatusLine().getStatusCode()==200){
					return 200;
				}
				else{
					show("Server Error!");
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleOwnEvent.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			if(result!=null){
				Global.MyEvents.remove(position);
				startActivity(new Intent(SingleOwnEvent.this,MyEvents.class));
			}
			
		}
		
	}
	
	public void LoadEvent(){
		Event event=Global.MyEvents.get(position);
		EditText edit=(EditText) this.findViewById(R.id.editText1);
		edit.setText(event.title);
		TextView textview=(TextView) this.findViewById(R.id.textView3);
		textview.setText(event.date);
		edit=(EditText) this.findViewById(R.id.editText2);
		edit.setText(event.location);
		edit=(EditText) this.findViewById(R.id.editText3);
		edit.setText(event.organizer);
		edit=(EditText) this.findViewById(R.id.editText4);
		edit.setText(event.description);
		textview=(TextView) this.findViewById(R.id.textView7);
		textview.setText("Subscribers: "+Integer.toString(event.subscribers));
		ImageView img=(ImageView) this.findViewById(R.id.imageView1);
		int qrCodeDimension=500;
		
		try {
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(event.Convert(),     
					null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
					qrCodeDimension);
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			img.setImageBitmap(bitmap);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Event Format Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (JSONException e) {
			Toast.makeText(this, "Event Format Error!", Toast.LENGTH_SHORT).show();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void ShareEvent(){
		ArrayList<String> temp=new ArrayList<String>();
		for(int i=0;i<Global.Contacts.size();i++){
			Card card=Global.Contacts.get(i);
			temp.add(Joiner.on(" ").join(new String[]{card.firstname,card.lastname}));
		}
		final String []names=temp.toArray(new String[temp.size()]);
		final boolean flags[]=new boolean[names.length];
		if(names.length>0){
			AlertDialog.Builder builder=new AlertDialog.Builder(SingleOwnEvent.this);
			builder.setTitle("Select Contacts");
			builder.setMultiChoiceItems(names, flags, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					// TODO Auto-generated method stub
					flags[which]=isChecked;
				}
				
			});
			builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.e("wq",Integer.toString(position));
					StringBuffer str=new StringBuffer();
					str.append("The following contacts are selected:");
					ArrayList<String> destinations=new ArrayList<String>();
					for(int i=names.length-1;i>=0;i--)
					{
						if(flags[i])
						{
							destinations.add(Global.Contacts.get(i).author);
						}
					}
					AsyncHttpClient client=new AsyncHttpClient();
					RequestParams para=new RequestParams();
					para.put("destination", Joiner.on("**").join(destinations.toArray(new String[destinations.size()])));
					para.put("sender", Global.Username);
					Event event=Global.MyEvents.get(position);
					para.put("author", event.author);
					para.put("createtime", event.createtime);
					
					client.post("http://wq91120602.appspot.com/shareeventnotification",para, new AsyncHttpResponseHandler(){

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							// TODO Auto-generated method stub
							show("failure");
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1,
								byte[] arg2) {
							// TODO Auto-generated method stub
							show("success");
						}
						
					});
				}
			});
			
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
		}
		else{
			show("No Contacts Available");
		}
	}
	
	
	//true for edit mode, false for nonedit mode
	public void EditMode(boolean flag){
		if(optmenu!=null){
			MenuItem item=optmenu.findItem(R.id.action_save);
			item.setVisible(flag);
			item=optmenu.findItem(R.id.action_edit);
			item.setVisible(!flag);
			item=optmenu.findItem(R.id.action_share);
			item.setVisible(!flag);
			item=optmenu.findItem(R.id.action_cancel);
			item.setVisible(flag);
		}
		if(!flag){
			EditText edit=(EditText) this.findViewById(R.id.editText1);
			edit.setInputType(InputType.TYPE_NULL);
			edit=(EditText) this.findViewById(R.id.editText2);
			edit.setInputType(InputType.TYPE_NULL);
			edit=(EditText) this.findViewById(R.id.editText3);
			edit.setInputType(InputType.TYPE_NULL);
			edit=(EditText) this.findViewById(R.id.editText4);
			edit.setInputType(InputType.TYPE_NULL);
			edit.setSingleLine(false);
			Button button=(Button) this.findViewById(R.id.button1);
			button.setVisibility(View.GONE);
			button=(Button) this.findViewById(R.id.button2);
			button.setVisibility(View.GONE);
			ImageView image=(ImageView) this.findViewById(R.id.imageView1);
			image.setVisibility(View.VISIBLE);
			TextView text=(TextView) this.findViewById(R.id.textView7);
			text.setVisibility(View.VISIBLE);
		}
		else{
			EditText edit=(EditText) this.findViewById(R.id.editText1);
			edit.setInputType(InputType.TYPE_CLASS_TEXT);
			edit=(EditText) this.findViewById(R.id.editText2);
			edit.setInputType(InputType.TYPE_CLASS_TEXT);
			edit=(EditText) this.findViewById(R.id.editText3);
			edit.setInputType(InputType.TYPE_CLASS_TEXT);
			edit=(EditText) this.findViewById(R.id.editText4);
			edit.setInputType(InputType.TYPE_CLASS_TEXT);
			edit.setSingleLine(false);
			Button button=(Button) this.findViewById(R.id.button1);
			button.setVisibility(View.VISIBLE);
			button=(Button) this.findViewById(R.id.button2);
			button.setVisibility(View.VISIBLE);
			ImageView image=(ImageView) this.findViewById(R.id.imageView1);
			image.setVisibility(View.GONE);
			TextView text=(TextView) this.findViewById(R.id.textView7);
			text.setVisibility(View.GONE);
		}
	}
	
	public void addDate(View view){
		calendar=Calendar.getInstance();
		new DatePickerDialog(SingleOwnEvent.this,new ListenerDate(),calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
	}
	
	private class ListenerDate implements DatePickerDialog.OnDateSetListener{

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			// TODO Auto-generated method stub
			SingleOwnEvent.this.year=year;
			SingleOwnEvent.this.month=monthOfYear;
			SingleOwnEvent.this.day=dayOfMonth;
			new TimePickerDialog(SingleOwnEvent.this, new ListenerTime(), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
		}
		
	}
	
	private class ListenerTime implements TimePickerDialog.OnTimeSetListener{

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			SingleOwnEvent.this.hour=hourOfDay;
			SingleOwnEvent.this.minute=minute;
			
			TextView textview=(TextView) SingleOwnEvent.this.findViewById(R.id.textView3);
			Time time=new Time();
			time.set(0,SingleOwnEvent.this.minute,SingleOwnEvent.this.hour,SingleOwnEvent.this.day,SingleOwnEvent.this.month,SingleOwnEvent.this.year);
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
	
	public void show(final String str){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(SingleOwnEvent.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
}
