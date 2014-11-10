package com.example.mini;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewNearby extends ActionBarActivity {

	String username;
	double latitude=0;
	double longitude=0;
	String[] streamarray;
	GridView gridview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_nearby);
		Intent intent=getIntent();
		
		LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		LocationListener listener=new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				latitude=location.getLatitude();
				longitude=location.getLongitude();
				Log.e("wq",String.valueOf(latitude)+";"+String.valueOf(longitude));
				new GetNearby().execute("http://wq911206.appspot.com/mobilenearby");
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
		
		
		username=intent.getStringExtra(ShowStreams.USER_NAME);
		TextView tv=(TextView) this.findViewById(R.id.textView1);
		tv.setText(username);
		
		gridview=(GridView) this.findViewById(R.id.gridView1);
		
		Button btback=(Button) this.findViewById(R.id.button1);
		btback.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(ViewNearby.this,ShowStreams.class);
				intent.putExtra(MainActivity.EXTRA_MESSAGE, username);
				//intent.putExtra("streamname", streamname);
				startActivity(intent);
			}
			
		});
		
		
	}
	
	class ImageAdapter extends BaseAdapter{

		private Bitmap []images;
		private String [] streams;
		private Context context;
		
		public ImageAdapter(Context context, Bitmap[] btm,String[] streams){
			this.context=context;
			this.images=btm;
			this.streams=streams;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return images.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View grid =inflater.inflate(R.layout.list_nearbyimages, parent,false);
			ImageView imageview=(ImageView) grid.findViewById(R.id.imageView1);
			//imageview.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 100));
			imageview.setImageBitmap(images[position]);
			TextView textview =(TextView) grid.findViewById(R.id.textView1);
			textview.setText(streams[position]);
			return grid;
		}
		
	}
	
	class GetNearby extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			
			Object[] results=null;
			try {
				JSONObject json = new JSONObject();
				json.put("username", username);
				json.put("latitude", latitude);
				json.put("longitude", longitude);
				HttpClient client = new DefaultHttpClient();
				URI website = new URI(params[0]);
				HttpPost request = new HttpPost();
				request.setURI(website);

				StringEntity data = new StringEntity(json.toString());
				request.setEntity(data);
				HttpResponse response = client.execute(request);

				String tmp = EntityUtils.toString(response.getEntity());
				json = new JSONObject(tmp);

				JSONArray tmp1 = (JSONArray) json.get("datastream");
				JSONArray tmp2 = (JSONArray) json.get("datapic");
				List<Bitmap> list = new ArrayList<Bitmap>();
				List<String> listname=new ArrayList<String>();

				for (int i = 0; i < tmp1.length(); i++) {
					// list.add(tmp1.getString(i));
					// list.add(tmp2.getString(i));
					String imgurl = "http://wq911206.appspot.com/img?img_id="+tmp2.getString(i);
					InputStream in = new URL(imgurl).openStream();
					Bitmap img = BitmapFactory.decodeStream(in);
					list.add(img);
					listname.add(tmp1.getString(i));
				}

				results = list.toArray(new Bitmap[list.size()]);
				streamarray=listname.toArray(new String[listname.size()]);
				
				Log.e("wq","successful");
				//Toast.makeText(ViewNearby.this, "Successful!", Toast.LENGTH_SHORT).show();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("wq","error !!!!!!!!");
				e.printStackTrace();
			}

			return results;
		}
		
		protected void onPostExecute(Object[] result){
			if (result.length==0){
				//tv2.append("No Pictures available");
				return;
			}
			
			Bitmap []images=(Bitmap[]) result;
			gridview.setAdapter(new ImageAdapter(ViewNearby.this,images,streamarray));
			
			gridview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent =new Intent(ViewNearby.this,ShowPictures.class);
					intent.putExtra(ShowStreams.USER_NAME, username);
					intent.putExtra(ShowStreams.STREAM_NAME,streamarray[position]);
					startActivity(intent);
				}
				
			});
			
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_nearby, menu);
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
