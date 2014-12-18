package com.example.term;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Event {
	public String title;
	public String author;
	public String date;
	public String location;
	public String organizer;
	public String description;
	public String createtime;
	public int subscribers;
	
	public Event(JSONObject json) throws JSONException{
		this.title=json.getString("title");
		this.author=json.getString("author");
		String str=json.getString("date");
		if(str.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}")){
			this.date=str;
		}
		else{
			this.date=str.substring(0, str.length()-3);
		}
		this.location=json.getString("location");
		this.organizer=json.getString("organizer");
		this.description=json.getString("description");
		this.createtime=json.getString("createtime");
		this.subscribers=json.getInt("subscribers");
	}
	
	public String Convert() throws JSONException{
		JSONObject json=new JSONObject();
		json.put("title", this.title);
		json.put("author",this.author);
		json.put("date", this.date);
		json.put("location", this.location);
		json.put("organizer", this.organizer);
		json.put("description", this.description);
		json.put("createtime", this.createtime);
		json.put("subscribers", this.subscribers);
		return json.toString();
	}
}
