package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

import net.sourceforge.zbar.Symbol;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.example.term.SingleCard.DeleteTask;
import com.google.common.base.Joiner;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Contacts extends ActionBarActivity {

	TextView textview;
	ListView listview;
	AutoCompleteTextView auto;
	ContactAdapter adapter=null;
	Boolean mode=null;
	int[] searchpositions=null;
	String[] names=null;
	Menu optmenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		textview =(TextView) this.findViewById(R.id.textView1);
		listview =(ListView) this.findViewById(R.id.listView1);
		auto=(AutoCompleteTextView) this.findViewById(R.id.autoCompleteTextView1);
		mode=false;
		
		if(isDeviceOnline()){
			new GetContacts().execute("http://wq91120602.appspot.com/getcontacts");
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts, menu);
		this.optmenu=menu;
		
		optmenu.findItem(R.id.seldate).setVisible(true);
		optmenu.findItem(R.id.action_edit).setVisible(true);
		optmenu.findItem(R.id.action_delete_contact).setVisible(false);
		optmenu.findItem(R.id.action_cancel_contact_delete).setVisible(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_me) {
			startActivity(new Intent(Contacts.this,MyCards.class));
			return true;
		}
		if (id == R.id.action_notification) {
			startActivity(new Intent(Contacts.this,Notifications.class));
			return true;
		}
		if(id==R.id.action_logout){
			startActivity(new Intent(Contacts.this,MainActivity.class));
			return true;
		}
		if(id==R.id.event_item1){
			startActivity(new Intent(Contacts.this,MyEvents.class));
			return true;
		}
		if(id==R.id.event_item2){
			startActivity(new Intent(Contacts.this,SubscribedEvents.class));
			return true;
		}
		if(id==R.id.event_item3){
			startActivity(new Intent(Contacts.this,NearbyEvents.class));
			return true;
		}
		
		if(id==R.id.seldate){
			mode=false;
			launchQRScanner();
		}
		if(id==R.id.action_edit){
			mode=true;
			DisplayContacts();
		}
		if(id==R.id.action_cancel_contact_delete){
			mode=false;
			DisplayContacts();
		}
		if(id==R.id.action_delete_contact){
			mode=false;
			DeleteContacts();
		}
		if(id==R.id.action_refresh){
			mode=false;
			if(isDeviceOnline()){
				Global.Contacts=new ArrayList<Card>();
				new GetContacts().execute("http://wq91120602.appspot.com/getcontacts");
			}
			else{
				Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){}
	
	public void launchQRScanner(){
		if(isCameraAvailable()){
			Intent intent=new Intent(this,ZBarScannerActivity.class);
			intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
			this.startActivityForResult(intent, 0);
		}
	}
	
	public void DeleteContacts(){
		StringBuffer str=new StringBuffer();
		str.append("Delete the following contact(s)?");
		
		ArrayList<String> contacts=new ArrayList<String>();
		ArrayList<String> times=new ArrayList<String>();
		
		for(int i=0;i<adapter.flags.length;i++){
			if(adapter.flags[i]){
				str.append("\n"+adapter.names[i]);
				contacts.add(Global.Contacts.get(i).author);
				times.add(Global.Contacts.get(i).createtime);
			}
		}
		
		//Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
		if(contacts.size()==0){
			return;
		}
		final String contactinfo=Joiner.on("**").join(contacts.toArray(new String[contacts.size()]));
		final String timeinfo=Joiner.on("**").join(times.toArray(new String[times.size()]));
		
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage(str);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if(isDeviceOnline()){
					new DeleteTask().execute("http://wq91120602.appspot.com/deletecontacts",contactinfo,timeinfo);
				}
				else{
					Toast.makeText(Contacts.this, "No network connection available", Toast.LENGTH_SHORT).show();
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode==RESULT_OK){
			final String scanresult=data.getStringExtra(ZBarConstants.SCAN_RESULT);
			try {
				JSONObject json=new JSONObject(scanresult);
				// create a Card instance to check whether the scan result is correct
				Card card=new Card(json);
				if(isDeviceOnline()){
					new AddContact().execute("http://wq91120602.appspot.com/addcontact",data.getStringExtra(ZBarConstants.SCAN_RESULT));
					//Toast.makeText(this, data.getStringExtra(ZBarConstants.SCAN_RESULT),Toast.LENGTH_LONG).show();
				}
				else{
					Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				show("Scan Error!");
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

	class DeleteTask extends AsyncTask<Object, Object, Object>{

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			try {
				JSONObject json=new JSONObject();
				json.put("author", Global.Username);
				json.put("contact", params[1]);
				json.put("createtime", params[2]);
				
				HttpClient client=new DefaultHttpClient();
				URI website=new URI((String) params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				
				HttpResponse response=client.execute(request);
				
				if(response.getStatusLine().getStatusCode()==200){
					//remove operation must be from large to small indices, otherwise there will be problem
					for (int i=adapter.flags.length-1;i>=0;i--){
						if(adapter.flags[i]){
							Global.Contacts.remove(i);
						}
					}
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
		
		protected void onPostExecute(Object result){
			DisplayContacts();
		}
		
	}
	
	class AddContact extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				JSONObject json=new JSONObject(params[1]);
				String author=json.getString("author");
				String createtime=json.getString("createtime");
				
				if(author.equals(Global.Username)){
					show("Cannot add yourself as a contact!");
					return null;
				}
				
				for(int i=0;i<Global.Contacts.size();i++){
					Card card=Global.Contacts.get(i);
					if(author.equals(card.author) && createtime.equals(card.createtime)){
						show("Contact already exists!");
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
					Global.Contacts.add(new Card(new JSONObject((String) result)));
					DisplayContacts();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					show("Input Error!");
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class GetContacts extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			if(Global.Contacts.size()>0){
				return null;
			}
			
			try {
				JSONObject json=new JSONObject();
				json.put("author", Global.Username);
				
				HttpClient client=new DefaultHttpClient();
				URI website=new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				
				HttpResponse response=client.execute(request);
				
				if(response.getStatusLine().getStatusCode()==200){
					String tmp=EntityUtils.toString(response.getEntity());
					JSONArray jsonrec = new JSONObject(tmp).getJSONArray("cards");
					for(int i=0;i<jsonrec.length();i++){
						Global.Contacts.add(new Card(new JSONObject(jsonrec.getString(i))));
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
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			DisplayContacts();
		}
	}
	
	public void DisplayContacts(){
		if(optmenu!=null){
			MenuItem item1=optmenu.findItem(R.id.seldate);
			MenuItem item2=optmenu.findItem(R.id.action_edit);
			MenuItem item3=optmenu.findItem(R.id.action_delete_contact);
			MenuItem item4=optmenu.findItem(R.id.action_cancel_contact_delete);
			MenuItem item5=optmenu.findItem(R.id.action_refresh);
			auto.setText("");
			
			if(!mode){
				item1.setVisible(true);
				item2.setVisible(true);
				item3.setVisible(false);
				item4.setVisible(false);
				item5.setVisible(true);
			}
			else{
				item1.setVisible(false);
				item2.setVisible(false);
				item3.setVisible(true);
				item4.setVisible(true);
				item5.setVisible(false);
			}
		}
		
		
		ArrayList<String> temp=new ArrayList<String>();
		for(int i=0;i<Global.Contacts.size();i++){
			Card card=Global.Contacts.get(i);
			temp.add(Joiner.on(" ").join(new String[]{card.firstname,card.lastname}));
		}
		names=temp.toArray(new String[temp.size()]);
		if(names.length==0){
			textview.setVisibility(View.VISIBLE);
			listview.setVisibility(View.GONE);
		}
		else{
			adapter=new ContactAdapter(Contacts.this,android.R.layout.simple_list_item_1,names);
			listview.setAdapter(adapter);
			
			ArrayAdapter<String> ad=new ArrayAdapter<String> (Contacts.this,R.layout.support_simple_spinner_dropdown_item,names);
			auto.setAdapter(ad);
 			
			listview.setVisibility(View.VISIBLE);
			listview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Intent intent =new Intent(Contacts.this,SingleContact.class);
					intent.putExtra("Contact", position);
					startActivity(intent);
				}
				
			});
			textview.setVisibility(View.GONE);
		}
	}
	
	class ContactAdapter extends ArrayAdapter<String>{

		String [] names=null;
		boolean []flags=null;
		
		public ContactAdapter(Context context, int resource, String[] objects) {
			super(context, resource, objects);
			this.names=objects;
			this.flags=new boolean[names.length];
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			LayoutInflater inflater =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view=inflater.inflate(R.layout.display_contacts,parent,false);
			TextView textview=(TextView) view.findViewById(R.id.textView1);
			CheckBox check=(CheckBox) view.findViewById(R.id.checkBox1);
			textview.setText(names[position]);
			
			if(!mode){
				check.setVisibility(View.GONE);
			}
			else{
				check.setVisibility(View.VISIBLE);
				check.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						CheckBox ck=(CheckBox) v;
						flags[position]=ck.isChecked();
						//Toast.makeText(getApplicationContext(), names[position]+"is"+ck.isChecked(), Toast.LENGTH_SHORT).show();
					}
				});
			}
			return view;
		}
		
	}
	
	public void Search(View view){
		Button bt=(Button) view;
		if(bt.getText().equals("Search")){
			bt.setText("Cancel");
			String se=auto.getText().toString();
			ArrayList<String> searchnames=new ArrayList<String>();
			for(int i=0;i<names.length;i++){
				if(names[i].contains(se)){
					searchnames.add(names[i]);
				}
			}
			final String[] temp=searchnames.toArray(new String[searchnames.size()]);
			listview.setAdapter(new ArrayAdapter<String>(Contacts.this,android.R.layout.simple_list_item_1,temp));
			listview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					//Log.e("wq",Integer.toString(position));
					String[] obj=Arrays.copyOfRange(temp, 0, position+1);
					String name=temp[position];
					int des=Collections.frequency(Arrays.asList(obj), name); //find frequency of the selected item
					//Log.e("wq",Arrays.asList(obj).toString());
					for(int i=0;i<names.length;i++){  //find its position in the original list
						if(name.equals(names[i])){
							des--;
							if(des==0){
								Intent intent =new Intent(Contacts.this,SingleContact.class);
								intent.putExtra("Contact", i);
								startActivity(intent);
							}
						}
					}
				}
				
			});
		}
		else{
			bt.setText("Search");
			DisplayContacts();
		}
	}
	
	public boolean isCameraAvailable(){
		PackageManager pm=this.getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
	
	public void show(final String str){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(Contacts.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
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
