package com.example.mini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageUpload extends ActionBarActivity {

	File uploadfile;
	String streamname;
	String username;
	ImageView iv;
	TextView tv;
	double latitude;
	double longitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_upload);
		
		Intent intent=getIntent();
		username=intent.getStringExtra("username");
		streamname=intent.getStringExtra("streamname");
		
		tv =(TextView) this.findViewById(R.id.textView1);
		iv=(ImageView) this.findViewById(R.id.imageView1);
		final TextView tvlat=(TextView) this.findViewById(R.id.textView3);
		final TextView tvlon=(TextView) this.findViewById(R.id.textView4);
		
		LocationManager manager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		LocationListener listener=new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				latitude=location.getLatitude();
				longitude=location.getLongitude();
				//tvlat.setText(String.valueOf(latitude));
				//tvlon.setText(String.valueOf(longitude));
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
		
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, (float) 0.1, listener);
		
		final File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
		final FileDialog fileDialog = new FileDialog(this, mPath);
        //fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                //Log.e(getClass().getName(), "selected file " + file.toString());
            	tv.setText("Selected file: " + file.toString());
            	uploadfile=file;
            	//tv.append(getClass().getName());
            	try {
					InputStream in =new FileInputStream(file);
					Bitmap bp=BitmapFactory.decodeStream(in);
					iv.setImageBitmap(bp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            }
        });
        
        Button bt=(Button) this.findViewById(R.id.button1);
        bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				fileDialog.showDialog();
			}       	
        });
        
        Button uploadbutton=(Button) this.findViewById(R.id.button4);
        uploadbutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String url="http://wq911206.appspot.com/mobileupload";
				try {
					Log.e("wq","qqq");
					AsyncHttpClient client=new AsyncHttpClient();
					RequestParams para=new RequestParams();
					para.put("image", uploadfile,"image/jpeg");
					para.put("streamname", streamname);
					para.put("username", username);
					para.put("latitude",latitude);
					para.put("longitude", longitude);
					
					client.post(url,para,new AsyncHttpResponseHandler(){

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2,
								Throwable arg3) {
							// TODO Auto-generated method stub
							Log.e("wq","fail");
							Toast.makeText(ImageUpload.this, "Failure!", Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							// TODO Auto-generated method stub
							Log.e("wq","success");
							Toast.makeText(ImageUpload.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
						}

						});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        });

        Button takephotobutton =(Button) this.findViewById(R.id.button3);
        takephotobutton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path=Environment.getExternalStorageDirectory().getPath()+File.separator+"DCIM"+File.separator+"Camera";
				uploadfile=new File(path,"qiyuanyuan"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".jpg");
				try {
					uploadfile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Intent intent =new Intent(MediaStore .ACTION_IMAGE_CAPTURE);
				//intent.putExtra(MediaStore.EXTRA_OUTPUT, uploadfile.toString());
				startActivityForResult(intent,0);
			}
        	
        });
        
        Button btback=(Button) this.findViewById(R.id.button2);
        
        btback.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(ImageUpload.this,ShowPictures.class);
				intent.putExtra(ShowStreams.USER_NAME, username);
				intent.putExtra(ShowStreams.STREAM_NAME,streamname);
				startActivity(intent);
			}
			
		});
        
	}
	
	@Override
	protected void onActivityResult(int reQuestCode, int resultCode, Intent data){
		super.onActivityResult(reQuestCode, resultCode, data);
		//textview.setText("123");
		
		
		Bitmap bm=(Bitmap) data.getExtras().get("data");
		try {
			FileOutputStream fout=new FileOutputStream(uploadfile);
			bm.compress(Bitmap.CompressFormat.JPEG, 100,fout);
			fout.flush();
			fout.close();
			InputStream in =new FileInputStream(uploadfile);
			Bitmap tmp=BitmapFactory.decodeStream(in);
			iv.setImageBitmap(tmp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tv.setText(uploadfile.toString());
		
		//textview.setText("123");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_upload, menu);
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
