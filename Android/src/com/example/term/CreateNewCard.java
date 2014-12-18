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
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Joiner;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateNewCard extends ActionBarActivity {

	String[] add_type={"Home", "Work"};
	String[] phone_type={"Home", "Work", "Mobile"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_card);
	}
	
	public void addEmail(View view){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addEmail);
    	EditText edittext=new EditText(CreateNewCard.this);
    	edittext.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
    	edittext.setHint("Email");
    	edittext.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    	layout.addView(edittext);
	}
	
	public void addURL(View view){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addURL);
    	EditText edittext=new EditText(CreateNewCard.this);
    	edittext.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    	edittext.setHint("URL");
    	edittext.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    	layout.addView(edittext);
	}
	
	public void addAddress(View view){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addAddress);
		LayoutInflater inflater=(LayoutInflater) CreateNewCard.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View display =inflater.inflate(R.layout.display_layout,layout,false);
		Spinner spinner=(Spinner) display.findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,add_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		EditText et=(EditText) display.findViewById(R.id.editText1);
		et.setHint("Address");
		et.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
		layout.addView(display);
	}
	
	public void addPhone(View view){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addPhone);
		LayoutInflater inflater=(LayoutInflater) CreateNewCard.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View display =inflater.inflate(R.layout.display_layout,layout,false);
		Spinner spinner=(Spinner) display.findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,phone_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		EditText et=(EditText) display.findViewById(R.id.editText1);
		et.setHint("Phone");
		et.setInputType(InputType.TYPE_CLASS_NUMBER);
		layout.addView(display);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_new_card, menu);
		return true;
	}
	
	@Override
	public void onBackPressed(){
		NavUtils.navigateUpFromSameTask(this);
	}
	
	public void CreateCard(View view){
		String firstname=((EditText)this.findViewById(R.id.editText2)).getText().toString();
		String lastname=((EditText)this.findViewById(R.id.editText1)).getText().toString();
		String company=((EditText)this.findViewById(R.id.editText3)).getText().toString();
		EditText et;
		Spinner sp;
		String str;
		int tp;
		
		if(firstname.length()==0){}
		if(lastname.length()==0){}
		
		ViewGroup layout=(ViewGroup) findViewById(R.id.addPhone);
		ArrayList<String> phonelist=new ArrayList<String>();
		for(int i=0;i<layout.getChildCount();i++){
			if(!(layout.getChildAt(i) instanceof Button || layout.getChildAt(i) instanceof TextView)){
				et=(EditText) layout.getChildAt(i).findViewById(R.id.editText1);
				str=et.getText().toString();
				sp=(Spinner) layout.getChildAt(i).findViewById(R.id.spinner1);
				tp=sp.getSelectedItemPosition();
				if(str.length()>0){
					phonelist.add(Joiner.on(",").join(tp,str));
				}
			}
		}
		
		layout=(ViewGroup) findViewById(R.id.addEmail);
		ArrayList<String> emaillist=new ArrayList<String>();
		for(int i=0;i<layout.getChildCount();i++){
			if(layout.getChildAt(i) instanceof EditText){
				et=(EditText) layout.getChildAt(i);
				str=et.getText().toString();
				if(str.length()>0){
					emaillist.add(str);
				}
			}
		}
		
		layout=(ViewGroup) findViewById(R.id.addAddress);
		ArrayList<String> addresslist=new ArrayList<String>();
		for(int i=0;i<layout.getChildCount();i++){
			if(! (layout.getChildAt(i) instanceof Button || layout.getChildAt(i) instanceof TextView)){
				et=(EditText) layout.getChildAt(i).findViewById(R.id.editText1);
				str=et.getText().toString();
				sp=(Spinner) layout.getChildAt(i).findViewById(R.id.spinner1);
				tp=sp.getSelectedItemPosition();
				if(str.length()>0){
					addresslist.add(Joiner.on(",").join(tp,str));
				}
			}
		}
		
		layout=(ViewGroup) findViewById(R.id.addURL);
		ArrayList<String> urllist=new ArrayList<String>();
		for(int i=0;i<layout.getChildCount();i++){
			if(layout.getChildAt(i) instanceof EditText){
				et=(EditText) layout.getChildAt(i);
				str=et.getText().toString();
				if(str.length()>0){
					urllist.add(str);
				}
			}
		}
		JSONObject json=new JSONObject();
		try {
			json.put("author",Global.Username);
			json.put("firstname", firstname);
			json.put("lastname", lastname);
			json.put("company", company);
			
			json.put("phones", Joiner.on("**").join(phonelist.toArray(new String[phonelist.size()])));
			json.put("addresses", Joiner.on("**").join(addresslist.toArray(new String[addresslist.size()])));
			json.put("emails", Joiner.on("**").join(emaillist.toArray(new String[emaillist.size()])));
			json.put("urls", Joiner.on("**").join(urllist.toArray(new String[urllist.size()])));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Input Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
		
		//Log.e("wq",json.toString());
		if(isDeviceOnline()){
			new SendData().execute("http://wq91120602.appspot.com/createcontact",json.toString());
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
	}
	
	class SendData extends AsyncTask<String, Object, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			int responsecode=0;
			String str=null;
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
					JSONObject js=new JSONObject(params[1]);
					js.put("createtime", new JSONObject(EntityUtils.toString(response.getEntity())).get("createtime"));
					str=js.toString();
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewCard.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewCard.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(CreateNewCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			
			Object[] result=new Object[2];
			result[0]=str;
			//result[1]=str;
			return result;
		}
		
		@Override
		protected void onPostExecute(Object[] result){
			String message=(String) result[0];
			if(message!=null){
				try {
					Log.e("wq",message);
					JSONObject json=new JSONObject(message);
					Global.Mycards.add(new Card(json));
					Toast.makeText(CreateNewCard.this, "New Card Created Successfully!", Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				Toast.makeText(CreateNewCard.this, "Failure!", Toast.LENGTH_SHORT).show();
			}
			NavUtils.navigateUpFromSameTask(CreateNewCard.this);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.action_cancel_create){
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
