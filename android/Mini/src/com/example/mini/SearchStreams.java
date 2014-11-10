package com.example.mini;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchStreams extends ActionBarActivity {

	String[] streamarray;
	String[] coverurlarray;
	String username;

	String[] results;
	String[] urls;
	
	GridView gridview;
	
	TextView tvstream;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_streams);
		Intent intent = getIntent();
		streamarray = intent.getStringArrayExtra("streamarray");
		coverurlarray = intent.getStringArrayExtra("coverurlarray");
		username=intent.getStringExtra(ShowStreams.USER_NAME);
		
		//Log.e("wq",streamarray+":"+coverurlarray);
		
		gridview=(GridView) this.findViewById(R.id.gridView1);
		tvstream=(TextView) this.findViewById(R.id.textView1);
		Button bt=(Button) this.findViewById(R.id.button1);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				SearchStreams.this,
				R.layout.support_simple_spinner_dropdown_item, streamarray);
		
		final AutoCompleteTextView auto = (AutoCompleteTextView) this
				.findViewById(R.id.autoCompleteTextView1);
		auto.setAdapter(adapter);
		
		
		
		bt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String se=auto.getText().toString();
				//Log.e("wq","se:"+se==null);
				List<String> tmp=new ArrayList<String>();
				List<String> tmpurl=new ArrayList<String>();
				for (int i=0;i<streamarray.length;i++){
					if(se.length()==0){
						break;
					}
					if(streamarray[i].contains(se)){
						tmp.add(streamarray[i]);
						tmpurl.add(coverurlarray[i]);
					}
				}
				results=tmp.toArray(new String[tmp.size()]);
				urls=tmpurl.toArray(new String[tmpurl.size()]);
				
				new GetCover().execute("wq");
			}
			
		});
		
		Button btback=(Button) this.findViewById(R.id.button2);
		btback.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(SearchStreams.this,ShowStreams.class);
				intent.putExtra(MainActivity.EXTRA_MESSAGE, username);
				//intent.putExtra("streamname", streamname);
				startActivity(intent);
			}
			
		});
		
		

	}
	
	class StreamAdapter extends BaseAdapter {

		private Context context;
		Bitmap[] data;

		public StreamAdapter(Context context, Bitmap[] data) {
			this.context = context;
			this.data=data;
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

			Bitmap tmp = data[position];
			textview.setText(results[position]);
			imageview.setImageBitmap(tmp);
			return grid;
		}

		
	}

	class GetCover extends AsyncTask<Object,Object,Object[]>{

		@Override
		protected Object[] doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Object[] results=null;
			List<Bitmap> list=new ArrayList<Bitmap>();
			
			for (int i=0;i<urls.length;i++){
				try {
					String imgurl=urls[i];
					InputStream in = new URL(imgurl).openStream();
					Bitmap img = BitmapFactory.decodeStream(in);
					list.add(img);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
			results=list.toArray(new Bitmap[list.size()]);
			return results;
		}
		
		protected void onPostExecute(Object[] result) {
			// tvstream.append(result[0]);
			if (result.length == 0) {
				tvstream.setText("No streams available");
				//return;
			}
			else{
				tvstream.setText("Found "+result.length+" streams:");
			}
			Bitmap[] streams=(Bitmap[]) result;
			gridview.setAdapter(new StreamAdapter(SearchStreams.this, streams));
			
			gridview.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Intent intent =new Intent(SearchStreams.this,ShowPictures.class);
					intent.putExtra(ShowStreams.USER_NAME, username);
					intent.putExtra(ShowStreams.STREAM_NAME,results[position]);
					startActivity(intent);
				}
				
			});
			
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_streams, menu);
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
