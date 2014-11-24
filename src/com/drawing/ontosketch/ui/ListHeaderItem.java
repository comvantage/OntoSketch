/**
 * 
 *Copyright (c) 2014, SAP SE: Christian Brändel | Florian Schneider | Angelika Salmen | Technische Universität Dresden, Chair of Media Design: Marius Brade | Rainer Groh
 *On behalf of Marius Brade, this research project (contract no. 080951799) was funded by the European Social Fond and the Free State of Saxony as well as SAP Research. On behalf of Angelika Salmen, the research leading to these results was partly funded by the European Community's Seventh Framework Programme under grant agreement no. FP7-284928 ComVantage.
 *All rights reserved.
 *Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
 
package com.drawing.ontosketch.ui;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drawing.application.MainActivity;
import com.drawing.ontosketch.R;

/**
 * CodeBehide class for UI Element ListHeaderItem
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 *
 */
public class ListHeaderItem extends LinearLayout implements OnClickListener, OnTouchListener
{
	private String itemname;

	protected LayoutInflater inflater;

	protected RelativeLayout ll;

	protected TextView tv;
	
	private boolean open = true;
	
	final ImageButton collapseBtn;
	final ImageButton deleteBtn;
	
	private ArrayList<ListItem> childs;

	@SuppressLint("NewApi")
	public ListHeaderItem(final Context context, final String itemname)
	{
		super(context);
		
		childs = new ArrayList<ListItem>();

		this.itemname = itemname;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ll = (RelativeLayout) inflater.inflate(R.layout.listheader_item, null);

		this.addView(ll);

		tv = (TextView) ll.findViewById(R.id.primaryText);

		tv.setText(itemname);
		
		this.setOnClickListener(this);
		this.setOnTouchListener(this);
		
		collapseBtn = (ImageButton) ll.findViewById(R.id.collapseButton); 
		deleteBtn = (ImageButton) ll.findViewById(R.id.deleteButton); 
		
		collapseBtn.setOnClickListener(this);
		
		deleteBtn.setOnClickListener(new OnClickListener() 
		{
			@SuppressLint("NewApi")
			public void onClick(View v) 
			{
				((MainActivity) context).showRemoveOntologyDialog(itemname);
			}
		});
	}

	public ArrayList<ListItem> getChilds()
	{
		return childs;
	}

	public void addChildren(ListItem cll)
	{
		if (!childs.contains(cll))
		{
			childs.add(cll);
		}
		
	}

	@Override
	public void onClick(View v)
	{
		for (ListItem li : childs)
		{
			if (open) 
			{

				li.setVisibility(View.GONE);
				collapseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_listitem_close2));
				
			}
			else 
			{
				li.setVisibility(View.VISIBLE);
				li.setIconOpen();
				
				collapseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_listitem_open2));
			}
		}
		
		open = !open;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{

		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				this.setBackgroundColor(getResources().getColor(R.color.tabBtnBackground));
				break;
			case MotionEvent.ACTION_UP:
				this.setBackgroundColor(getResources().getColor(R.color.tabBackground));
				break;
			case MotionEvent.ACTION_CANCEL:
				this.setBackgroundColor(getResources().getColor(R.color.tabBackground));
				break;
			case MotionEvent.ACTION_MOVE:
				this.setBackgroundColor(getResources().getColor(R.color.tabBackground));
				break;
		}
		
		return false;
	}


}
