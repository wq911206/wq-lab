package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

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

import com.example.term.MyCards.GetOwnCards;
import com.google.common.base.Joiner;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MyEvents extends ActionBarActivity {

	TextView textview;
	ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);
		
		textview=(TextView) this.findViewById(R.id.textView1);
		listview=(ListView) this.findViewById(R.id.listView1);
		
		if(isDeviceOnline()){
			new GetOwnEvents().execute("http://wq91120602.appspot.com/getownevents");
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_events, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.action_createnewevent){
			startActivity(new Intent(MyEvents.this,CreateNewEvent.class));
			return true;
		}
		if(id==R.id.action_refresh){
			if(isDeviceOnline()){
				Global.MyEvents=new ArrayList<Event>();
				new GetOwnEvents().execute("http://wq91120602.appspot.com/getownevents");
			}
			else{
				Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
			}
		}
		
		if(id==R.id.action_contacts){
			startActivity(new Intent(MyEvents.this,Contacts.class));
			return true;
		}
		if(id==R.id.action_logout){
			startActivity(new Intent(MyEvents.this,MainActivity.class));
			return true;
		}
		if (id == R.id.action_me) {
			startActivity(new Intent(MyEvents.this,MyCards.class));
			return true;
		}
		if(id==R.id.event_item2){
			startActivity(new Intent(MyEvents.this,SubscribedEvents.class));
			return true;
		}
		if(id==R.id.event_item3){
			startActivity(new Intent(MyEvents.this,NearbyEvents.class));
			return true;
		}
		if (id == R.id.action_notification) {
			startActivity(new Intent(this,Notifications.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){
		startActivity(new Intent(MyEvents.this,Contacts.class));
	}
	
	class GetOwnEvents extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			Object[] results =null;
			
			if(Global.MyEvents.size()>0){
				return results;
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
						Global.MyEvents.add(new Event(tmpjson));
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
			ArrayList<String> temp=new ArrayList<String>();
			for(int i=0;i<Global.MyEvents.size();i++){
				Event event=Global.MyEvents.get(i);
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
				listview.setAdapter(new ArrayAdapter<String>(MyEvents.this,android.R.layout.simple_list_item_1,names));
				listview.setVisibility(View.VISIBLE);
				listview.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent intent=new Intent(MyEvents.this,SingleOwnEvent.class);
						intent.putExtra("OwnEvent", position);
						startActivity(intent);
					}
					
				});
			}
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
				Toast.makeText(MyEvents.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
}
