package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sourceforge.zbar.Symbol;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.example.term.Contacts.AddContact;
import com.example.term.Contacts.GetContacts;
import com.example.term.SingleOwnEvent.DeleteTask;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SubscribedEvents extends ActionBarActivity {

	TextView textview;
	ListView listview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribed_events);
		textview=(TextView) this.findViewById(R.id.textView1);
		listview=(ListView) this.findViewById(R.id.listView1);
		
		if(isDeviceOnline()){
			new GetSubscribedEvents().execute("http://wq91120602.appspot.com/getsubscribedevents");
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.subscribed_events, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_subscribe) {
			launchQRScanner();
		}
		if(id==R.id.action_refresh){
			if(isDeviceOnline()){
				Global.OtherEvents=new ArrayList<Event>();
				new GetSubscribedEvents().execute("http://wq91120602.appspot.com/getsubscribedevents");
			}
			else{
				Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
			}
		}
		if (id == R.id.action_notification) {
			startActivity(new Intent(this,Notifications.class));
			return true;
		}
		
		if(id==R.id.action_contacts){
			startActivity(new Intent(SubscribedEvents.this,Contacts.class));
			return true;
		}
		if(id==R.id.action_logout){
			startActivity(new Intent(SubscribedEvents.this,MainActivity.class));
			return true;
		}
		if (id == R.id.action_me) {
			startActivity(new Intent(SubscribedEvents.this,MyCards.class));
			return true;
		}
		if(id==R.id.event_item1){
			startActivity(new Intent(SubscribedEvents.this,MyEvents.class));
			return true;
		}
		if(id==R.id.event_item3){
			startActivity(new Intent(SubscribedEvents.this,NearbyEvents.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode==RESULT_OK){
			final String scanresult=data.getStringExtra(ZBarConstants.SCAN_RESULT);
			try {
				JSONObject json=new JSONObject(scanresult);
				Event event=new Event(json);
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setMessage("Subscribe to "+json.getString("title")+"?");
				builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						if(isDeviceOnline()){
							new SubscribeEvent().execute("http://wq91120602.appspot.com/subscribetoevent",scanresult);
							//Toast.makeText(SubscribedEvents.this, scanresult,Toast.LENGTH_LONG).show();
						}
						else{
							Toast.makeText(SubscribedEvents.this, "No network connection available", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				
				builder.create().show();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				show("Scan Error");
				e.printStackTrace();
			}
		}
		else if(resultCode==RESULT_CANCELED && data!=null){
			String error=data.getStringExtra(ZBarConstants.ERROR_INFO);
			if(!TextUtils.isEmpty(error)){
				Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	class SubscribeEvent extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				JSONObject json=new JSONObject(params[1]);
				String author=json.getString("author");
				String createtime=json.getString("createtime");
				
				if(author.equals(Global.Username)){
					show("Cannot subscribe to your own event!");
					return null;
				}
				
				for(int i=0;i<Global.OtherEvents.size();i++){
					Event event=Global.OtherEvents.get(i);
					if(author.equals(event.author) && createtime.equals(event.createtime)){
						show("Already subscribed to this event!");
						return null;
					}
				}
				
				json.put("subscriber", Global.Username);
				
				HttpClient client=new DefaultHttpClient();
				URI website=new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				
				HttpResponse response=client.execute(request);
				
				if(response.getStatusLine().getStatusCode()==200){
					return params[1];
				}
				else{
					show("Server Error!");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				show("Input Error!");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				show("Network Failure!");
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				show("Input Error!");
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				show("Network Failure!");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				show("Input Error!");
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			if(result!=null){
				try {
					Global.OtherEvents.add(new Event(new JSONObject((String) result)));
					Global.OtherEvents.get(Global.OtherEvents.size()-1).subscribers++;
					Display();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					show("Input Error!");
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class GetSubscribedEvents extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			Object[] results =null;
			
			if(Global.OtherEvents.size()>0){
				return results;
				//Global.OtherEvents=new ArrayList<Event>();
			}
			
			try {
				JSONObject json=new JSONObject();
				json.put("author", Global.Username);
				
				HttpClient client =new DefaultHttpClient();
				URI website=new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				
				HttpResponse response=client.execute(request);
				
				if (response.getStatusLine().getStatusCode()==200){
					String tmp=EntityUtils.toString(response.getEntity());
					JSONArray jsonrec=new JSONObject(tmp).getJSONArray("events");
					for (int i=0;i<jsonrec.length();i++){
						JSONObject tmpjson=new JSONObject(jsonrec.getString(i));
						Global.OtherEvents.add(new Event(tmpjson));
					}
				}
				else{
					show("Server Error!");
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				show("Input Error!");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				show("Network Failure!");
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				show("Input Error!");
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				show("Network Failure!");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				show("Input Error!");
				e.printStackTrace();
			}
			return results;
		}
		
		@Override
		protected void onPostExecute(Object[] result){
			Display();
		}
	}
	
	public void Display(){
		ArrayList<String> temp=new ArrayList<String>();
		for(int i=0;i<Global.OtherEvents.size();i++){
			Event event=Global.OtherEvents.get(i);
			String name=event.title;
			if(name.length()==0) name="No title";
			temp.add(name);
		}
		String []names=temp.toArray(new String[temp.size()]);
		
		if(names.length==0){
			textview.setVisibility(View.VISIBLE);
			listview.setVisibility(View.GONE);
		}
		else{
			textview.setVisibility(View.GONE);
			listview.setAdapter(new ArrayAdapter<String>(SubscribedEvents.this,android.R.layout.simple_list_item_1,names));
			listview.setVisibility(View.VISIBLE);
			listview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent=new Intent(SubscribedEvents.this,SingleOtherEvent.class);
					intent.putExtra("OtherEvent", position);
					startActivity(intent);
				}
				
			});
		}
	}
	
	public void launchQRScanner(){
		if(isCameraAvailable()){
			Intent intent=new Intent(this,ZBarScannerActivity.class);
			intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
			this.startActivityForResult(intent, 0);
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
				Toast.makeText(SubscribedEvents.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	
	public boolean isCameraAvailable(){
		PackageManager pm=this.getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
	@Override
	public void onBackPressed(){
		startActivity(new Intent(this,Contacts.class));
	}
}
