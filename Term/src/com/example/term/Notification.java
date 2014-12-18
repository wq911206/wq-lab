package com.example.term;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Notification {
	public int type;
	public String message;
	public String sender;
	public String receivetime;
	public boolean flag;
	
	public Notification(JSONObject json) throws JSONException{
		this.type=json.getInt("type");
		this.message=json.getString("message");
		this.sender=json.getString("sender");
		this.receivetime=json.getString("receivetime");
		this.flag=json.getBoolean("flag");
	}
	
	public String Convert() throws JSONException{
		JSONObject json=new JSONObject();
		json.put("type", this.type);
		json.put("message", this.message);
		json.put("sender", this.sender);
		json.put("receivetime", this.receivetime);
		json.put("flag", this.flag);
		return json.toString();
	}
}
