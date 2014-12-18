package com.example.term;

import java.util.ArrayList;

import org.apache.http.Header;

import com.example.term.Contacts.ContactAdapter;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SingleContact extends ActionBarActivity {

	private Menu optmenu=null;
	SelAdapter adapter=null;
	String[] add_type={"Home", "Work"};
	String[] phone_type={"Home", "Work", "Mobile"};
	int position;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_contact);
		Intent intent=this.getIntent();
		position=intent.getIntExtra("Contact", 0);
		Display();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single_contact, menu);
		optmenu=menu;
		return true;
	}
	
	@Override
	public void onBackPressed(){
		NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_share) {
			ShareContact();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void Display(){
		Card card=Global.Contacts.get(position);
		EditText et;
		et=(EditText) SingleContact.this.findViewById(R.id.editText1);
		et.setText(card.lastname);
		et.setInputType(InputType.TYPE_NULL);
		
		et=(EditText) SingleContact.this.findViewById(R.id.editText2);
		et.setText(card.firstname);
		et.setInputType(InputType.TYPE_NULL);
		
		et=(EditText) SingleContact.this.findViewById(R.id.editText3);
		et.setText(card.company);
		et.setInputType(InputType.TYPE_NULL);
		
		ArrayList<String> list;
		list=card.phone;
		for(int i=0;i<list.size();i++){
			ArrayList<String> temp=Lists.newArrayList(Splitter.on(",").split(list.get(i)));
			if(temp.size()!=2){
				continue;
			}
			//TextView tv=(TextView) this.findViewById(R.id.textView4);
			//tv.setVisibility(View.VISIBLE);;
			addPhone(temp.get(1),Integer.parseInt(temp.get(0)));
		}
		
		list=card.address;
		for(int i=0;i<list.size();i++){
			ArrayList<String> temp=Lists.newArrayList(Splitter.on(",").split(list.get(i)));
			if(temp.size()!=2){
				continue;
			}
			//TextView tv=(TextView) this.findViewById(R.id.textView6);
			//tv.setVisibility(View.VISIBLE);
			addAddress(temp.get(1),Integer.parseInt(temp.get(0)));
		}
		
		list=card.email;
		for(int i=0;i<list.size();i++){
			if(list.get(i).length()>0){
				addEmail(list.get(i));
				//TextView tv=(TextView) this.findViewById(R.id.textView5);
				//tv.setVisibility(View.VISIBLE);
			}
		}
		
		list=card.url;
		for(int i=0;i<list.size();i++){
			if(list.get(i).length()>0){
				addURL(list.get(i));
				//TextView tv=(TextView) this.findViewById(R.id.textView7);
				//tv.setVisibility(View.VISIBLE);
			}
		}

	}
	
	public void addPhone(String info,int sel){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addPhone);
		LayoutInflater inflater=(LayoutInflater) SingleContact.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View display =inflater.inflate(R.layout.display_layout,layout,false);
		Spinner spinner=(Spinner) display.findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,phone_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(sel);
		spinner.setEnabled(false);
		EditText et=(EditText) display.findViewById(R.id.editText1);
		et.setInputType(InputType.TYPE_NULL);
		et.setText(info);
		layout.addView(display);
	}
	
	public void addAddress(String info,int sel){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addAddress);
		LayoutInflater inflater=(LayoutInflater) SingleContact.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View display =inflater.inflate(R.layout.display_layout,layout,false);
		Spinner spinner=(Spinner) display.findViewById(R.id.spinner1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,add_type);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(sel);
		spinner.setEnabled(false);
		EditText et=(EditText) display.findViewById(R.id.editText1);
		et.setInputType(InputType.TYPE_NULL);
		et.setText(info);
		layout.addView(display);
	}
	
	public void addEmail(String info){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addEmail);
    	EditText edittext=new EditText(SingleContact.this);
    	edittext.setInputType(InputType.TYPE_NULL);
    	edittext.setText(info);
    	edittext.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    	layout.addView(edittext);
	}
	
	public void addURL(String info){
		ViewGroup layout=(ViewGroup) findViewById(R.id.addURL);
    	EditText edittext=new EditText(SingleContact.this);
    	edittext.setInputType(InputType.TYPE_NULL);
    	edittext.setText(info);
    	edittext.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    	layout.addView(edittext);
	}
	
	public void ShareContact(){
		ArrayList<String> temp=new ArrayList<String>();
		for(int i=0;i<Global.Contacts.size();i++){
			//////////////!!!!!!!!!!!!
			if(i==position){
				continue;
			}
			//////////////!!!!!!!!!!!!
			Card card=Global.Contacts.get(i);
			temp.add(Joiner.on(" ").join(new String[]{card.firstname,card.lastname}));
		}
		final String []names=temp.toArray(new String[temp.size()]);
		final boolean flags[]=new boolean[names.length];
		if(names.length>0){
			AlertDialog.Builder builder=new AlertDialog.Builder(SingleContact.this);
			adapter=new SelAdapter(SingleContact.this,android.R.layout.simple_list_item_1,names);
			builder.setTitle("Select Contacts");
			/*builder.setAdapter(adapter, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});*/
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
					//Toast.makeText(SingleContact.this, Integer.toString(which), Toast.LENGTH_SHORT).show();
					Log.e("wq",Integer.toString(position));
					StringBuffer str=new StringBuffer();
					str.append("The following contacts are selected:");
					ArrayList<String> destinations=new ArrayList<String>();
					for(int i=names.length-1;i>=0;i--)
					//for(int i=adapter.flags.length-1;i>=0;i--)
					{
						if(flags[i])
						//if(adapter.flags[i])
						{
							if(i>=position){
								str.append("\n"+Integer.toString(i+1)+"."+names[i]);
								destinations.add(Global.Contacts.get(i+1).author);
							}
							else{
								str.append("\n"+Integer.toString(i)+"."+names[i]);
								destinations.add(Global.Contacts.get(i).author);
							}
						}
					}
					AsyncHttpClient client=new AsyncHttpClient();
					RequestParams para=new RequestParams();
					para.put("destination", Joiner.on("**").join(destinations.toArray(new String[destinations.size()])));
					para.put("sender", Global.Username);
					Card card=Global.Contacts.get(position);
					para.put("author", card.author);
					para.put("createtime", card.createtime);
					para.put("firstname", card.firstname);
					para.put("lastname", card.lastname);
					
					client.post("http://wq91120602.appspot.com/sharecontactnotification",para, new AsyncHttpResponseHandler(){

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
					//show(str.toString());
				}
			});
			
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			////////////serve as a thrid button for later use
			/*builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});*/
			builder.create().show();
		}
		else{
			show("No Contacts Available");
		}
	}
	
	
	
	//this adapter is outdated
	class SelAdapter extends ArrayAdapter<String>{

		String [] names=null;
		boolean []flags=null;
		
		public SelAdapter(Context context, int resource, String[] objects) {
			super(context, resource, objects);
			this.names=objects;
			this.flags=new boolean[names.length];
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			
			LayoutInflater inflater =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view=inflater.inflate(R.layout.display_contacts,parent,false);
			TextView textview=(TextView) view.findViewById(R.id.textView1);
			CheckBox check=(CheckBox) view.findViewById(R.id.checkBox1);
			textview.setText(names[position]);
			
			check.setVisibility(View.VISIBLE);
			check.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CheckBox ck=(CheckBox) v;
					flags[position]=ck.isChecked();
					//Toast.makeText(getApplicationContext(), names[position]+"is"+ck.isChecked(), Toast.LENGTH_SHORT).show();
				}
			});
			return view;
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
				Toast.makeText(SingleContact.this, str, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	
}
