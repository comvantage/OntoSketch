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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drawing.application.MainActivity;
import com.drawing.datastructure.OntologyObjectTypes;
import com.drawing.ontosketch.R;

/**
 * CodeBehide class for UI Element IndividualListItem
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 *
 */
public class IndividualListItem extends ListItem
{ 
	private String classname;
	
	private ImageButton childToggleBtn;

	@SuppressLint("NewApi")
	public IndividualListItem(Context context, String itemname, final String classname)
	{
		super(context, itemname);
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ll = (LinearLayout) inflater.inflate(R.layout.individual_list_item, null);
		
		this.addView(ll);
		
		tv = (TextView) ll.findViewById(R.id.primaryText);
		
		tv.setText(itemname);
		
		tv2 = (TextView) ll.findViewById(R.id.secondaryText);
		
		tv2.setText(getResources().getString(R.string.individual_class_of) + " " + classname);
		
		this.classname = classname;
		
		setCurrentState(ListItemState.ACTIVE);
		
		childToggleBtn = (ImageButton) ll.findViewById(R.id.itemButton);
		
		childToggleBtn.setOnClickListener(new OnClickListener() 
		{
			@SuppressLint("NewApi")
			public void onClick(View v) 
			{
				((MainActivity) v.getContext()).selectiveUpdateOntoPanel(OntologyObjectTypes.FORMALIZEDINDIVIDUAL, OntologyObjectTypes.FORMALIZEDCONCEPT, getUri(), classname);
			}
		});
		
	}

	public IndividualListItem(final IndividualListItem li)
	{
		super(li.getContext(), li.getItemname());
		
		inflater = (LayoutInflater) li.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ll = (LinearLayout) inflater.inflate(R.layout.individual_list_item, null);
		
		this.addView(ll);
		
		tv = (TextView) ll.findViewById(R.id.primaryText);
		
		tv.setText(li.getItemname());
		
		tv2 = (TextView) ll.findViewById(R.id.secondaryText);
		
		tv2.setText(getResources().getString(R.string.individual_class_of) + " " + li.getClassName());
		
		setCurrentState(li.getCurrentState());
		
		uri = li.getUri();
		
		childToggleBtn = (ImageButton) ll.findViewById(R.id.itemButton);
		
		childToggleBtn.setOnClickListener(new OnClickListener() 
		{
			@SuppressLint("NewApi")
			public void onClick(View v) 
			{
				((MainActivity) v.getContext()).selectiveUpdateOntoPanel(OntologyObjectTypes.FORMALIZEDINDIVIDUAL, OntologyObjectTypes.FORMALIZEDCONCEPT, getUri(), li.getClassName());
			}
		});
	}
	
	public String getClassName() 
	{
		return classname;
	}
	
}


	
