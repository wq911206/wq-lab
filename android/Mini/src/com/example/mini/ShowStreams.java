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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowStreams extends ActionBarActivity {

	TextView tvstream;
	GridView gv;
	String username;
	double latitude;
	double longitude;
	static final String USER_NAME="com.example.mini.username";
	static final String STREAM_NAME="com.example.mini.streamname";
	String[] streamarray;
	String[] coverurlarray;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_streams);
		Intent intent = getIntent();
		
		
		username = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

		TextView tv = (TextView) findViewById(R.id.textView1);

		tv.setText(username);
		tvstream = (TextView) findViewById(R.id.textView2);
		gv = (GridView) findViewById(R.id.gridView1);

		new GetStreams().execute("http://wq911206.appspot.com/mobilestream",username);
		
		Button btnearby=(Button) this.findViewById(R.id.button1);
		btnearby.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(ShowStreams.this,ViewNearby.class);
				intent.putExtra(USER_NAME, username);
				startActivity(intent);
				
			}
			
		});
		
		Button btsearch=(Button) this.findViewById(R.id.button2);
		btsearch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent (ShowStreams.this,SearchStreams.class);
				//Log.e("wq",streamarray.length+":"+coverurlarray.length);
				intent.putExtra("streamarray",streamarray);
				intent.putExtra("coverurlarray", coverurlarray);
				intent.putExtra(USER_NAME, username);
				startActivity(intent);
				
			}
			
		});
		
		Button btlouout=(Button) this.findViewById(R.id.button3);
		btlouout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(ShowStreams.this,MainActivity.class);
				startActivity(intent);
			}
			
		});
		
	}

	class StreamCover {
		public String name = null;
		public Bitmap coverimg = null;

		public StreamCover(String name, Bitmap img) {
			this.name = name;
			this.coverimg = img;
		}
	}

	class StreamAdapter extends BaseAdapter {

		private StreamCover[] data = null;
		private Context context;

		public StreamAdapter(Context context,StreamCover[] data) {
			this.context = context;
			this.data = data;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.length;
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

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View grid = inflater.inflate(R.layout.list_stream, parent, false);

			ImageView imageview = (ImageView) grid
					.findViewById(R.id.imageView1);
			TextView textview = (TextView) grid.findViewById(R.id.textView3);

			StreamCover tmp = data[position];
			textview.setText(tmp.name);
			imageview.setImageBitmap(tmp.coverimg);
			return grid;
		}
	}

	class GetStreams extends AsyncTask<String, Object, Object[]> {

		@Override
		protected Object[] doInBackground(String... params) {
			Object[] results = null;

			try {
				JSONObject json = new JSONObject();
				json.put("username", params[1]);
				HttpClient client = new DefaultHttpClient();
				URI website = new URI(params[0]);
				HttpPost request = new HttpPost();
				request.setURI(website);

				StringEntity data = new StringEntity(json.toString());
				request.setEntity(data);
				HttpResponse response = client.execute(request);

				String tmp = EntityUtils.toString(response.getEntity());
				json = new JSONObject(tmp);

				JSONArray tmp1 = (JSONArray) json.get("streamnames");
				JSONArray tmp2 = (JSONArray) json.get("streamcoverurls");
				List<StreamCover> list = new ArrayList<StreamCover>();
				List<String> tmplist=new ArrayList<String>();
				List<String> tmpcover=new ArrayList<String>();

				for (int i = 0; i < tmp1.length(); i++) {
					// list.add(tmp1.getString(i));
					// list.add(tmp2.getString(i));
					String imgurl = tmp2.getString(i);
					InputStream in = new URL(imgurl).openStream();
					Bitmap img = BitmapFactory.decodeStream(in);
					list.add(new StreamCover(tmp1.getString(i), img));
					tmplist.add(tmp1.getString(i));
					tmpcover.add(imgurl);
				}

				results = list.toArray(new StreamCover[list.size()]);
				streamarray=tmplist.toArray(new String[tmplist.size()]);
				coverurlarray=tmpcover.toArray(new String[tmpcover.size()]);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("wq","errorerror");
				e.printStackTrace();
			}

			return results;
		}

		protected void onPostExecute(Object[] result) {
			// tvstream.append(result[0]);
			if (result.length == 0) {
				tvstream.setText("No streams available");
				return;
			}
			final StreamCover[] data = (StreamCover[]) result;
			gv.setAdapter(new StreamAdapter(ShowStreams.this, data));
			
			gv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent =new Intent(ShowStreams.this,ShowPictures.class);
					intent.putExtra(USER_NAME, username);
					intent.putExtra(STREAM_NAME,data[position].name );
					startActivity(intent);
				}
				
			});
			
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_streams, menu);
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
