package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.Header;
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
import com.example.term.SingleContact.SelAdapter;
import com.google.common.base.Joiner;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class Notifications extends ActionBarActivity {

	TextView textview;
	ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
		
		textview=(TextView) this.findViewById(R.id.textView1);
		listview=(ListView) this.findViewById(R.id.listView1);
		
		if(isDeviceOnline()){
			new GetNotifications().execute("http://wq91120602.appspot.com/getnotification");
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notifications, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.action_contacts){
			startActivity(new Intent(this,Contacts.class));
			return true;
		}
		if(id==R.id.action_logout){
			startActivity(new Intent(this,MainActivity.class));
			return true;
		}
		if(id==R.id.event_item1){
			startActivity(new Intent(this,MyEvents.class));
			return true;
		}
		if(id==R.id.event_item2){
			startActivity(new Intent(this,SubscribedEvents.class));
			return true;
		}
		if(id==R.id.event_item3){
			startActivity(new Intent(this,NearbyEvents.class));
			return true;
		}
		if (id == R.id.action_me) {
			startActivity(new Intent(this,MyCards.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){
		startActivity(new Intent(Notifications.this,Contacts.class));
	}
	
	class GetNotifications extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			Object[] results =null;
			
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
						Global.Notifications=new ArrayList<Notification>();
					String tmp=EntityUtils.toString(response.getEntity());
					JSONArray jsonrec=new JSONObject(tmp).getJSONArray("notification");
					for (int i=0;i<jsonrec.length();i++){
						JSONObject tmpjson=new JSONObject(jsonrec.getString(i));
						Global.Notifications.add(new Notification(tmpjson));
					}
				}
				else{
					Log.e("wq","failure");
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
		for(int i=0;i<Global.Notifications.size();i++){
			int type =Global.Notifications.get(i).type;
			if(type==0){
				temp.add("Share Contact Request");
			}
			if(type==1){
				temp.add("Event Invitation");
			}
			if(type==2){
				temp.add("System");
			}
			
		}
		String []names=temp.toArray(new String[temp.size()]);
		
		if(names.length==0){
			textview.setVisibility(View.VISIBLE);
			listview.setVisibility(View.GONE);
		}
		else{
			textview.setVisibility(View.GONE);
			listview.setAdapter(new ArrayAdapter<String>(Notifications.this,android.R.layout.simple_list_item_1,names));
			listview.setVisibility(View.VISIBLE);
			listview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					AlertDialog.Builder builder=new AlertDialog.Builder(Notifications.this);
					
					Notification notification=Global.Notifications.get(position);
					final int type=notification.type;
					if(type==0){
						builder.setTitle("Share Contact Request");
					}
					if(type==1){
						builder.setTitle("Event Invitation");
					}
					if(type ==2){
						builder.setTitle("System Notification");
					}

					if(type==0 || type==1){
						try {
							final JSONObject json = new JSONObject(notification.message);
							String message=null;
							if(type==0){
								message=notification.sender+" wants to share "+json.getString("firstname")+" "+json.getString("lastname")+" with you.";
							}
							if(type==1){
								message=notification.sender+" wants to invite you to "+json.getString("title")+".";
							}
							builder.setMessage(message);
							final AsyncHttpClient client=new AsyncHttpClient();
							final RequestParams para=new RequestParams();
							para.put("type",type);
							para.put("author",json.get("author"));
							para.put("subscriber", Global.Username);
							para.put("createtime", json.get("createtime"));
							para.put("position", position);
							
							builder.setNegativeButton("Accept", new DialogInterface.OnClickListener(){
								@Override
								public void onClick(final DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									para.put("decision", 1);
									client.post("http://wq91120602.appspot.com/handlecontactnotification",para, new AsyncHttpResponseHandler(){
										@Override
										public void onFailure(int arg0, Header[] arg1,
												byte[] arg2, Throwable arg3) {
											// TODO Auto-generated method stub
											show("failure");
											dialog.cancel();
										}
										@Override
										public void onSuccess(int arg0, Header[] arg1,
												byte[] arg2) {
											// TODO Auto-generated method stub
											try {
												if(type==0){
													boolean flag=false;
													for (int i=0;i<Global.Contacts.size();i++){
														Card card=Global.Contacts.get(i);
														if(card.author.equals(json.get("author")) && card.createtime.equals(json.get("createtime"))){
															flag=true;
															break;
														}
													}
													if(json.get("author").equals(Global.Username)){
														flag=true;
													}
													if(!flag){
														Global.Contacts.add(new Card(json));
													}
												}
												if(type==1){
													boolean flag=false;
													for(int i=0;i<Global.OtherEvents.size();i++){
														Event event=Global.OtherEvents.get(i);
														if(event.author.equals(json.get("author")) && event.createtime.equals(json.get("createtime"))){
															flag=true;
															break;
														}
													}
													if(json.get("author").equals(Global.Username)){
														flag=true;
													}
													if(!flag){
														Global.OtherEvents.add(new Event(json));
														Global.OtherEvents.get(Global.OtherEvents.size()-1).subscribers++;
													}
												}
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											Global.Notifications.remove(position);
											show("success");
											dialog.cancel();
											Display();
										}
										
									});
								}
								
							});
							
							builder.setPositiveButton("Cancel",new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}});
							
							builder.setNeutralButton("Decline",new DialogInterface.OnClickListener(){

								@Override
								public void onClick(final DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									para.put("decision", 0);
									client.post("http://wq91120602.appspot.com/handlecontactnotification",para, new AsyncHttpResponseHandler(){

										@Override
										public void onFailure(int arg0, Header[] arg1,
												byte[] arg2, Throwable arg3) {
											// TODO Auto-generated method stub
											show("failure");
											dialog.cancel();
										}

										@Override
										public void onSuccess(int arg0, Header[] arg1,
												byte[] arg2) {
											// TODO Auto-generated method stub
											Global.Notifications.remove(position);
											show("success");
											dialog.cancel();
											Display();
										}
										
									});
								}
								
							});
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					if(type==2){
						builder.setMessage(notification.message);
						builder.setPositiveButton("Cancel",new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}});
						
						builder.setNegativeButton("OK", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(final DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								final AsyncHttpClient client=new AsyncHttpClient();
								final RequestParams para=new RequestParams();
								para.put("author", Global.Username);
								para.put("position", position);
								client.post("http://wq91120602.appspot.com/handlesystemnotification",para, new AsyncHttpResponseHandler(){

									@Override
									public void onFailure(int arg0,
											Header[] arg1, byte[] arg2,
											Throwable arg3) {
										// TODO Auto-generated method stub
										show("failure");
										dialog.cancel();
									}

									@Override
									public void onSuccess(int arg0,
											Header[] arg1, byte[] arg2) {
										// TODO Auto-generated method stub
										Global.Notifications.remove(position);
										show("success");
										dialog.cancel();
										Display();
									}
									
								});
							}
							
						});
					}
					builder.create().show();
					
				}
				
			});
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
				Toast.makeText(Notifications.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
}
