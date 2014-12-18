package com.example.term;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import android.support.v7.app.ActionBarActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {


	static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    
    private String Email;
    
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.Contacts=null;
        Global.Mycards=null;
        Global.Username=null;
        Global.MyEvents=null;
        Global.OtherEvents=null;
        Global.Notifications=null;
        setContentView(R.layout.activity_main);
    }

    public void userLogin(View view){
    	String[] accountTypes=new String[] {"com.google"};
    	Intent intent=AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
    	startActivityForResult(intent,REQUEST_CODE_PICK_ACCOUNT);
    	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if (requestCode==REQUEST_CODE_PICK_ACCOUNT){
    		if(resultCode==RESULT_OK){
    			Email=data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
    			String [] parts=Email.split("@");
    			Global.Username=parts[0].toLowerCase();
    			
    			//Bundle bundle=data.getExtras();
    			
    			//for (String key: bundle.keySet()){
    				//Object value=bundle.get(key);
    				//Log.e("wq",String.format("%s %s (%s)", key,value.toString(), value.getClass().getName()));
    			//}
    			
    			if(isDeviceOnline()){
    				new LoginTask().execute();
    			}else{
    				Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
    			}
    		}
    		else if(resultCode==RESULT_CANCELED){
    			Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
    		}
    	}
    	else if(requestCode==REQUEST_CODE_RECOVER_FROM_AUTH_ERROR){
    		//new LoginTask().execute();
    		if (data == null) {
    			Toast.makeText(this, "Unknown error, click the button again", Toast.LENGTH_SHORT).show();
                return;
            }
    		if(resultCode==RESULT_OK){
    			new LoginTask().execute();
    			return;
    		}
    		else if(resultCode==RESULT_CANCELED){
    			Toast.makeText(this, "User rejected authorization", Toast.LENGTH_SHORT).show();
    			return;
    		}
    	}
    	super.onActivityResult(requestCode, resultCode, data);
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
    
    private class LoginTask extends AsyncTask<Object, Object, Object>{

		@Override
		protected Object doInBackground(Object... params) {
			try {
				
				String token=GoogleAuthUtil.getToken(MainActivity.this, Email, SCOPE);
				URL url=new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
				HttpURLConnection conn=(HttpURLConnection) url.openConnection();
				int sc=conn.getResponseCode();
				
				if(sc==200){
					InputStream is = conn.getInputStream();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
			        byte[] data = new byte[2048];
			        int len = 0;
			        while ((len = is.read(data, 0, data.length)) >= 0) {
			            bos.write(data, 0, len);
			        }
			        
			        String response=new String(bos.toByteArray());
			        
			        final JSONObject json=new JSONObject(response);
			        final String firstname=json.getString("given_name");
			        
			        
			        Log.e("wq",response);
			        
			        runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			            	Toast.makeText(MainActivity.this, "Welcome, "+firstname, Toast.LENGTH_SHORT).show();
			            	Global.Contacts=new ArrayList<Card>();
			            	Global.Mycards=new ArrayList<Card>();
			            	Global.MyEvents=new ArrayList<Event>();
			            	Global.OtherEvents=new ArrayList<Event>();
			            	Global.Notifications=new ArrayList<Notification>();
			            	startActivity(new Intent(MainActivity.this,Contacts.class));
			            }
			        });
			        
			        is.close();
			        return null;
				}
				else if (sc==401){
					GoogleAuthUtil.invalidateToken(MainActivity.this, token);
		            runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			            	Toast.makeText(MainActivity.this, "Server auth error, please try again", Toast.LENGTH_SHORT).show();
			            }
			        });
				}
			} catch (final UserRecoverableAuthException e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						startActivityForResult(e.getIntent(),REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
						
					}
					
				});
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GoogleAuthException e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
					}
					
				});
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
    	
    }
    
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
    
    @Override
	public void onBackPressed(){
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
}
