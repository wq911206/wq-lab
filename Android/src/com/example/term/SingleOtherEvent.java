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
import org.json.JSONException;
import org.json.JSONObject;

import com.example.term.SingleOwnEvent.DeleteTask;
import com.google.common.base.Joiner;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SingleOtherEvent extends ActionBarActivity {

	int position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_other_event);
		position=this.getIntent().getIntExtra("OtherEvent", 0);
		LoadEvent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single_other_event, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_unsubscribe) {
			UnSubscribeEvent();
			return true;
		}
		if(id==R.id.action_share){
			ShareEvent();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void LoadEvent(){
		Event event=Global.OtherEvents.get(position);
		EditText edit=(EditText) this.findViewById(R.id.editText1);
		edit.setText(event.title);
		edit.setInputType(InputType.TYPE_NULL);
		TextView textview=(TextView) this.findViewById(R.id.textView3);
		textview.setText(event.date);
		edit=(EditText) this.findViewById(R.id.editText2);
		edit.setText(event.location);
		edit.setInputType(InputType.TYPE_NULL);
		edit=(EditText) this.findViewById(R.id.editText3);
		edit.setText(event.organizer);
		edit.setInputType(InputType.TYPE_NULL);
		edit=(EditText) this.findViewById(R.id.editText4);
		edit.setText(event.description);
		edit.setInputType(InputType.TYPE_NULL);
		textview=(TextView) this.findViewById(R.id.textView7);
		textview.setText("Subscribers: "+Integer.toString(event.subscribers));
		ImageView img=(ImageView) this.findViewById(R.id.imageView1);
		int qrCodeDimension=500;
		
		try {
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(event.Convert(),     
					null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
					qrCodeDimension);
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			img.setImageBitmap(bitmap);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Event Format Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (JSONException e) {
			Toast.makeText(this, "Event Format Error!", Toast.LENGTH_SHORT).show();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void UnSubscribeEvent(){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("Unsubscribe this event?");
		builder.setCancelable(false);
		builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if(isDeviceOnline()){
					new DeleteTask().execute("http://wq91120602.appspot.com/unsubscribeevent");
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
		
		builder.create().show();
	}
	
	class DeleteTask extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			try {
				HttpClient client=new DefaultHttpClient();
				URI website = new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				JSONObject json=new JSONObject();
				Event event=Global.OtherEvents.get(position);
				json.put("author", event.author);
				json.put("createtime", event.createtime);
				json.put("subscriber", Global.Username);
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				HttpResponse response=client.execute(request);
				if(response.getStatusLine().getStatusCode()==200){
					return 200;
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
				Global.OtherEvents.remove(position);
				startActivity(new Intent(SingleOtherEvent.this,SubscribedEvents.class));
			}
		}
	}
	
	public void ShareEvent(){
		ArrayList<String> temp=new ArrayList<String>();
		for(int i=0;i<Global.Contacts.size();i++){
			Card card=Global.Contacts.get(i);
			temp.add(Joiner.on(" ").join(new String[]{card.firstname,card.lastname}));
		}
		final String []names=temp.toArray(new String[temp.size()]);
		final boolean flags[]=new boolean[names.length];
		if(names.length>0){
			AlertDialog.Builder builder=new AlertDialog.Builder(SingleOtherEvent.this);
			builder.setTitle("Select Contacts");
			builder.setMultiChoiceItems(names, flags, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					// TODO Auto-generated method stub
					flags[which]=isChecked;
				}
				
			});
			builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.e("wq",Integer.toString(position));
					StringBuffer str=new StringBuffer();
					str.append("The following contacts are selected:");
					ArrayList<String> destinations=new ArrayList<String>();
					for(int i=names.length-1;i>=0;i--)
					{
						if(flags[i])
						{
							destinations.add(Global.Contacts.get(i).author);
						}
					}
					AsyncHttpClient client=new AsyncHttpClient();
					RequestParams para=new RequestParams();
					para.put("destination", Joiner.on("**").join(destinations.toArray(new String[destinations.size()])));
					para.put("sender", Global.Username);
					Event event=Global.OtherEvents.get(position);
					para.put("author", event.author);
					para.put("createtime", event.createtime);
					
					client.post("http://wq91120602.appspot.com/shareeventnotification",para, new AsyncHttpResponseHandler(){

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							// TODO Auto-generated method stub
							show("failure");
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1,
								byte[] arg2) {
							// TODO Auto-generated method stub
							show("success");
						}
						
					});
				}
			});
			
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			builder.create().show();
		}
		else{
			show("No Contacts Available");
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
				Toast.makeText(SingleOtherEvent.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
}
