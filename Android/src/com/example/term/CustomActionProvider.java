package com.example.term;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CustomActionProvider extends ActionProvider {

	Context mContext;
	String[] str;
	public CustomActionProvider(Context context, String[] str) {
		super(context);
		mContext=context;
		this.str=str;
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateActionView() {
		// TODO Auto-generated method stub
		LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view=inflater.inflate(R.layout.menu_actionprovider, null);
		ListView list=(ListView) view.findViewById(R.id.listView1);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, str);
		list.setAdapter(adapter);
		
		return view;
	}

}
