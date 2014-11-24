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
 
package com.drawing.application;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

import com.drawing.datastructure.CustomPath;
import com.drawing.datastructure.FormalizedConcept;
import com.drawing.datastructure.FormalizedIndividual;
import com.drawing.datastructure.GestureTypes;
import com.drawing.ontosketch.ui.ClassListItem;
import com.drawing.ontosketch.ui.IndividualClassListItem;
import com.drawing.ontosketch.ui.IndividualListItem;
import com.hp.hpl.jena.ontology.OntResource;

/**
 * DragListener for formalized obejects from the ontoPanel
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 *
 */
public class SketchBoardDragListener implements OnDragListener 
{
	    @Override
	    public boolean onDrag(View v, DragEvent event) {
	    	
	    	DrawView sketchBoard = (DrawView) v;
	    	
	    	switch (event.getAction()) {

		      case DragEvent.ACTION_DRAG_STARTED:
		    	  
		    	  sketchBoard.ma.getSearchInput().setCursorVisible(false);
		    	  
		    	  Log.d("DRAG", "ACTION_DRAG_STARTED");
		 
		        break;
		      case DragEvent.ACTION_DRAG_ENTERED:
		    	  
		    	  Log.d("DRAG", "ACTION_DRAG_ENTERED");

		        break;
		      case DragEvent.ACTION_DRAG_EXITED:
		    	  
		    	  Log.d("DRAG", "ACTION_DRAG_EXITED");

		        break;
		      case DragEvent.ACTION_DROP:
		    	  
		    	  Log.d("DRAG", "ACTION_DROP");
		    	  
			        View view = (View) event.getLocalState();
	
			        String uri = event.getClipData().getItemAt(0).getText().toString();
			        
			        OntResource ontresource = MainActivity.getOntologyResource(uri);
			        
			        if (view.getClass().equals(ClassListItem.class))
			        {
			        	CustomPath cp = new CustomPath();
			        	
			        	cp.setHighlighted(false);
			        	cp.setType(GestureTypes.CONCEPT);
			        	
			        	cp.setColor(Color.TRANSPARENT);
			        	
			        	sketchBoard.ma.setOntopanelConceptListItemInactive(uri);
	
			        	FormalizedConcept fc = new FormalizedConcept(cp, sketchBoard.matrix, ontresource.getLocalName(), "");
	
			        	sketchBoard.addFormalizedObject(fc, new Point((int)event.getX(), (int)event.getY()), ontresource);
	
			        }
			        else if(view.getClass().equals(IndividualListItem.class) || view.getClass().equals(IndividualClassListItem.class))
			        {
			        	CustomPath cp = new CustomPath();
			        	
			        	cp.setHighlighted(false);
			        	cp.setType(GestureTypes.INDIVIDUAL);
			        	
			        	cp.setColor(Color.TRANSPARENT);
			        	
			        	sketchBoard.ma.setOntopanelIndividualListItemInactive(uri);
			        	
			        	FormalizedIndividual fi = new FormalizedIndividual(cp, sketchBoard.matrix, ontresource.getLocalName(),"");
	
		        		sketchBoard.addFormalizedObject(fi, new Point((int)event.getX(), (int)event.getY()), ontresource);
			        }
	
			        
			        sketchBoard.invalidate();

	        break;
	      case DragEvent.ACTION_DRAG_ENDED:
	      default:
	        break;
	      }
	      return true;
	    }
	  }