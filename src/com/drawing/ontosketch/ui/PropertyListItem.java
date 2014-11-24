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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.drawing.application.MainActivity;
import com.drawing.ontosketch.R;

/**
 * CodeBehide class for UI Element PropertyListItem
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 *
 */
public class PropertyListItem extends ListItem implements OnCheckedChangeListener
{
	private String itemname;
	private String namespace;
	private String uri;
	
	private int color;
	
	private PropertyIconBackground iconBackground;

	private boolean allowDrag = false;

	protected LayoutInflater inflater;

	protected LinearLayout ll;

	protected CheckBox cb;
	
	private MainActivity ma;
	
	@SuppressLint("NewApi")
	public PropertyListItem(Context context, String itemname, int color)
	{
		super(context, itemname);
		
		ma = (MainActivity) getContext();

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		this.setLayoutParams(params);

		this.itemname = itemname;
		
		this.color = color;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ll = (LinearLayout) inflater.inflate(R.layout.property_list_item, null);

		this.addView(ll);

		cb = (CheckBox) ll.findViewById(R.id.primaryText);

		cb.setText(itemname);
		
		iconBackground = (PropertyIconBackground) ll.findViewById(R.id.imageDraw);
		
		iconBackground.setColor(color);

		setCurrentState(ListItemState.INACTIVE);
		
		this.setOnTouchListener(null);

	}

	public PropertyListItem(PropertyListItem li)
	{
		super(li.getContext(), li.getItemname());
		
		ma = (MainActivity) getContext();

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		this.setLayoutParams(params);

		this.itemname = li.getItemname();
		
		this.color = li.getColor();

		inflater = (LayoutInflater) li.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ll = (LinearLayout) inflater.inflate(R.layout.property_list_item, null);

		this.addView(ll);

		cb = (CheckBox) ll.findViewById(R.id.primaryText);

		cb.setText(itemname);
		
		iconBackground = (PropertyIconBackground) ll.findViewById(R.id.imageDraw);
		
		iconBackground.setColor(color);

		setCurrentState(li.getCurrentState());
		
		this.setOnTouchListener(null);
		
		uri = li.getUri();
	}

	private ListItemState _currentState = ListItemState.NONE;

	public void setCurrentState(ListItemState clis)
	{
		if (clis != _currentState)
		{
			_currentState = clis;

			switch (clis)
			{
			case ACTIVE:

				drawActive();

				break;

			case INACTIVE:

				drawInactive();

				break;

			default:
				break;
			}

		}

		return;
	}

	public ListItemState getCurrentState()
	{
		return _currentState;
	}
	
	private void drawInactive()
	{
		iconBackground.setColor(getResources().getColor(R.color.tabBtnBackground));
		
		cb.setOnCheckedChangeListener(null);
		
		cb.setChecked(false);
		cb.setEnabled(false);
		
		ma.setOntopanelPropertyListItemActive(uri, false);

	}

	private void drawActive()
	{
		iconBackground.setColor(color);
		
		cb.setChecked(true);
		cb.setEnabled(true);
		
		ma.setOntopanelPropertyListItemActive(uri, true);
		
		cb.setOnCheckedChangeListener(this);

	}
	
	public String getNamespace()
	{
		return namespace;
	}


	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}


	public String getUri()
	{
		return uri;
	}


	public void setUri(String uri)
	{
		this.uri = uri;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if(isChecked)
		{
			ma.showPropertyRelation(this.uri, this.color);
		}
		else 
		{
			ma.hidePropertyRelation(this.uri);
		}
		
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
