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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.drawing.ontosketch.R;

/**
 * CodeBehide class for UI Element IndividualClassListItem
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 *
 */
public class IndividualClassListItem extends ListItem 
{

	public IndividualClassListItem(Context context, String itemname, int layer)
	{
		super(context, itemname);
	
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ll = (LinearLayout) inflater.inflate(R.layout.class_individual_list_item, null);
		
		this.addView(ll);
		
		tv = (TextView) ll.findViewById(R.id.primaryText);
		
		tv.setText(itemname);
		
		LayoutParams params = (LayoutParams) tv.getLayoutParams();
		params.setMargins(5+(25*layer), 0, 5, 0); //left, top, right, bottom
		tv.setLayoutParams(params);

		setCurrentState(ListItemState.ACTIVE);
		
		this.setBackgroundColor(getResources().getColor(R.color.tabBtnBackground));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{

		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				this.setBackgroundColor(getResources().getColor(R.color.tabBackground));
				break;
			case MotionEvent.ACTION_UP:
				this.setBackgroundColor(getResources().getColor(R.color.tabBtnBackground));
				break;
			case MotionEvent.ACTION_CANCEL:
				this.setBackgroundColor(getResources().getColor(R.color.tabBtnBackground));
				break;
			case MotionEvent.ACTION_MOVE:
				this.setBackgroundColor(getResources().getColor(R.color.tabBtnBackground));
				break;
		}
		
		return false;
	}
	
}
