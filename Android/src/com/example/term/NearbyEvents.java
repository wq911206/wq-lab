package com.example.term;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

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

import com.example.term.SubscribedEvents.SubscribeEvent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NearbyEvents extends ActionBarActivity {

	MapView mapview;
	GoogleMap map;
	
	double latitude=0;
	double longitude=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby_events);
		
		mapview=(MapView) this.findViewById(R.id.mapview);
        mapview.onCreate(savedInstanceState);
        
        map=mapview.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setCompassEnabled(false);
		map.getUiSettings().setRotateGesturesEnabled(false);
		
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			@Override
			public View getInfoWindow(Marker arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public View getInfoContents(Marker arg0) {
				// TODO Auto-generated method stub
				View view = getLayoutInflater().inflate(R.layout.infowindow, null);
				try {
					final String str=arg0.getTitle();
					final JSONObject json=new JSONObject(str);
					Event event = new Event(json);
					EditText edit=(EditText) view.findViewById(R.id.editText1);
					edit.setText(event.title);
					edit.setInputType(InputType.TYPE_NULL);
					TextView textview=(TextView) view.findViewById(R.id.textView3);
					textview.setText(event.date);
					edit=(EditText) view.findViewById(R.id.editText2);
					edit.setText(event.location);
					edit.setInputType(InputType.TYPE_NULL);
					edit=(EditText) view.findViewById(R.id.editText3);
					edit.setText(event.organizer);
					edit.setInputType(InputType.TYPE_NULL);
					edit=(EditText) view.findViewById(R.id.editText4);
					edit.setText(event.description);
					edit.setInputType(InputType.TYPE_NULL);
					textview=(TextView) view.findViewById(R.id.textView7);
					textview.setText("Subscribers: "+Integer.toString(event.subscribers));
					
					map.setOnInfoWindowClickListener(new OnInfoWindowClickListener(){
						@Override
						public void onInfoWindowClick(Marker arg0) {
							// TODO Auto-generated method stub
							AlertDialog.Builder builder=new AlertDialog.Builder(NearbyEvents.this);
							try {
								builder.setMessage("Subscribe to "+json.getString("title")+"?");
								builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										
										if(isDeviceOnline()){
											new SubscribeEvent().execute("http://wq91120602.appspot.com/subscribetoevent",str);
											//Toast.makeText(SubscribedEvents.this, scanresult,Toast.LENGTH_LONG).show();
										}
										else{
											show("No network connection available");
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
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							builder.create().show();
						}
					});
					
					return view;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
		});
		
		
		
		LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener listener=new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				latitude=location.getLatitude();
				longitude=location.getLongitude();
				LatLng latlng=new LatLng(latitude,longitude);
				//CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 10);
				//map.animateCamera(cameraUpdate);
				//map.addMarker(new MarkerOptions().position(latlng).title("wq"));
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
		
		//map.addMarker(new MarkerOptions().position(new LatLng(-31.90, 115.86)).title("wq"));
		map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener(){

			@Override
			public boolean onMyLocationButtonClick() {
				// TODO Auto-generated method stub
				map.clear();
				final AsyncHttpClient client=new AsyncHttpClient();
				final RequestParams para=new RequestParams();
				para.put("latitude", latitude);
				para.put("longitude", longitude);
				client.post("http://wq91120602.appspot.com/nearbyevents",para, new AsyncHttpResponseHandler(){

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						show("failure");
					}

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						// TODO Auto-generated method stub
						show("success");
						Log.e("wq",new String(arg2));
						String tmp=new String(arg2);
						try {
							JSONArray jsonrec=new JSONObject(tmp).getJSONArray("events");
							for(int i=0;i<jsonrec.length();i++){
								String str=jsonrec.getString(i);
								JSONObject tmpjson=new JSONObject(str);
								map.addMarker(new MarkerOptions().position(new LatLng(tmpjson.getDouble("latitude"),tmpjson.getDouble("longitude"))).title(str));
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
				
				return false;
			}
			
		});
	}
	
	@Override
	public void onResume() {
		mapview.onResume();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.nearby_events, menu);
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
	
	@Override
	public void onBackPressed(){
		startActivity(new Intent(this,Contacts.class));
	}
	
	public void show(final String str){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(NearbyEvents.this, str, Toast.LENGTH_SHORT).show();
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
	
	class GetNearbyEvents extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			int responsecode=0;
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
					return EntityUtils.toString(response.getEntity());
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(NearbyEvents.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(NearbyEvents.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(NearbyEvents.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(NearbyEvents.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			if(result!=null){
				String tmp=(String) result;
				try {
					JSONArray jsonrec= new JSONObject(tmp).getJSONArray("events");
					for (int i=0;i<jsonrec.length();i++){
						JSONObject tmpjson=new JSONObject(jsonrec.getString(i));
						map.addMarker(new MarkerOptions().position(new LatLng(tmpjson.getDouble("latitude"),tmpjson.getDouble("longitude"))).title(tmpjson.getString("title")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					show("Error!");
					e.printStackTrace();
				}
				
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
					show("success");
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
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					show("Input Error!");
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
