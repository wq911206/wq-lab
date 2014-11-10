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
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowPictures extends ActionBarActivity {

	TextView tv1;
	TextView tv2;
	GridView gridview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_pictures);
		
		Intent intent=getIntent();
		final String username=intent.getStringExtra(ShowStreams.USER_NAME);
		final String streamname=intent.getStringExtra(ShowStreams.STREAM_NAME);
		
		tv1=(TextView) this.findViewById(R.id.textView1);
		tv2=(TextView) this.findViewById(R.id.textView2);
		gridview=(GridView) this.findViewById(R.id.gridView1);
		tv1.setText(username);
		tv2.setText(streamname);
		
		new GetPictures().execute("http://wq911206.appspot.com/mobilepic",username,streamname);
		Button bt=(Button) this.findViewById(R.id.button1);
		bt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(ShowPictures.this,ImageUpload.class);
				intent.putExtra("username", username);
				intent.putExtra("streamname", streamname);
				startActivity(intent);
			}
			
		});
		
		Button btback=(Button) this.findViewById(R.id.button2);
		btback.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(ShowPictures.this,ShowStreams.class);
				intent.putExtra(MainActivity.EXTRA_MESSAGE, username);
				//intent.putExtra("streamname", streamname);
				startActivity(intent);
			}
			
		});
		
	}
	
	class ImageAdapter extends BaseAdapter{

		private Bitmap []images;
		private Context context;
		
		public ImageAdapter(Context context, Bitmap[] btm){
			this.context=context;
			this.images=btm;
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
			View grid =inflater.inflate(R.layout.list_images, parent,false);
			ImageView imageview=(ImageView) grid.findViewById(R.id.imageView1);
			//imageview.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 100));
			imageview.setImageBitmap(images[position]);
			return grid;
		}
	}
	
	class GetPictures extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			Object []results=null;
			
			
			try {
				JSONObject json=new JSONObject();
				json.put("username", params[1]);
				json.put("streamname", params[2]);
				
				HttpClient client=new DefaultHttpClient();
				URI website =new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				
				StringEntity data =new StringEntity(json.toString());
				request.setEntity(data);
				
				HttpResponse response=client.execute(request);
				String tmp=EntityUtils.toString(response.getEntity());
				json=new JSONObject(tmp);
				JSONArray tmp1=json.getJSONArray("pictures");
				List<Bitmap> list=new ArrayList<Bitmap>();
				
				for (int i=0;i<tmp1.length();i++){
					String picurl="http://wq911206.appspot.com/img?img_id="+tmp1.getString(i);
					InputStream in =new URL(picurl).openStream();
					Bitmap img=BitmapFactory.decodeStream(in);
					list.add(img);
				}
				
				results=list.toArray(new Bitmap[list.size()]);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return results;
		}
		
		protected void onPostExecute(Object[] result){
			if (result.length==0){
				tv2.append("\n No Pictures available");
				return;
			}
			
			Bitmap []images=(Bitmap[]) result;
			gridview.setAdapter(new ImageAdapter(ShowPictures.this,images));
			
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_pictures, menu);
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
