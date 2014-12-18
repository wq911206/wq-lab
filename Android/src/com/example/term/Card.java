package com.example.term;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import android.util.Log;

public class Card {
	public String firstname;
	public String lastname;
	public String company;
	public String author;
	public String createtime;
	
	public ArrayList<String> address;
	public ArrayList<String> phone;
	public ArrayList<String> url;
	public ArrayList<String> email;
	
	/*
	 in case of erroneous scan result, throw json exception will give the activity a 
	 chance to deal with the error, avoiding clash
	 */
	public Card(JSONObject json) throws JSONException{  
		this.firstname=json.getString("firstname");
		this.lastname=json.getString("lastname");
		this.company=json.getString("company");
		this.author=json.getString("author");
		this.createtime=json.getString("createtime");
		
		this.phone=Lists.newArrayList(Splitter.on("**").split(json.getString("phones")));
		this.address=Lists.newArrayList(Splitter.on("**").split(json.getString("addresses")));
		this.email=Lists.newArrayList(Splitter.on("**").split(json.getString("emails")));
		this.url=Lists.newArrayList(Splitter.on("**").split(json.getString("urls")));
	}
	
	public String Convert() throws JSONException{
		JSONObject json=new JSONObject();
		json.put("author",this.author);
		json.put("firstname", this.firstname);
		json.put("lastname", this.lastname);
		json.put("company", this.company);
		json.put("createtime", this.createtime);
		
		json.put("phones", Joiner.on("**").join(this.phone.toArray(new String[this.phone.size()])));
		json.put("addresses", Joiner.on("**").join(this.address.toArray(new String[this.address.size()])));
		json.put("emails", Joiner.on("**").join(this.email.toArray(new String[this.email.size()])));
		json.put("urls", Joiner.on("**").join(this.url.toArray(new String[this.url.size()])));
		return json.toString();
	}
	
	
}
