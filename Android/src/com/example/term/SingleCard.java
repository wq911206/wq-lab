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

import com.example.term.Contacts.DeleteTask;
import com.example.term.CreateNewCard.SendData;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SingleCard extends ActionBarActivity {

	private Menu optmenu;
	ViewGroup parentview;
	String[] add_type={"Home", "Work"};
	String[] phone_type={"Home", "Work", "Mobile"};
	int position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		position=intent.getIntExtra("OwnCard", 0);
		Display();
	}
	
	public void Display(){
		SingleCard.this.setContentView(R.layout.activity_single_card);
		parentview=(ViewGroup) this.findViewById(R.id.scrollView1);
		InputMethodManager inputMethodManager = (InputMethodManager) SingleCard.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(parentview.getWindowToken(), 0);
		LoadCard();
		EditMode(parentview,false);
	}
	
	public void LoadCard(){
		Card card=Global.Mycards.get(position);
		EditText et;
		et=(EditText) SingleCard.this.findViewById(R.id.editText1);
		et.setText(card.lastname);
		
		et=(EditText) SingleCard.this.findViewById(R.id.editText2);
		et.setText(card.firstname);
		
		et=(EditText) SingleCard.this.findViewById(R.id.editText3);
		et.setText(card.company);
		
		ArrayList<String> list;
		list=card.phone;
		for(int i=0;i<list.size();i++){
			ArrayList<String> temp=Lists.newArrayList(Splitter.on(",").split(list.get(i)));
			if(temp.size()!=2){
				continue;
			}
			TextView tv=(TextView) this.findViewById(R.id.textView4);
			tv.setVisibility(View.VISIBLE);
			addPhone(temp.get(1),Integer.parseInt(temp.get(0)));
		}
		
		list=card.address;
		for(int i=0;i<list.size();i++){
			ArrayList<String> temp=Lists.newArrayList(Splitter.on(",").split(list.get(i)));
			if(temp.size()!=2){
				continue;
			}
			TextView tv=(TextView) this.findViewById(R.id.textView6);
			tv.setVisibility(View.VISIBLE);
			addAddress(temp.get(1),Integer.parseInt(temp.get(0)));
		}
		
		list=card.email;
		for(int i=0;i<list.size();i++){
			if(list.get(i).length()>0){
				addEmail(list.get(i));
				TextView tv=(TextView) this.findViewById(R.id.textView5);
				tv.setVisibility(View.VISIBLE);
			}
		}
		
		list=card.url;
		for(int i=0;i<list.size();i++){
			if(list.get(i).length()>0){
				addURL(list.get(i));
				TextView tv=(TextView) this.findViewById(R.id.textView7);
				tv.setVisibility(View.VISIBLE);
			}
		}
		
		try {
			JSONObject json=new JSONObject();
			json.put("author", card.author);
			json.put("createtime", card.createtime);
			ImageView img=(ImageView) this.findViewById(R.id.imageView1);
			int qrCodeDimension=500;
			
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(card.Convert(),     
					null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
					qrCodeDimension);
			Bitmap bitmap=qrCodeEncoder.encodeAsBitmap();
			img.setImageBitmap(bitmap);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Toast.makeText(SingleCard.this, "Card Format Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			Toast.makeText(SingleCard.this, "Card Format Error!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed(){
		NavUtils.navigateUpFromSameTask(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single_card, menu);
		this.optmenu=menu;
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id==R.id.action_edit){
			optmenu.findItem(R.id.action_save).setVisible(true);
			optmenu.findItem(R.id.action_cancel).setVisible(true);
			optmenu.findItem(R.id.action_edit).setVisible(false);
			EditMode(parentview,true);
		}
		if(id==R.id.action_save){
			optmenu.findItem(R.id.action_save).setVisible(false);
			optmenu.findItem(R.id.action_cancel).setVisible(false);
			optmenu.findItem(R.id.action_edit).setVisible(true);
			SaveCard();
			
		}
		if(id==R.id.action_cancel){
			optmenu.findItem(R.id.action_save).setVisible(false);
			optmenu.findItem(R.id.action_cancel).setVisible(false);
			optmenu.findItem(R.id.action_edit).setVisible(true);
			Display();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void addEmail(View view){
		addEmail("Email");
	}
	
	public void addEmail(String info){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addEmail);
    	EditText edittext=new EditText(SingleCard.this);
    	edittext.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
    	edittext.setHint("Email");
    	if(!info.equals("Email")){
    		edittext.setText(info);
    	}
    	edittext.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    	layout.addView(edittext);
	}
	
	public void addURL(View view){
		addURL("URL");
	}
	
	public void addURL(String info){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addURL);
    	EditText edittext=new EditText(SingleCard.this);
    	edittext.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    	edittext.setHint("URL");
    	if(!info.equals("URL")){
    		edittext.setText(info);
    	}
    	edittext.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    	layout.addView(edittext);
	}
	
	public void addAddress(View view){
		addAddress("Address",0);
	}
	
	public void addAddress(String info,int sel){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addAddress);
		LayoutInflater inflater=(LayoutInflater) SingleCard.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View display =inflater.inflate(R.layout.display_layout,layout,false);
		Spinner spinner=(Spinner) display.findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,add_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(sel);
		EditText et=(EditText) display.findViewById(R.id.editText1);
		et.setHint("Address");
		if(!info.equals("Address")){
			et.setText(info);
		}
		et.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
		layout.addView(display);
	}
	
	public void addPhone(View view){
		addPhone("Phone",0);
	}
	
	public void addPhone(String info,int sel){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addPhone);
		LayoutInflater inflater=(LayoutInflater) SingleCard.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View display =inflater.inflate(R.layout.display_layout,layout,false);
		Spinner spinner=(Spinner) display.findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,phone_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(sel);
		EditText et=(EditText) display.findViewById(R.id.editText1);
		et.setHint("Phone");
		et.setInputType(InputType.TYPE_CLASS_NUMBER);
		if(!info.equals("Phone")){
			et.setText(info);
		}
		layout.addView(display);
	}
	
	public void deleteCard(View view){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("Delete this card?");
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if(isDeviceOnline()){
					new DeleteTask().execute("http://wq91120602.appspot.com/deletecard");
				}
				else{
					Toast.makeText(SingleCard.this, "No network connection available", Toast.LENGTH_SHORT).show();
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
	
	//true in editmode, false exit editmode
	public void EditMode(ViewGroup group, Boolean flag){
		
		if(flag){
			TextView tv4=(TextView) this.findViewById(R.id.textView4);
			TextView tv5=(TextView) this.findViewById(R.id.textView5);
			TextView tv6=(TextView) this.findViewById(R.id.textView6);
			TextView tv7=(TextView) this.findViewById(R.id.textView7);
			tv4.setVisibility(View.GONE);
			tv5.setVisibility(View.GONE);
			tv6.setVisibility(View.GONE);
			tv7.setVisibility(View.GONE);
		}
		
		for (int i=0;i<group.getChildCount();i++){
			int id=group.getId();
			View view=group.getChildAt(i);
			if(view instanceof EditText){
				//Log.e("wq","EditText");
				if(!flag){
					((EditText) view).setInputType(InputType.TYPE_NULL);
				}
				else{
					int inputtype;
					switch(id){
					case R.id.addEmail: 
						inputtype=InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS; 
						break;
					case R.id.addPhone: 
						inputtype=InputType.TYPE_CLASS_NUMBER; 
						break;
					case R.id.addAddress: 
						inputtype=InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS; 
						break;
					case R.id.addURL: 
						inputtype=InputType.TYPE_TEXT_VARIATION_URI; 
						break;
					default: 
						inputtype=InputType.TYPE_CLASS_TEXT;
						break;
					}
					((EditText) view).setInputType(inputtype);
				}
			}
			if(view instanceof Button){
				//Log.e("wq","Button");
				if(!flag){
					((Button) view).setVisibility(View.GONE);
				}
				else{
					((Button) view).setVisibility(View.VISIBLE);
				}
			}
			if(view instanceof ImageView){
				//Log.e("wq","ImageView");
				if(flag){
					((ImageView) view).setVisibility(View.GONE);
				}
				else{
					((ImageView) view).setVisibility(View.VISIBLE);
				}
			}
			if(view instanceof Spinner){
				//Log.e("wq","Spinner");
				((Spinner) view).setEnabled(flag);
			}
			if(view instanceof ViewGroup){
				//Log.e("wq","ViewGroup");
				EditMode((ViewGroup) view,flag);
			}
		}
	}
	
	public void SaveCard(){
		String firstname=((EditText)this.findViewById(R.id.editText2)).getText().toString();
		String lastname=((EditText)this.findViewById(R.id.editText1)).getText().toString();
		String company=((EditText)this.findViewById(R.id.editText3)).getText().toString();
		EditText et;
		Spinner sp;
		String str;
		int tp;
		
		if(firstname.length()==0){}
		if(lastname.length()==0){}
		
		Log.e("wq",firstname);
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
			json.put("createtime", Global.Mycards.get(position).createtime);
			
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
		
		if(isDeviceOnline()){
			//Global.Mycards.set(position, new Card(json));
			new SaveTask().execute("http://wq91120602.appspot.com/modifycard",json.toString());
		}
		else{
			Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
		}
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
				json.put("author", Global.Mycards.get(position).author);
				json.put("createtime", Global.Mycards.get(position).createtime);
				StringEntity data=new StringEntity(json.toString());
				request.setEntity(data);
				HttpResponse response=client.execute(request);
				if(response.getStatusLine().getStatusCode()==200){
					return 200;
				}
				else{
					show("Server Error!");
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			if(result!=null){
				Global.Mycards.remove(position);
				startActivity(new Intent(SingleCard.this,MyCards.class));
			}
			
		}
		
	}
	
	class SaveTask extends AsyncTask<String, Object, Object>{

		@Override
		protected Object doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				HttpClient client=new DefaultHttpClient();
				URI website =new URI(params[0]);
				HttpPost request=new HttpPost();
				request.setURI(website);
				StringEntity data=new StringEntity(params[1]);
				request.setEntity(data);
				HttpResponse response=client.execute(request);
				if(response.getStatusLine().getStatusCode()==200){
					return new JSONObject(params[1]);
				}
				else{
					show("Server Error!");
				}
				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Network Failure!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(SingleCard.this, "Input Error!", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result){
			if(result!=null){
				try {
					Global.Mycards.set(position, new Card((JSONObject) result));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					show("Data format error!");
					e.printStackTrace();
				}
				Display();
			}
			
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
				Toast.makeText(SingleCard.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	
}
